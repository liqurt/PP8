package com.ssafy.smartstore.response

import com.google.gson.annotations.SerializedName
import java.util.*

// o_id 기준으로 분류하고, img는 그 중 하나로 사용하기
data class LastOrderResponse(
    @SerializedName("img") val img: String,
    @SerializedName("quantity") var orderCnt: Int,
    @SerializedName("user_id") val userId: String,
    @SerializedName("o_id") val orderId: Int,
    @SerializedName("product_id") val productId: Int,
    @SerializedName("p_detail") val p_detail: String,
    @SerializedName("name") val productName: String,
    @SerializedName("order_time") val orderDate: Date,
    @SerializedName("totalprice") val totalPrice: Int,
    @SerializedName("unitprice") val unitPrice: Int,
    @SerializedName("stamp") val stamp: Int,
    @SerializedName("order_table") val orderTable: String,
    @SerializedName("type") val type: String,

)