package com.ssafy.smartstore.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ssafy.smartstore.R
import com.ssafy.smartstore.api.ProductApi
import com.ssafy.smartstore.config.ApplicationClass
import com.ssafy.smartstore.dto.Comment
import com.ssafy.smartstore.dto.Product
import com.ssafy.smartstore.response.MenuDetailWithCommentResponse
import com.ssafy.smartstore.util.RetrofitCallback
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "싸피"

class MyCommentAdapter(var list: ArrayList<Comment>) :
    RecyclerView.Adapter<MyCommentAdapter.MyCommentHolder>() {
    inner class MyCommentHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var img: ImageView = itemView.findViewById(R.id.rcvMyCommentItemProductImg1)
        var rat: RatingBar = itemView.findViewById(R.id.rcvMyCommentItemRating1)
        var com: TextView = itemView.findViewById(R.id.rcvMyCommentItemComment1)

        fun bindInfo(position: Int, data: Comment) {
            getProductImageString(data.productId, ProductImgCallback())
            rat.rating = data.rating
            com.text = data.comment
        }

        private fun getProductImageString(productId: Int, callback: ProductImgCallback){
            val service = ApplicationClass.retrofit.create(ProductApi::class.java)
            service.getProductWithComments(productId)
                .enqueue(object : Callback<List<MenuDetailWithCommentResponse>> {
                    override fun onResponse(
                        call: Call<List<MenuDetailWithCommentResponse>>,
                        response: Response<List<MenuDetailWithCommentResponse>>
                    ) {
                        val res = response.body()
                        if(response.code() == 200){
                            if (res != null) {
                                val menuDetail = (response.body() ?: mutableListOf()) as MutableList<MenuDetailWithCommentResponse>
                                val result = menuDetail[0].productImg
                                callback.onSuccess(response.code(), result)
                            }
                        } else {
                            callback.onFailure(response.code())
                        }
                    }

                    override fun onFailure(
                        call: Call<List<MenuDetailWithCommentResponse>>,
                        t: Throwable
                    ) {
                        Log.d(TAG, "onFailure: 통신 오류")
                    }

                })
        }

        inner class ProductImgCallback : RetrofitCallback<String> {
            override fun onError(t: Throwable) {
            }
            override fun onSuccess(code: Int, responseData: String) {
                Glide.with(itemView)
                    .load("${ApplicationClass.MENU_IMGS_URL}${responseData}")
                    .into(img)
            }
            override fun onFailure(code: Int) {
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyCommentHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_my_comment, parent, false)
        return MyCommentHolder(view)
    }

    override fun onBindViewHolder(holder: MyCommentHolder, position: Int) {
        holder.apply {
            bindInfo(position, list[position])
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}