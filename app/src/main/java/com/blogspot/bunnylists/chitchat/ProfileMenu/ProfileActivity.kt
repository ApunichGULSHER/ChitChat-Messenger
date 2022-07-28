package com.blogspot.bunnylists.chitchat.ProfileMenu

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.blogspot.bunnylists.chitchat.R

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        Toast.makeText(this, "I am laughing!!", Toast.LENGTH_SHORT).show()
    }
}