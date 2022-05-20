package ecommerce.service.client.base;

import java.util.List;

public class ShopCartOrder implements java.io.Serializable {

	private static final long serialVersionUID = 1821901254631880819L;

	private String token;
	
	private Long userId;
	
	private Long createTime;

	private List<Long> productIds;
	
	private List<Long> quantities;

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public List<Long> getProductIds() {
		return productIds;
	}

	public void setProductIds(List<Long> productIds) {
		this.productIds = productIds;
	}

	public List<Long> getQuantities() {
		return quantities;
	}

	public void setQuantities(List<Long> quantities) {
		this.quantities = quantities;
	}

	public Long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

}
