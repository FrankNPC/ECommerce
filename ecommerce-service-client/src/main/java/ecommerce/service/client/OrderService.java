package ecommerce.service.client;

import java.util.List;

import ecommerce.service.client.base.Order;
import ecommerce.service.client.base.ShopCartOrder;

public interface OrderService {

	public Result<List<Order>> queryOrders(Order order, int start, int size);
	public Result<Order> getOrderById(long orderId);
	public Result<Order> getOrderByIdAndUserId(long orderId, long userId);
	
	public Result<Boolean> createOrder(ShopCartOrder shopCartOrder);
	public Result<Boolean> payment(Order order);
	public Result<Boolean> refund(Order order);
	
	public Result<Boolean> close(Order order);
	public Result<Boolean> complete(Order order);

}
