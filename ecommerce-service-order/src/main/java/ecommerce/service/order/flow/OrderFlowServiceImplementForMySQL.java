package ecommerce.service.order.flow;


import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ecommerce.service.KeyIdentifies;
import ecommerce.service.client.ProductService;
import ecommerce.service.client.Result;
import ecommerce.service.client.base.Order;
import ecommerce.service.client.base.Product;
import ecommerce.service.client.base.ShopCartOrder;
import ecommerce.service.order.client.OrderFlowException;
import ecommerce.service.order.client.OrderFlowService;
import ecommerce.service.order.dao.OrderDAOFromMySQL;

/**
 * 1/5/10/30 minutes must pay
 * actived->paid->completed
 * actived->paid->refund->completed
 * actived->part paid->completed
 * actived->no pay->closed
 */
@Service
public class OrderFlowServiceImplementForMySQL implements OrderFlowService{

	@Autowired
	private OrderDAOFromMySQL orderDAO;

	@Resource
	private StringRedisTemplate stringRedisTemplate;

	@Resource
	private ProductService productService;

	@Override
	@Transactional(rollbackFor = OrderFlowException.class)
	public Order createOrder(ShopCartOrder shopCartOrder) throws OrderFlowException{
		Order order = new Order();

		Boolean existed = stringRedisTemplate.opsForSet().isMember(KeyIdentifies.ShopCartOrder.value, shopCartOrder.getToken());
		if (existed==null||existed) {
			return order;
		}

		Long id = stringRedisTemplate.opsForValue().increment(KeyIdentifies.OrderId.value, 1);
		if (id==null) {
			throw new OrderFlowException(String.format("Failed to get main order: %d", shopCartOrder.getUserId()));
		}

		long userId = shopCartOrder.getUserId(),leftOne=1l<<62;
		while((userId&leftOne)==0) {userId<<=1;}
		order.setId(id|userId);
		order.setUserId(shopCartOrder.getUserId());
		order.setStatus(Order.Status.Actived.value);
		order.setCreateTime((int) (shopCartOrder.getCreateTime()/1000));
		order.setParentId(0l);
		order.setProductId(0l);
		
		order.setPaidAmount(0l);
		order.setReceiptAmount(0l);

		Result<List<Long>> batchResult = productService.increBatchQuantity(shopCartOrder.getProductIds(), shopCartOrder.getQuantities());
		if (batchResult==null||batchResult.getCode()!=Result.Code.OK.value) {
			throw new OrderFlowException(String.format("quantities not available: %d %s", shopCartOrder.getUserId(), shopCartOrder.getToken()));
		}
		try {
			for(int i=0; i<shopCartOrder.getProductIds().size(); i++) {
				if (batchResult.getData().get(i)==null) {continue;}
				
				Result<Product> prodResult = productService.getProductById(shopCartOrder.getProductIds().get(i));
				if (prodResult==null||prodResult.getCode()!=Result.Code.OK.value) {
					throw new OrderFlowException(String.format("product not exist: %d", order.getProductId()));
				}
				Order subOrder = new Order();
				Long subId = stringRedisTemplate.opsForValue().increment(KeyIdentifies.SubOrderId.value, 1);
				if (subId==null) {
					throw new OrderFlowException(String.format("Failed to get sub order id: %d $d", order.getId(), shopCartOrder.getUserId()));
				}
				subOrder.setId(subId|userId);
				subOrder.setUserId(shopCartOrder.getUserId());
				subOrder.setStatus(Order.Status.Actived.value);
				subOrder.setProductId(shopCartOrder.getProductIds().get(i));
				subOrder.setCreateTime(order.getCreateTime());
				subOrder.setParentId(order.getId());
				
				subOrder.setPaidAmount(0l);
				subOrder.setReceiptAmount(prodResult.getData().getPrice()*shopCartOrder.getQuantities().get(i));
				if (!orderDAO.insert(subOrder)) {
					throw new OrderFlowException(String.format("Failed to add sub order: %d %d %d %d", order.getId(), subId, shopCartOrder.getUserId(), shopCartOrder.getProductIds().get(i)));
				}
				order.setReceiptAmount(subOrder.getReceiptAmount()+order.getReceiptAmount());
			}

			if (!orderDAO.insert(order)) {
				throw new OrderFlowException(String.format("Failed to add main order: %d", id));
			}
		}catch(Exception e) {
			for(int i=0; i<shopCartOrder.getQuantities().size(); i++) {
				shopCartOrder.getQuantities().set(i, -shopCartOrder.getQuantities().get(i));
			}
			Result<List<Long>> rollbackBatchResult = productService.increBatchQuantity(shopCartOrder.getProductIds(), shopCartOrder.getQuantities());
			if (rollbackBatchResult==null||rollbackBatchResult.getCode()!=Result.Code.OK.value) {
				e.addSuppressed(new OrderFlowException(String.format("quantities not available: %d %s", shopCartOrder.getUserId(), shopCartOrder.getToken())));
			}
			throw e;
		}
		stringRedisTemplate.opsForSet().remove(KeyIdentifies.ShopCartOrder.value, shopCartOrder.getToken());
		return order;
	}

