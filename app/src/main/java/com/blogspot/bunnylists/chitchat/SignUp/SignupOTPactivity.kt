package com.blogspot.bunnylists.chitchat.SignUp

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.*
import com.blogspot.bunnylists.chitchat.R
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignupOTPactivity : AppCompatActivity() {
    private lateinit var otpEditText: EditText
    private lateinit var otpButton: Button
    private lateinit var mAuth : FirebaseAuth
    private lateinit var mDB : FirebaseDatabase
    private lateinit var mDBRef : DatabaseReference
    private lateinit var fUser : FirebaseUser
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signupotpactivity)
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
                    startActivity(Intent(this, createProfileActivity::class.java))
                    finish()
                }
                else {
                    Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show()
                }
            }
    }
}