package com.ssafy.cafe.controller.rest;

import java.util.List;
import java.util.Map;

import org.mybatis.logging.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ssafy.cafe.model.dto.Coupon;
import com.ssafy.cafe.model.service.CouponService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/rest/coupon")
@CrossOrigin("*")
public class CouponRestController {
	
	private static final org.mybatis.logging.Logger logger = LoggerFactory.getLogger(CouponRestController.class);
	
	@Autowired
    CouponService couponService;
    
    @PostMapping
    @Transactional
    @ApiOperation(value="coupon 생성", response = Boolean.class)
    public Boolean insertCoupon(@RequestBody Coupon coupon) {
    	System.out.println(coupon);
        couponService.insertCoupon(coupon);
        return true;
    }
    
    @PutMapping
    @Transactional
    @ApiOperation(value="coupon 수정", response = Boolean.class)
    public Boolean updateCoupon(@RequestBody Coupon coupon) {
        couponService.updateCoupon(coupon);
        return true;
    }
    
//    @GetMapping("/{id}")
//    @Transactional
//    @ApiOperation(value="id로 쿠폰 확인", response = Coupon.class)
//    public Boolean selectByCouponId(@PathVariable Integer id) {
//        couponService.selectByCouponId(id);
//        return true;
//    }
    
    @GetMapping("/{userId}")
    @Transactional
    @ApiOperation(value="userId로 쿠폰 확인", response = List.class)
    public List<Coupon> selectCouponByUserId(@PathVariable String userId) {
        return couponService.selectCouponByUserId(userId);
    }
    
    @DeleteMapping("/{id}")
    @Transactional
    @ApiOperation(value="userId로 쿠폰 삭제", response = Boolean.class)
    public Boolean deleteCouponById(@PathVariable Integer id) {
       couponService.deleteCouponById(id);
       return true;
    }
}
