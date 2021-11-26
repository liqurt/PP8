package com.ssafy.smartstore.dto

data class OrderDetail (
    var id: Int,
    var orderId: Int,
    var productId: Int,
    var quantity: Int,
    var p_detail: String) {

    var unitPrice:Int = 0
    var img:String = ""
    var productName:String = ""
    var productType:String = ""

    constructor(productId: Int, quantity: Int, p_detail: String) :this(0, 0, productId, quantity, p_detail)
    constructor():this(0,0, "")

}
