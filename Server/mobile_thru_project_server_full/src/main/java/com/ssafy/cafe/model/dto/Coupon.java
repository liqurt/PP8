package com.ssafy.cafe.model.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "CouponDTO : 쿠폰정보", description = "쿠폰의 상세한 정보를 나타냄")
public class Coupon {
	@ApiModelProperty(value = "쿠폰의 ID", example = "")
	private Integer id;
    
	@ApiModelProperty(value = "쿠폰의 할인율", example = "")
	private Integer discountRate;
	
	@ApiModelProperty(value = "발급일", example = "null")
    private Date publishDate;
    
	@ApiModelProperty(value = "만료일(발급일 + 7일)", example = "null")
    private Date expirationDate;
    
	@ApiModelProperty(value = "사용한 날짜", example = "null")
	private Date useDate;
    
	@ApiModelProperty(value = "사용했는가?", example = "F")
    private String isUsed;
   
	@ApiModelProperty(value = "만료되었는가?", example = "F")
    private String isExpired;
    
	@ApiModelProperty(value = "쿠폰의 주인의 id", example = "id 01", required = true)
    private String userId;
    
    
	public Coupon(Integer id, Integer discountRate, Date publishDate, Date expirationDate, Date useDate, String isUsed,
			String isExpired, String userId) {
		super();
		this.id = id;
		this.discountRate = discountRate;
		this.publishDate = publishDate;
		this.expirationDate = expirationDate;
		this.useDate = useDate;
		this.isUsed = isUsed;
		this.isExpired = isExpired;
		this.userId = userId;
	}
	public Coupon(Integer discountRate, Date publishDate, Date expirationDate, Date useDate, String isUsed,
			String isExpired, String userId) {
		this.discountRate = discountRate;
		this.publishDate = publishDate;
		this.expirationDate = expirationDate;
		this.useDate = useDate;
		this.isUsed = isUsed;
		this.isExpired = isExpired;
		this.userId = userId;
	}
	public Coupon() {
		
	}
	
	// Cannot deserialize value of type 'java.util.Date' from String "Dec 2, 2021 12:00:00 AM" : not a valid representation
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getDiscountRate() {
		return discountRate;
	}
	public void setDiscountRate(Integer discountRate) {
		this.discountRate = discountRate;
	}
	public Date getPublishDate() {
		return publishDate;
	}
	public void setPublishDate(Date publishDate) {
		this.publishDate = publishDate;
	}
	public Date getExpirationDate() {
		return expirationDate;
	}
	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}
	public Date getUseDate() {
		return useDate;
	}
	public void setUseDate(Date useDate) {
		this.useDate = useDate;
	}
	public String getIsUsed() {
		return isUsed;
	}
	public void setIsUsed(String isUsed) {
		this.isUsed = isUsed;
	}
	public String getIsExpired() {
		return isExpired;
	}
	public void setIsExpired(String isExpired) {
		this.isExpired = isExpired;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
}
