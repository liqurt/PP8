package com.ssafy.cafe.model.dao;

import java.util.List;

import com.ssafy.cafe.model.dto.Coupon;

public interface CouponDao {
	int insertCoupon (Coupon coupon);

	int updateCoupon (Coupon coupon);
	
//	Coupon selectByCouponId (Integer couponId);
	
	List<Coupon> selectCouponByUserId (String userId);
	
	void deleteCouponById(Integer id);

}
