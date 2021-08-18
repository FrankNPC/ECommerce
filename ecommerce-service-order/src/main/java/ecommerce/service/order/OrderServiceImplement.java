package ecommerce.service.order;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ecommerce.service.KeyIdentifies;
import ecommerce.service.client.OrderService;
import ecommerce.service.client.ProductService;
import ecommerce.service.client.Result;
import ecommerce.service.client.UserService;
import ecommerce.service.client.base.Order;
import ecommerce.service.client.base.Product;
import ecommerce.service.client.base.ShopCartOrder;
import ecommerce.service.client.base.User;
import ecommerce.service.order.client.OrderFlowException;
import ecommerce.service.order.client.OrderFlowService;
import ecommerce.service.order.dao.OrderDAOFromMySQL;

@Service
public class OrderServiceImplement implements OrderService{
	
    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImplement.class);

    @Autowired
    private OrderDAOFromMySQL orderDAO;
    
//    @Autowired
//    private OrderDAOByCassandra orderDAO;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private KafkaTemplate<String, String> stringKafkaTemplate;

    @Resource
    private OrderFlowService orderFlowService;

    @Resource
    private ProductService productService;

    @Resource
	private UserService userService;
    
	@Override
	public Result<List<Order>> queryOrders(Order order, int start, int size) {
		Result<List<Order>> result = new Result<List<Order>>();
		start = start<0?0:start;
		size = size<10||size>100?100:size;
		result.setData(orderDAO.queryOrders(order, start, size));
		result.setCode(result.getData()==null||result.getData().isEmpty()?-1:0);
		return result;
	}
	@Override
	public Result<Order> getOrderById(long orderId) {
		Result<Order> result = new Result<Order>();
		result.setData(orderDAO.getOrderById(orderId));
		result.setCode(result.getData()==null?Result.Code.Error.value:Result.Code.OK.value);
		return result;
	}
	@Override
	public Result<Order> getOrderByIdAndUserId(long orderId, long userId) {
		Result<Order> result = new Result<Order>();
		result.setData(orderDAO.getOrderByIdAndUserId(orderId, userId));
		result.setCode(result.getData()==null?Result.Code.Error.value:Result.Code.OK.value);
		return result;
	}
	
	@Override
	public Result<Boolean> createOrder(ShopCartOrder shopCartOrder){
		Result<Boolean> result = new Result<Boolean>();
		result.setCode(Result.Code.Error.value);
		
		if (shopCartOrder.getProductIds().size()!=shopCartOrder.getQuantities().size()||shopCartOrder.getProductIds().size()>100) {
			return result;
		}
		Boolean existed = stringRedisTemplate.opsForSet().isMember(KeyIdentifies.ShopCartOrder.value, shopCartOrder.getToken());
		if (existed==null||existed) {
			return result;
		}
    	Result<User> userResult = userService.getUserById(shopCartOrder.getUserId());
    	if (userResult==null||userResult.getCode()!=Result.Code.OK.value) {
			return result;
    	}

		Result<List<Long>> quantityResult = productService.queryQuantity(shopCartOrder.getProductIds());
		for(int i=0; i<shopCartOrder.getProductIds().size(); i++) {
			if (quantityResult.getData().get(i)==null||shopCartOrder.getQuantities().get(i)<1||shopCartOrder.getQuantities().get(i)>quantityResult.getData().get(i)) {
				return result;
			}

			if (quantityResult==null||quantityResult.getCode()!=Result.Code.OK.value) {
				result.setMessage(String.format("product not exist: %d %s", shopCartOrder.getProductIds().get(i)));
				return result;
			}
			
			Result<Product> productResult = productService.getProductById(shopCartOrder.getProductIds().get(i));
			if (productResult==null||productResult.getCode()!=Result.Code.OK.value) {
				result.setMessage(String.format("product not exist: %d %s", shopCartOrder.getProductIds().get(i)));
				return result;
			}
			
			if (quantityResult.getData().get(i)<shopCartOrder.getQuantities().get(i)) {
				result.setMessage(String.format("quantity not available: %d %d %d",
						shopCartOrder.getProductIds().get(i),
						shopCartOrder.getQuantities().get(i),
						quantityResult.getData()
						));
				return result;
			}
		}

		shopCartOrder.setCreateTime(System.currentTimeMillis());
		try {
			stringRedisTemplate.opsForSet().add(KeyIdentifies.ShopCartOrder.value, shopCartOrder.getToken());
			stringKafkaTemplate.send(OrderMessageTopics.ShopCartOrder.value, new ObjectMapper().writeValueAsString(shopCartOrder));
			result.setData(true);
		} catch (JsonProcessingException e) {
			logger.error(e.getMessage());
		}
		result.setCode(result.getData()==null?Result.Code.Error.value:Result.Code.OK.value);
		return result;
	}
	
	@Override
	public Result<Boolean> payment(Order order) {
		Result<Boolean> result = new Result<Boolean>();
		result.setCode(Result.Code.Error.value);
		if (order.getParentId()>0) {
			Order parentOrder = orderDAO.getOrderById(order.getParentId());
			if (parentOrder==null||parentOrder.getStatus()!=Order.Status.Actived.value) {
				return result;
			}
		}

		Order getOrder = orderDAO.getOrderById(order.getId());
		if (getOrder==null||getOrder.getStatus()!=Order.Status.Actived.value) {
			return result;
		}

		try {
			stringKafkaTemplate.send(OrderMessageTopics.PaymentOrder.value, new ObjectMapper().writeValueAsString(order));
			result.setData(true);
		} catch (JsonProcessingException e) {
			logger.error(e.getMessage());
		}
		result.setCode(result.getData()==null?Result.Code.Error.value:Result.Code.OK.value);
		return result;
	}
	
	@Override
	public Result<Boolean> refund(Order order) {
		Result<Boolean> result = new Result<Boolean>();
		result.setCode(Result.Code.Error.value);
		if (order.getParentId()>0) {
			Order parentOrder = orderDAO.getOrderById(order.getParentId());
			if (parentOrder==null||parentOrder.getStatus()!=Order.Status.Paid.value) {
				return result;
			}
		}

		Order getOrder = orderDAO.getOrderById(order.getId());
		if (getOrder==null||getOrder.getStatus()!=Order.Status.Paid.value) {
			return result;
		}

		try {
			stringKafkaTemplate.send(OrderMessageTopics.RefundOrder.value, new ObjectMapper().writeValueAsString(order));
			result.setData(true);
		} catch (JsonProcessingException e) {
			logger.error(e.getMessage());
		}
		result.setCode(result.getData()==null?Result.Code.Error.value:Result.Code.OK.value);
		return result;
	}
	
	@Override
	public Result<Boolean> close(Order order) {
		Result<Boolean> result = new Result<Boolean>();
		try {
			orderFlowService.close(order);
			result.setData(true);
		} catch (OrderFlowException e) {
			logger.error(e.getMessage());
		}
		result.setCode(result.getData()==null?Result.Code.Error.value:Result.Code.OK.value);
		return result;
	}
	
	@Override
	public Result<Boolean> complete(Order order) {
		Result<Boolean> result = new Result<Boolean>();
		try {
			orderFlowService.complete(order);
			result.setData(true);
		} catch (OrderFlowException e) {
			logger.error(e.getMessage());
		}
		result.setCode(result.getData()==null?Result.Code.Error.value:Result.Code.OK.value);
		return result;
	}

}
