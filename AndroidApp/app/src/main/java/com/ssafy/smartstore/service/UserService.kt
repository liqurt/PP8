package com.ssafy.smartstore.service

import android.content.Context
import android.util.Log
import android.view.Menu
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.ssafy.smartstore.config.ApplicationClass
import com.ssafy.smartstore.dto.*
import com.ssafy.smartstore.response.MenuDetailWithCommentResponse
import com.ssafy.smartstore.util.RetrofitCallback
import com.ssafy.smartstore.util.RetrofitUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*


private const val TAG = "LoginService_싸피"
class UserService {

    fun login(user:User, callback: RetrofitCallback<User>)  {
        RetrofitUtil.userService.login(user).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                val res = response.body()
                if(response.code() == 200){
                    if (res != null) {
                        callback.onSuccess(response.code(), res)
                    }
                } else {
                    callback.onFailure(response.code())
                }
            }
            override fun onFailure(call: Call<User>, t: Throwable) {
                callback.onError(t)
            }
        })
    }

    fun getMyComment(user:User, callback: RetrofitCallback<HashMap<String,Any>>) {
        RetrofitUtil.userService.getInfo(user)
            .enqueue(object : Callback<HashMap<String, Any>> {
                override fun onResponse(
                    call: Call<HashMap<String, Any>>,
                    response: Response<HashMap<String, Any>>
                ) {
                    val res = response.body()
                    if(response.code() == 200){
                        if (res != null) {
                            callback.onSuccess(response.code(), res)
                        }
                    } else {
                        callback.onFailure(response.code())
                    }
                }
                override fun onFailure(call: Call<HashMap<String, Any>>, t: Throwable) {
                    Log.d(TAG, "onFailure : sad...")
                }
            })
    }
}