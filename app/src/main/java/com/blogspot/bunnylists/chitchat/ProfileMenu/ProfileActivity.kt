package com.blogspot.bunnylists.chitchat.ProfileMenu

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.blogspot.bunnylists.chitchat.LoginActivity
import com.blogspot.bunnylists.chitchat.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

class ProfileActivity : AppCompatActivity() {
    private lateinit var accountTab : LinearLayout
    private lateinit var referTab : LinearLayout
    private lateinit var logoutTab : LinearLayout
    private lateinit var mAuth : FirebaseAuth
    private lateinit var mDbRef : DatabaseReference
    private lateinit var nameTV : TextView
    private lateinit var aboutTV : TextView
    private lateinit var profilePic : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        accountTab = findViewById(R.id.accounttab)
        referTab = findViewById(R.id.refertab)
        logoutTab = findViewById(R.id.logouttab)
        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().reference
        nameTV = findViewById(R.id.Name)
        aboutTV = findViewById(R.id.About)
        profilePic = findViewById(R.id.userImage)


        val name = intent.getStringExtra("userName").toString()
        val profileUrl = intent.getStringExtra("profileUrl").toString()
        val about = intent.getStringExtra("userAbout").toString()

        nameTV.text = name
        aboutTV.text = about
        Picasso.get().load(profileUrl).into(profilePic)

        logoutTab.setOnClickListener {
            mAuth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        accountTab.setOnClickListener {
            val intent = Intent(this, AccountActivity::class.java)
            startActivity(intent)
        }

        referTab.setOnClickListener {
            TODO()
        }
    }
}