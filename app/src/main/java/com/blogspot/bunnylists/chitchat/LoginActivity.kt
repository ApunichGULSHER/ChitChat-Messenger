package com.blogspot.bunnylists.chitchat

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.blogspot.bunnylists.chitchat.Chat.LoadingDialog
import com.blogspot.bunnylists.chitchat.SignUp.SignupActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.*
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {
    private lateinit var loadingDialog : LoadingDialog
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
        val window = window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.parseColor("#E0E0E0")

        phoneText = findViewById(R.id.loginEditTextPhone)
        createAccountButton = findViewById(R.id.loginSignupButton)
        loginButton = findViewById(R.id.loginLoginButton)
        mAuth = FirebaseAuth.getInstance()
        loadingDialog = LoadingDialog(this)


        createAccountButton.setOnClickListener {
            val intent=Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        loginButton.setOnClickListener {
            var number = phoneText.text.trim()
            if (number.isEmpty() || number.length > 10 || number.length < 10) {
                Toast.makeText(this, "Please enter a valid mobile number", Toast.LENGTH_SHORT).show()
            } else {
                number = "+91$number"
                checkUser(number)
            }
        }


        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            // This method is called when the verification is completed
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                loadingDialog.isDismiss()
                startActivity(Intent(applicationContext, FriendsList::class.java))
                finish()
                Log.d("Msg", "onVerificationCompleted Success")
            }
            // Called when verification is failed add log statement to see the exception
            override fun onVerificationFailed(e: FirebaseException) {
                loadingDialog.isDismiss()
                Toast.makeText(this@LoginActivity, "Server error, please try later", Toast.LENGTH_SHORT).show()
            }
            // On code is sent by the firebase this method is called
            // in here we start a new activity where user can enter the OTP
            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                loadingDialog.isDismiss()
                storedVerificationId = verificationId
                resendToken = token

                // Start a new activity using intent
                // also send the storedVerificationId using intent
                // we will use this id to send the otp back to firebase
//                Toast.makeText(this@LoginActivity, "code sent", Toast.LENGTH_SHORT).show()
                val intent = Intent(applicationContext, LoginOtpActivity::class.java)
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
                    loadingDialog.startLoading()
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