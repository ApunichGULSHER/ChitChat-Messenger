package com.blogspot.bunnylists.chitchat

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

class LoginOtpActivity : AppCompatActivity() {
    private lateinit var otpButton : Button
    private lateinit var otpEditText : EditText
    private lateinit var mAuth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_otpactivity)
        val window = window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.parseColor("#000000")

        otpButton = findViewById(R.id.otpVerifyButton)
        otpEditText = findViewById(R.id.otpEditText)

        mAuth = FirebaseAuth.getInstance()
        val storedVerificationId = intent.getStringExtra("storedVerificationId")
        otpButton.setOnClickListener {
            val otp = otpEditText.text.trim().toString()
            if (otp.isNotEmpty()){
                val credential : PhoneAuthCredential = PhoneAuthProvider.getCredential(
                    storedVerificationId.toString(), otp)
                signInWithPhoneAuthCredential(credential)
            }
            else
                Toast.makeText(this,"Enter OTP", Toast.LENGTH_SHORT).show()
        }
    }


    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this){ task->
                if(task.isSuccessful){
                    val pref : SharedPreferences = getSharedPreferences("login", MODE_PRIVATE)
                    val editor : SharedPreferences.Editor = pref.edit()
                    editor.putBoolean("LogedIn", true).apply()
                    startActivity(Intent(this, FriendsList::class.java))
                    finish()
                }
                else {
                    Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show()
                }
            }
    }
}