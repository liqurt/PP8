package com.ssafy.cafe.model.service;

import java.util.List;
import java.util.Map;

import com.ssafy.cafe.model.dto.Coupon;

public interface CouponService {
	/**
     * Coupon를 등록한다.
     * @param coupon
     */
	void insertCoupon(Coupon coupon);
	
	/**
     * Coupon를 수정한다.
     * @param coupon
     */
	void updateCoupon(Coupon coupon);
	
	/**
     * id로 쿠폰 찾기
     * @param couponId
     * @return
     */
//	Coupon selectByCouponId (Integer couponId);
	
	/**
     * userId로 쿠폰 찾기
     * @param userId
     * @return
     */
	List<Coupon> selectCouponByUserId (String userId);
	
	void deleteCouponById(Integer id);
}
