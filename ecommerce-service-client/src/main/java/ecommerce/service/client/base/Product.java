package ecommerce.service.client.base;

public class Product implements java.io.Serializable {

	private static final long serialVersionUID = 1821901254631880819L;

	private Long id;
	
	private String name;
	
	private Long price;

	private Long quantity;

	private Integer status;
	
	private Integer categoryId;

	public enum Status {
		Actived(0), Invalid(-1);
		public int value=0;
		private Status(int s){this.value=s;}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getPrice() {
		return price;
	}

	public void setPrice(Long price) {
		this.price = price;
	}

	public Long getQuantity() {
		return quantity;
	}

	public void setQuantity(Long quantity) {
		this.quantity = quantity;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Integer categoryId) {
		this.categoryId = categoryId;
	}

}
