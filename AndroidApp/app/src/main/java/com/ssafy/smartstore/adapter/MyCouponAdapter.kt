package com.ssafy.smartstore.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.smartstore.R
import com.ssafy.smartstore.dto.Coupon
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "싸피"

class MyCouponAdapter(var list: List<Coupon>) : RecyclerView.Adapter<MyCouponAdapter.MyCouponHolder>() {
    inner class MyCouponHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val discountRate : TextView = itemView.findViewById(R.id.discountRate)
        val publishDate : TextView = itemView.findViewById(R.id.publishDate)
        val expirationDate : TextView = itemView.findViewById(R.id.expirationDate)
        val couponApply = itemView.findViewById<ConstraintLayout>(R.id.couponApply)
        fun bindInfo(position: Int, data: Coupon) {
            if(data.isExpired == "F" && data.isUsed == "F"){
                discountRate.text = "${data.discountRate}% 할인"
                publishDate.text = "발급일 : ${dateToStringKorean(data.publishDate!!)}"
                expirationDate.text = "만료일 : ${dateToStringKorean(data.expirationDate!!)}"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyCouponHolder {
        Log.d(TAG, "onCreateViewHolder: $list")
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_my_coupon, parent, false)
        return MyCouponHolder(view)
    }

    override fun onBindViewHolder(holder: MyCouponHolder, position: Int) {
        holder.apply {
            bindInfo(position, list[position])
            if(onItemClickListener != null){
                itemView.setOnClickListener {
                    onItemClickListener?.onClick(it, position)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun dateToStringKorean(date : Date) : String{
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN)
        val koreanTimeZone = TimeZone.getTimeZone("Asia/Seoul")
        dateFormat.timeZone = koreanTimeZone
        return dateFormat.format(date)
    }

    interface OnItemClickListener{
        fun onClick(itemView : View, position: Int){}
    }
    var onItemClickListener : OnItemClickListener? = null

}