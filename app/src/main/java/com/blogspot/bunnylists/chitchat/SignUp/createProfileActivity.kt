package com.blogspot.bunnylists.chitchat.SignUp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.blogspot.bunnylists.chitchat.FriendsList
import com.blogspot.bunnylists.chitchat.R
import com.blogspot.bunnylists.chitchat.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class createProfileActivity : AppCompatActivity() {
    private lateinit var editTextName : EditText
    private lateinit var continueButton : Button
    private lateinit var mAuth : FirebaseAuth
    private lateinit var mDBRef : DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_profile)

        editTextName = findViewById(R.id.UserName)
        continueButton = findViewById(R.id.ContinueButton)

        continueButton.setOnClickListener {
            val name  = editTextName.text.toString()
            if(name.isEmpty())
                Toast.makeText(this, "Please Enter Name", Toast.LENGTH_SHORT).show()
            else{
                val profilePicUrl = "https://images.pexels.com/photos/1704488/pexels-photo-1704488.jpeg?auto=compress&cs=tinysrgb&w=600"
                mAuth = FirebaseAuth.getInstance()
                val phoneNumber = mAuth.currentUser?.phoneNumber!!
                mDBRef = FirebaseDatabase.getInstance().reference
                mDBRef.child("Users").child(phoneNumber).setValue(User(name, phoneNumber, profilePicUrl))
                startActivity(Intent(this, FriendsList::class.java))
                finish()
            }
        }

    }
}