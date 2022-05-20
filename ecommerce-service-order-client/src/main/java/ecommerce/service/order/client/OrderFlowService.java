package ecommerce.service.order.client;

import ecommerce.service.client.base.Order;
import ecommerce.service.client.base.ShopCartOrder;

public interface OrderFlowService {

	public Order createOrder(ShopCartOrder shopCartOrder) throws OrderFlowException;
	public Order payment(Order order) throws OrderFlowException;
	public Order refund(Order order) throws OrderFlowException;
	
	public Order close(Order order) throws OrderFlowException;
	public Order complete(Order order) throws OrderFlowException;

}
