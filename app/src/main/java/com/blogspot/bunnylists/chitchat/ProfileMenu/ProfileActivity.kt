package com.blogspot.bunnylists.chitchat.ProfileMenu

import android.app.DownloadManager
import android.content.*
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.blogspot.bunnylists.chitchat.LoginActivity
import com.blogspot.bunnylists.chitchat.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import java.io.File
import java.net.URL


class ProfileActivity : AppCompatActivity() {
    private lateinit var updateTab : LinearLayout
    private lateinit var referTab : LinearLayout
    private lateinit var logoutTab : LinearLayout
    private lateinit var mAuth : FirebaseAuth
    private lateinit var mDbRef : DatabaseReference
    private lateinit var nameTV : TextView
    private lateinit var aboutTV : TextView
    private lateinit var profilePic : ImageView
    private lateinit var name : String
    private lateinit var about : String
    private lateinit var profilePicUrl : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val window = window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.parseColor("#009688")

        referTab = findViewById(R.id.refertab)
        logoutTab = findViewById(R.id.logouttab)
        updateTab = findViewById(R.id.checkupdatetab)
        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().reference
        nameTV = findViewById(R.id.Name)
        aboutTV = findViewById(R.id.About)
        profilePic = findViewById(R.id.userImage)

        name = intent.getStringExtra("userName").toString()
        about = intent.getStringExtra("userAbout").toString()
        profilePicUrl = intent.getStringExtra("profileUrl").toString()

        nameTV.text = name
        aboutTV.text = about
        Picasso.get().load(profilePicUrl).into(profilePic)

        logoutTab.setOnClickListener {
            mAuth.signOut()
            val pref : SharedPreferences = getSharedPreferences("login", MODE_PRIVATE)
            val editor : SharedPreferences.Editor = pref.edit()
            editor.putBoolean("LogedIn", false).apply()
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        referTab.setOnClickListener {
            val intent = Intent(this, InviteFriends::class.java)
            startActivity(intent)
        }
        profilePic.setOnClickListener {
                val intent = Intent(this, UpdateProfileActivity::class.java)
                intent.putExtra("name", name)
                intent.putExtra("about", about)
                intent.putExtra("profilePicUrl", profilePicUrl)
                startActivity(intent)
        }

        updateTab.setOnClickListener {
            mDbRef.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val currentVersion = applicationContext.packageManager
                        .getPackageInfo(applicationContext.packageName, 0).versionName.toString()
                    
                    val latestVersion = snapshot.child("latestVersion").value.toString()
                    var latestVersionLink = snapshot.child("AppDownloadLink").value
                    if (currentVersion < latestVersion){
                        Toast.makeText(this@ProfileActivity, "New version available, kindly download", Toast.LENGTH_SHORT).show()
                        latestVersionLink = Uri.parse(latestVersionLink.toString())
                        val intent = Intent(Intent.ACTION_VIEW, latestVersionLink)
                        startActivity(intent)
                    }
                    else if(currentVersion > latestVersion)
                        Toast.makeText(this@ProfileActivity, "Beta version installed", Toast.LENGTH_SHORT).show()
                    else
                        Toast.makeText(this@ProfileActivity, "Up to Date", Toast.LENGTH_SHORT).show()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ProfileActivity, "Try later!", Toast.LENGTH_SHORT).show()
                }

            })
        }
    }
}

