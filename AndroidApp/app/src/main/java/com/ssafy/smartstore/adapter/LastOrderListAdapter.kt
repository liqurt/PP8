package com.ssafy.smartstore.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ssafy.smartstore.R
import com.ssafy.smartstore.config.ApplicationClass
import com.ssafy.smartstore.dto.OrderDetail
import com.ssafy.smartstore.response.LastOrderResponse
import com.ssafy.smartstore.response.OrderDetailResponse
import com.ssafy.smartstore.util.CommonUtils


class LastOrderListAdapter(val context: Context, val orderDetail:List<LastOrderResponse>) :RecyclerView.Adapter<LastOrderListAdapter.LastOrderListHolder>(){

    inner class LastOrderListHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val menuImage = itemView.findViewById<ImageView>(R.id.menuImage)
        val textShoppingMenuName = itemView.findViewById<TextView>(R.id.textShoppingMenuName)
        val textShoppingMenuMoney = itemView.findViewById<TextView>(R.id.textShoppingMenuMoney)
        val textShoppingMenuCount = itemView.findViewById<TextView>(R.id.textShoppingMenuCount)
        val textShoppingMenuMoneyAll = itemView.findViewById<TextView>(R.id.textShoppingMenuMoneyAll)
        val tvMenuCustom = itemView.findViewById<TextView>(R.id.tv_custom_detail)

        fun bindInfo(data:LastOrderResponse){
            var type = if(data.type == "coffee") "잔" else "개"

            Glide.with(itemView)
                .load("${ApplicationClass.MENU_IMGS_URL}${data.img}")
                .into(menuImage)

            textShoppingMenuName.text = data.productName
            textShoppingMenuMoney.text = CommonUtils.makeComma(data.unitPrice)
            textShoppingMenuCount.text = "${data.orderCnt} ${type}"
            textShoppingMenuMoneyAll.text = CommonUtils.makeComma(data.unitPrice * data.orderCnt)
            tvMenuCustom.text = data.p_detail
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LastOrderListAdapter.LastOrderListHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_order_detail_list, parent, false)
        return LastOrderListHolder(view)
    }

    override fun onBindViewHolder(holder: LastOrderListAdapter.LastOrderListHolder, position: Int) {
        holder.bindInfo(orderDetail[position])
    }

    override fun getItemCount(): Int {
        return orderDetail.size
    }
}