	@Override
	@Transactional(rollbackFor = OrderFlowException.class)
	public Order payment(Order order) throws OrderFlowException {
		if (order.getStatus()!=Order.Status.Actived.value) {
			throw new OrderFlowException(String.format("order status: %d %d", order.getId(), order.getStatus()));
		}
		if (order.getPaidAmount()==0) {
			throw new OrderFlowException(String.format("order payment amount not accepted: %d %d", order.getId(), order.getPaidAmount()));
		}
		if (!orderDAO.increPaidAmount(order.getId(), order.getPaidAmount())) {
			throw new OrderFlowException(String.format("Failed to add paid amount on main order: %d %d", order.getId(), order.getPaidAmount()));
		}
		
		if (!orderDAO.updateStatus(order, Order.Status.Paid.value)) {
			throw new OrderFlowException(String.format("Failed to update status to Paid on main order: %d %d", order.getId(), order.getStatus()));
		}
		order.setStatus(Order.Status.Paid.value);
		
		if (order.getParentId()>0) {
			if (!orderDAO.increPaidAmount(order.getParentId(), order.getPaidAmount())) {
				throw new OrderFlowException(String.format("Failed to add paid amount on parent order: %d %d", order.getParentId(), order.getPaidAmount()));
			}
		}else {
			Order subOrderExample = new Order();
			subOrderExample.setParentId(order.getId());
			List<Order> paidOrders = orderDAO.queryOrders(subOrderExample, 0, 1000);
			paidOrders.stream()
				.filter(paidOrder->paidOrder.getStatus()==Order.Status.Actived.value)
				.forEach(paidOrder->{
					orderDAO.updateStatus(paidOrder, Order.Status.Paid.value);
					paidOrder.setStatus(Order.Status.Paid.value);
				});
		}
		return order;
	}

	@Override
	@Transactional(rollbackFor = OrderFlowException.class)
	public Order refund(Order order) throws OrderFlowException {
		if (order.getStatus()!=Order.Status.Paid.value) {
			throw new OrderFlowException(String.format("order status: %d %d", order.getId(), order.getStatus()));
		}
		if (!orderDAO.increPaidAmount(order.getId(), -order.getPaidAmount())) {
			throw new OrderFlowException(String.format("Failed to add paid amount on main order: %d %d", order.getId(), order.getPaidAmount()));
		}
		
		if (!orderDAO.updateStatus(order, Order.Status.Refund.value)) {
			throw new OrderFlowException(String.format("Failed to update status to Refund on main order: %d %d", order.getId(), order.getStatus()));
		}
		order.setStatus(Order.Status.Refund.value);
		
		if (order.getParentId()>0) {
			if (!orderDAO.increPaidAmount(order.getParentId(), -order.getPaidAmount())) {
				throw new OrderFlowException(String.format("Failed to add paid amount on parent order: %d %d", order.getParentId(), order.getPaidAmount()));
			}
		}else {
			Order subOrderExample = new Order();
			subOrderExample.setParentId(order.getId());
			List<Order> paidOrders = orderDAO.queryOrders(subOrderExample, 0, 1000);
			paidOrders.stream()
				.filter(paidOrder->paidOrder.getStatus()==Order.Status.Paid.value)
				.forEach(paidOrder->{
					orderDAO.updateStatus(paidOrder, Order.Status.Refund.value);
					paidOrder.setStatus(Order.Status.Refund.value);
				});
		}
		return order;
	}

