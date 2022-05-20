package ecommerce.service;

public enum ProductMessageTopics {
	ProductQuantitySync("product.quantity.sync", 0);
	public String value="";
	public int interval=0;
	private ProductMessageTopics(String s, int interval){this.value=s;this.interval=interval;}
}
