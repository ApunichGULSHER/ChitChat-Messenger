package com.blogspot.bunnylists.chitchat.SignUp

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.blogspot.bunnylists.chitchat.FriendsList
import com.blogspot.bunnylists.chitchat.R
import com.blogspot.bunnylists.chitchat.User
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.util.jar.Manifest

class createProfileActivity() : AppCompatActivity(){
    private lateinit var editTextName : EditText
    private lateinit var editTextAbout : EditText
    private lateinit var continueButton : Button
    private lateinit var profilePic : ImageView
    private lateinit var mAuth : FirebaseAuth
    private lateinit var mFireStore : FirebaseStorage
    private lateinit var mDBRef : DatabaseReference
    private lateinit var mStorageRef : StorageReference
    private val STORAGE_REQ_CODE : Int= 100
    private val IMAGE_PICK_CODE : Int= 200
    private lateinit var phoneNumber : String
    private lateinit var userImageUrl : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_profile)
        val window = window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.parseColor("#FFFFFF")

        editTextName = findViewById(R.id.UserName)
        continueButton = findViewById(R.id.ContinueButton)
        editTextAbout = findViewById(R.id.UserAbout)
        profilePic = findViewById(R.id.setProfilePicIV)
        userImageUrl = "https://images.pexels.com/photos/1704488/pexels-photo-1704488.jpeg?auto=compress&cs=tinysrgb&w=600"
        mAuth = FirebaseAuth.getInstance()
        phoneNumber = mAuth.currentUser?.phoneNumber!!
        mFireStore = FirebaseStorage.getInstance()

        profilePic.setOnClickListener {
            if(checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)==
                    PackageManager.PERMISSION_DENIED){
                val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                requestPermissions(permissions, STORAGE_REQ_CODE)
            }
            else
                pickImageFromGallery()
        }
        continueButton.setOnClickListener {
            val name  = editTextName.text.toString()
            val about = editTextAbout.text.toString()
            if(name.isNullOrEmpty() || about.isNullOrEmpty())
                Toast.makeText(this, "Please Enter Name", Toast.LENGTH_SHORT).show()
            else{
                val pref : SharedPreferences = getSharedPreferences("login", MODE_PRIVATE)
                val editor : SharedPreferences.Editor = pref.edit()
                editor.putBoolean("LogedIn", true).apply()
                mDBRef = FirebaseDatabase.getInstance().reference
                mDBRef.child("Users").child(phoneNumber).setValue(User(name, phoneNumber, userImageUrl, about))
                startActivity(Intent(this, FriendsList::class.java))
                finish()
            }
        }
    }
    private fun pickImageFromGallery(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            STORAGE_REQ_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImageFromGallery()
                } else
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE){
            if(data == null || data.data == null){
                return
            }
            else {
                profilePic.setImageURI(data.data)
                val fileName = phoneNumber
                mStorageRef = mFireStore.reference.child("UserProfilePictures/$fileName")
                mStorageRef.putFile(data.data!!).addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener {
                        userImageUrl = it.toString()
                    }
                }
                    .addOnFailureListener(OnFailureListener { e->
                        Toast.makeText(this, "$e", Toast.LENGTH_SHORT).show()
                    })
            }
        }
    }
}

