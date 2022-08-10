package com.blogspot.bunnylists.chitchat

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler().postDelayed({
            val pref: SharedPreferences = getSharedPreferences("login",MODE_PRIVATE)
            val check=pref.getBoolean("LogedIn", false)
            if (check){
                val intent= Intent(this,FriendsList::class.java)
                startActivity(intent)
                finish()
            }else{
                val intent= Intent(this,LoginActivity::class.java)
                startActivity(intent)
                finish()
            }

        }, 1300)
    }
}