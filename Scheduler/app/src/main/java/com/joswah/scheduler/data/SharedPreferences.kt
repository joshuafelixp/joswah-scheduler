package com.joswah.scheduler.data

import android.content.Context
import android.content.SharedPreferences

class SharedPreferences (context: Context){
    private val login = "login"
    private val sf = "MyPreferences"
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(sf, Context.MODE_PRIVATE)

    fun setStatusLogin(status : Boolean){
        sharedPreferences.edit().putBoolean(login,status).apply()
    }

    fun getStatusLogin():Boolean{
        return sharedPreferences.getBoolean(login,false)
    }
    fun clearData(){
        sharedPreferences.edit().clear().apply()
    }
}