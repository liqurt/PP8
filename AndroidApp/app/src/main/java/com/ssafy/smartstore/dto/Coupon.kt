package com.ssafy.smartstore.dto

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.util.*

data class Coupon(
    val id: Int = -1,
    val discountRate : Int?,
    val publishDate: Date?,
    val expirationDate: Date?,
    var useDate: Date?,
    var isUsed: String,
    val isExpired: String,
    val userId: String
) {
    @RequiresApi(Build.VERSION_CODES.O)
    constructor(userId: String) : this(
        0,
        null,
        null,
        null,
        null,
        "F",
        "F",
        userId
    )
}
