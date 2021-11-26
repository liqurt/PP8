package com.ssafy.smartstore.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.os.Handler
import com.ssafy.smartstore.R
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView

import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target


class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val gif_image: ImageView = findViewById<View>(R.id.logo_ani) as ImageView
        Glide.with(this).load(R.drawable.logoani).into(gif_image)

        startLoading()
    }

    fun startLoading() {
        val handler = Handler()
        handler.postDelayed(Runnable {
            startActivity(Intent(applicationContext, LoginActivity::class.java))
            finish()
        }, 3000)
    }
}