	@Override
	@Transactional(rollbackFor = OrderFlowException.class)
	public Order close(Order order) throws OrderFlowException{
		if (order.getParentId()>0&&order.getStatus()!=Order.Status.Actived.value) {
			throw new OrderFlowException(String.format("sub order status: %d %d", order.getId(), order.getStatus()));
		}
		if (order.getStatus()==Order.Status.Closed.value||order.getStatus()==Order.Status.Completed.value) {
			throw new OrderFlowException(String.format("order can't close: %d %d", order.getId(), order.getStatus()));
		}
		
		if (!orderDAO.updateStatus(order, Order.Status.Closed.value)) {
			throw new OrderFlowException(String.format("Failed to update status to Closed on main order: %d %d", order.getId(), order.getStatus()));
		}
		order.setStatus(Order.Status.Closed.value);
		
		if (order.getParentId()==0) {
			Order subOrderExample = new Order();
			subOrderExample.setParentId(order.getId());
			List<Order> subOrders = orderDAO.queryOrders(subOrderExample, 0, 1000);
			for(Order subOrder : subOrders) {
				if (subOrder.getStatus()==Order.Status.Actived.value||subOrder.getStatus()==Order.Status.Refund.value) {
					if (!orderDAO.updateStatus(subOrder, Order.Status.Closed.value)) {
						throw new OrderFlowException(String.format("Failed to update status to Closed on main order: %d %d", subOrder.getId(), subOrder.getStatus()));
					}
					subOrder.setStatus(Order.Status.Closed.value);
				}
			}
		}
		return order;
	}
	
	@Override
	@Transactional(rollbackFor = OrderFlowException.class)
	public Order complete(Order order) throws OrderFlowException{
		if (order.getParentId()>0) {
			throw new OrderFlowException(String.format("sub order can't complete: %d", order.getId()));
		}
		if (order.getStatus()==Order.Status.Closed.value||order.getStatus()==Order.Status.Completed.value) {
			throw new OrderFlowException(String.format("order can't compelete: %d %d", order.getId(), order.getStatus()));
		}
		
		if (!orderDAO.updateStatus(order, Order.Status.Completed.value)) {
			throw new OrderFlowException(String.format("Failed to update status to Completed on main order: %d %d", order.getId(), order.getStatus()));
		}
		order.setStatus(Order.Status.Completed.value);
		
		Order subOrderExample = new Order();
		subOrderExample.setParentId(order.getId());
		List<Order> subOrders = orderDAO.queryOrders(subOrderExample, 0, 1000);
		for(Order subOrder : subOrders) {
			if (subOrder.getStatus()==Order.Status.Actived.value) {
				if (!orderDAO.updateStatus(subOrder, Order.Status.Closed.value)) {
					throw new OrderFlowException(String.format("Failed to update status to Closed on main order: %d %d", subOrder.getId(), subOrder.getStatus()));
				}
				subOrder.setStatus(Order.Status.Closed.value);
			}else {
				if (!orderDAO.updateStatus(subOrder, Order.Status.Completed.value)) {
					throw new OrderFlowException(String.format("Failed to update status to Completed on main order: %d %d", subOrder.getId(), subOrder.getStatus()));
				}
				subOrder.setStatus(Order.Status.Completed.value);
			}
		}
		return order;
	}
}
