package com.ssafy.smartstore.api

import com.ssafy.smartstore.dto.Coupon
import retrofit2.Call
import retrofit2.http.*

interface CouponApi {

    @POST("/rest/coupon")
    fun insertCoupon(@Body coupon : Coupon) : Call<Boolean>

    @PUT("/rest/coupon")
    fun updateCoupon(@Body coupon : Coupon) : Call<Boolean>

    @GET("/rest/coupon/{userId}")
    fun selectCouponByUserId(@Path("userId") Id: String) : Call<List<Coupon>>

    @DELETE("/rest/coupon/{id}")
    fun deleteCouponById(@Path("id") id : Int) :Call<Boolean>
}