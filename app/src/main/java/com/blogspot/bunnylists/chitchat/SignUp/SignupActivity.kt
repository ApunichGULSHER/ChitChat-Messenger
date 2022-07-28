package com.blogspot.bunnylists.chitchat.SignUp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.blogspot.bunnylists.chitchat.R
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.database.*
import java.util.concurrent.TimeUnit

class SignupActivity : AppCompatActivity() {
    private lateinit var editTextPhone : EditText
    private lateinit var signupButton : Button
    lateinit var mAuth : FirebaseAuth
    lateinit var mDBRef : DatabaseReference
    lateinit var storedVerificationId: String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        Toast.makeText(this, "Entered Signup Activity", Toast.LENGTH_SHORT).show()
        setContentView(R.layout.activity_signup)
        editTextPhone=findViewById(R.id.signupEditTextPhone)
        signupButton=findViewById(R.id.signupSignupButton)

        mAuth = FirebaseAuth.getInstance()


        // Callback function for Phone Auth
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            // This method is called when the verification is completed
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {

            }

            // Called when verification is failed add log statement to see the exception
            override fun onVerificationFailed(e: FirebaseException) {
                Toast.makeText(this@SignupActivity, "Network issue, please try later", Toast.LENGTH_SHORT).show()
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
//                Toast.makeText(this@SignupActivity, "onCodeSent", Toast.LENGTH_SHORT).show()
                val intent = Intent(applicationContext, SignupOTPactivity::class.java)
                intent.putExtra("storedVerificationId", storedVerificationId)
                startActivity(intent)
                finish()
            }
        }
        signupButton.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser(){
        var number = editTextPhone.text.toString()
        if(number.isEmpty())
            Toast.makeText(this, "Fill every detail", Toast.LENGTH_SHORT).show()
        else{
            mDBRef = FirebaseDatabase.getInstance().reference
            mDBRef.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    number= "+91$number"
                    if(snapshot.child("Users").hasChild(number)){
                        Toast.makeText(this@SignupActivity, "Number already Registered", Toast.LENGTH_SHORT).show()
                    }
                    else
                        sendVerificationCode(number)
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
        }

    }
    private fun sendVerificationCode(number : String){
        val options=PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(number)
            .setTimeout(60L,TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        Log.d("MSG","Auth started")
    }
}