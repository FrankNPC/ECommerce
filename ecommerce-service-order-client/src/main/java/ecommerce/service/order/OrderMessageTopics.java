package ecommerce.service.order;

public enum OrderMessageTopics {
	ShopCartOrder("order.shortcart", 0),
	RetryShopCartOrder("order.shortcart.retry", 30000),
	OrderClose("order.close", 30*60*1000),
	OrderComplete("order.complete", 14*24*3600*1000),
	PaymentOrder("order.payment", 0),
	RefundOrder("order.refund", 0);
	public String value="";
	public int interval=0;
	private OrderMessageTopics(String s, int interval){this.value=s;this.interval=interval;}
}
