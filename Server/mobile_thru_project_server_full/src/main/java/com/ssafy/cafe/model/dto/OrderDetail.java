package com.ssafy.cafe.model.dto;


public class OrderDetail {
    private Integer id;
    private Integer orderId;
    private Integer productId;
    private Integer quantity;
    private String p_detail;
    
    public OrderDetail(Integer id, Integer orderId, Integer productId, Integer quantity, String p_detail) {
        super();
        this.id = id;
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.p_detail = p_detail;
    }
    
    public OrderDetail(Integer productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }
    
    public OrderDetail(Integer orderId, Integer productId, Integer quantity, String p_detail) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.p_detail = p_detail;
    }
    
    public OrderDetail() {}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getOrderId() {
		return orderId;
	}

	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}

	public Integer getProductId() {
		return productId;
	}

	public void setProductId(Integer productId) {
		this.productId = productId;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	
	
	public String getP_detail() {
		return p_detail;
	}

	public void setP_detail(String p_detail) {
		this.p_detail = p_detail;
	}
	

	@Override
	public String toString() {
		return "OrderDetail [id=" + id + ", orderId=" + orderId + ", productId=" + productId + ", quantity=" + quantity
				+ ", p_detail=" + p_detail + "]";
	}
    
    
}
