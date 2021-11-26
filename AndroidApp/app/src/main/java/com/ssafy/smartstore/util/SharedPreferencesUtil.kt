package com.ssafy.smartstore.util

import android.content.Context
import android.content.SharedPreferences
import com.ssafy.smartstore.dto.User

class SharedPreferencesUtil (context: Context) {
    val SHARED_PREFERENCES_NAME = "smartstore_preference"
    val COOKIES_KEY_NAME = "cookies"

    var preferences: SharedPreferences =
        context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun setGradeTitle(title : String){
        val editor = preferences.edit()
        editor.putString("title", title)
        editor.apply()
    }

    fun setGradeStep(step : Int){
        val editor = preferences.edit()
        editor.putInt("step", step)
        editor.apply()
    }

    fun getGradeTitleAndStep() : HashMap<String, Any>{
        val title = preferences.getString("title", "")
        val step = preferences.getInt("step", -1)
        val map = HashMap<String, Any>()
        map["title"] = title!!
        map["step"] = step
        return map
    }

    //사용자 정보 저장
    fun addUser(user:User){
        val editor = preferences.edit()
        editor.putString("id", user.id)
        editor.putString("name", user.name)
        editor.putString("pass", user.pass)
        editor.apply()
    }

    fun getUser(): User{
        val id = preferences.getString("id", "")
        if (id != ""){
            val name = preferences.getString("name", "")
            val pass = preferences.getString("pass", "")
            return User(id!!, name!!, pass!!,0)
        }else{
            return User()
        }
    }

    fun deleteUser(){
        //preference 지우기
        val editor = preferences.edit()
        editor.clear()
        editor.apply()
    }

    fun addUserCookie(cookies: HashSet<String>) {
        val editor = preferences.edit()
        editor.putStringSet(COOKIES_KEY_NAME, cookies)
        editor.apply()
    }

    fun getUserCookie(): MutableSet<String>? {
        return preferences.getStringSet(COOKIES_KEY_NAME, HashSet())
    }

    fun deleteUserCookie() {
        preferences.edit().remove(COOKIES_KEY_NAME).apply()
    }


}