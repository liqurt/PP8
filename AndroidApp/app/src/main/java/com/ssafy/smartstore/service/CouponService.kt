package com.ssafy.smartstore.service

import android.util.Log
import com.ssafy.smartstore.dto.Coupon
import com.ssafy.smartstore.util.RetrofitCallback
import com.ssafy.smartstore.util.RetrofitUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "싸피"

class CouponService {
    fun insertCoupon(coupon : Coupon, callback: RetrofitCallback<Boolean>){
        val request : Call<Boolean> = RetrofitUtil.couponService.insertCoupon(coupon)
        request.enqueue(object : Callback<Boolean>{
            override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                Log.d(TAG, "insertCoupon-onResponse: ")
                val res = response.body()
                if(response.code() == 200){
                    if (res != null) {
                        callback.onSuccess(response.code(), res)
                    }
                } else {
                    callback.onFailure(response.code())
                }
            }

            override fun onFailure(call: Call<Boolean>, t: Throwable) {
                Log.d(TAG, "insertCoupon-onFailure: ")
            }

        })
    }

    fun updateCoupon(coupon : Coupon){
        val request : Call<Boolean> = RetrofitUtil.couponService.updateCoupon(coupon)
        request.enqueue(object : Callback<Boolean>{
            override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                Log.d(TAG, "updateCoupon-onResponse: ${response}")
            }
            override fun onFailure(call: Call<Boolean>, t: Throwable) {
                Log.d(TAG, "updateCoupon-onFailure: ")
            }
        })
    }

    fun selectCouponByUserId(userId : String, callback: RetrofitCallback<List<Coupon>>){
        val request : Call<List<Coupon>> = RetrofitUtil.couponService.selectCouponByUserId(userId)
        request.enqueue(object : Callback<List<Coupon>>{
            override fun onResponse(call: Call<List<Coupon>>, response: Response<List<Coupon>>) {
                Log.d(TAG, "selectCouponByUserId-onResponse: ")
                Log.d(TAG, "onResponse: $response")
                val res = response.body()
                if(response.code() == 200){
                    if (res != null) {
                        callback.onSuccess(response.code(), res)
                    }
                } else {
                    callback.onFailure(response.code())
                }
            }

            override fun onFailure(call: Call<List<Coupon>>, t: Throwable) {
                Log.d(TAG, "selectCouponByUserId-onFailure: ")
            }
        })
    }

    fun deleteCouponById(id : Int){
        val request : Call<Boolean> = RetrofitUtil.couponService.deleteCouponById(id)
        request.enqueue(object : Callback<Boolean>{
            override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                Log.d(TAG, "deleteCouponById-onResponse: $response")
            }

            override fun onFailure(call: Call<Boolean>, t: Throwable) {
            }

        })
    }
}