package ecommerce.service;

public enum KeyIdentifies {
	OrderId("ecommerce.order.id",0), 
	SubOrderId("ecommerce.order.sub.id",0), 
	ProductId("ecommerce.product.id",0), 
	UserId("ecommerce.user.id",0), 
	TokenId("ecommerce.token.id",0),
	DelayShopCartOrderMessage("ecommerce.message.delay.shopcartorder",0), 
	DelayCloseMessage("ecommerce.message.delay.order.close",0), 
	DelayCompleteMessage("ecommerce.message.delay.order.complete",0), 
	ShopCartOrder("ecommerce.shopcartorder.token",0),
	ProductQuantityKey("ecommerce.product.quantity", 0),
	ProductQuantitySync("ecommerce.product.quantity.sync", 0),
	ProductQuantityLock("ecommerce.product.quantity.lock_%d", 10000),
	TokenKey("token_%s", 600),
	ProductKey("ecommerce.product.id_%d", 7*24*3600);
	public long interval = 0;
	public String value;
	private KeyIdentifies(String s, long interval){this.value=s;this.interval=interval;}
}
