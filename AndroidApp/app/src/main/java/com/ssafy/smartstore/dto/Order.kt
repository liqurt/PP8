package com.ssafy.smartstore.dto

import java.util.*

data class Order (
    var id: Int,
    var userId: String,
    var orderTable: String,
    var orderTime: String,
    var complited: String,
    val details: ArrayList<OrderDetail> = ArrayList() ){

    var totalQnanty:Int = 0
    var totalPrice:Int = 0
    var topProductName:String = ""
    var topImg:String = ""

    constructor():this(0,"","","","")

    constructor(userId: String, orderTable: String, details: ArrayList<OrderDetail>) : this(0, userId, orderTable, "", "", details)
}
