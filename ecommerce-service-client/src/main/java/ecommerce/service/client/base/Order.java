package ecommerce.service.client.base;

public class Order implements java.io.Serializable {

	private static final long serialVersionUID = 1821901254631880819L;

	private Long id;

	private Long parentId;

	private Long userId;
	
	private Long productId;

	private Long receiptAmount;
	
	private Long paidAmount;

	private Integer status;
	
	private Integer createTime;

	private String externalOrderId;
	
	public enum Status {
		Actived(0), Paid(1), Refund(2), Completed(3), Closed(-1);
		public int value=0;
		private Status(int s){this.value=s;}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Integer createTime) {
		this.createTime = createTime;
	}

	public Long getPaidAmount() {
		return paidAmount;
	}

	public void setPaidAmount(Long paidAmount) {
		this.paidAmount = paidAmount;
	}

	public Long getReceiptAmount() {
		return receiptAmount;
	}

	public void setReceiptAmount(Long receiptAmount) {
		this.receiptAmount = receiptAmount;
	}

	public String getExternalOrderId() {
		return externalOrderId;
	}

	public void setExternalOrderId(String externalOrderId) {
		this.externalOrderId = externalOrderId;
	}

}
