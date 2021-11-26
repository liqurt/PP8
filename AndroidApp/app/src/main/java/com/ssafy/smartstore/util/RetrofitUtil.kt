package com.ssafy.smartstore.util

import com.ssafy.smartstore.api.*
import com.ssafy.smartstore.config.ApplicationClass

class RetrofitUtil {
    companion object{
        val commentService = ApplicationClass.retrofit.create(CommentApi::class.java)
        val orderService = ApplicationClass.retrofit.create(OrderApi::class.java)
        val productService = ApplicationClass.retrofit.create(ProductApi::class.java)
        val userService = ApplicationClass.retrofit.create(UserApi::class.java)
        val couponService = ApplicationClass.retrofit.create(CouponApi::class.java)
    }
}