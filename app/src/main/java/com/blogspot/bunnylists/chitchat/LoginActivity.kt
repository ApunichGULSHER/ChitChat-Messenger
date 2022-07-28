package com.blogspot.bunnylists.chitchat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.blogspot.bunnylists.chitchat.SignUp.SignupActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.*
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {
    private lateinit var phoneText : EditText
    private lateinit var loginButton : Button
    private lateinit var createAccountButton: Button
    private lateinit var mAuth: FirebaseAuth
    lateinit var storedVerificationId: String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var mDBref : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
//        Toast.makeText(this, "Entered Login Activity", Toast.LENGTH_SHORT).show()
        phoneText = findViewById(R.id.loginEditTextPhone)
        createAccountButton = findViewById(R.id.loginSignupButton)
        loginButton = findViewById(R.id.loginLoginButton)
        mAuth = FirebaseAuth.getInstance()


        createAccountButton.setOnClickListener {
            val intent=Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        loginButton.setOnClickListener {
            var number = phoneText.text.trim()
            if (number.isNotEmpty()) {
                number = "+91$number"
                checkUser(number)
            } else {
                Toast.makeText(this, "Please enter Mobile number", Toast.LENGTH_SHORT).show()
            }
        }


        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            // This method is called when the verification is completed
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                startActivity(Intent(applicationContext, FriendsList::class.java))
                finish()
                Log.d("Msg", "onVerificationCompleted Success")
            }
            // Called when verification is failed add log statement to see the exception
            override fun onVerificationFailed(e: FirebaseException) {
                Log.d("MSg", "onVerificationFailed  $e")
            }
            // On code is sent by the firebase this method is called
            // in here we start a new activity where user can enter the OTP
            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d("Msg", "onCodeSent: $verificationId")
                storedVerificationId = verificationId
                resendToken = token

                // Start a new activity using intent
                // also send the storedVerificationId using intent
                // we will use this id to send the otp back to firebase
//                Toast.makeText(this@LoginActivity, "code sent", Toast.LENGTH_SHORT).show()
                val intent = Intent(applicationContext, LoginOTPactivity::class.java)
                intent.putExtra("storedVerificationId", storedVerificationId)
                startActivity(intent)
                finish()
            }
        }
    }
    private fun sendVerificationCode(number : String){
//        Toast.makeText(this, "inside sendVerificationCode", Toast.LENGTH_SHORT).show()
        val options=PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(number)
            .setTimeout(60L,TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        Log.d("MSG","Auth started")
    }

    private fun checkUser(number : String){
        mDBref = FirebaseDatabase.getInstance().reference
        mDBref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.child("Users").hasChild(number)){
                    sendVerificationCode(number)
                   }
                else
                    Toast.makeText(this@LoginActivity, "Number not registered", Toast.LENGTH_SHORT).show()

            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}