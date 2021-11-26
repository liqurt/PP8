package com.ssafy.cafe.model.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.cafe.model.dao.CouponDao;
import com.ssafy.cafe.model.dto.Coupon;

/**
 * @author Seung Il Yoon
 * @since 2021. 11. 23
 */
@Service
public class CouponServiceImpl implements CouponService {

	@Autowired
	CouponDao cpDao;
	
	@Override
	@Transactional
	public void insertCoupon(Coupon coupon) {
		cpDao.insertCoupon(coupon);
	}

//	@Override
//	@Transactional
//	public void deleteCoupon(Integer couponId) {
//		cpDao.delete(couponId);
//	}

	@Override
	@Transactional
	public void updateCoupon(Coupon coupon) {
		cpDao.updateCoupon(coupon);
	}

//	@Override
//	public Coupon selectByCouponId(Integer couponId) {
//		return cpDao.selectByCouponId(couponId);
//	}

	@Override
	public List<Coupon> selectCouponByUserId(String userId) {
		return cpDao.selectCouponByUserId(userId);
	}

	@Override
	@Transactional
	public void deleteCouponById(Integer id) {
		cpDao.deleteCouponById(id);
	}

}
