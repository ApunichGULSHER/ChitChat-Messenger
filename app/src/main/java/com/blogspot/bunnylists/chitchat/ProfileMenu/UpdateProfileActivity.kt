package com.blogspot.bunnylists.chitchat.ProfileMenu

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.blogspot.bunnylists.chitchat.Chat.LoadingDialog
import com.blogspot.bunnylists.chitchat.R
import com.blogspot.bunnylists.chitchat.User
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UpdateProfileActivity : AppCompatActivity() {
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var profilePic: ImageView
    private lateinit var nameET: EditText
    private lateinit var aboutET: EditText
    private lateinit var updateButton: Button
    private lateinit var mDbRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private val STORAGE_REQ_CODE: Int = 100
    private val IMAGE_PICK_CODE: Int = 200
    private lateinit var mFireStore: FirebaseStorage
    private lateinit var mStorageRef: StorageReference
    private lateinit var loggedInUserMobile: String
    private lateinit var profilePicUrl: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_profile)
        val window = window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.parseColor("#FFFFFF")

        loadingDialog = LoadingDialog(this)
        profilePic = findViewById(R.id.updateProfilePicIV)
        nameET = findViewById(R.id.updateUserName)
        aboutET = findViewById(R.id.updateUserAbout)
        updateButton = findViewById(R.id.UpdateButton)
        mAuth = FirebaseAuth.getInstance()
        loggedInUserMobile = mAuth.currentUser?.phoneNumber!!
        mFireStore = FirebaseStorage.getInstance()

        val name = intent.getStringExtra("name").toString()
        profilePicUrl = intent.getStringExtra("profilePicUrl").toString()
        val about = intent.getStringExtra("about").toString()

        nameET.setText(name)
        aboutET.setText(about)
        Picasso.get().load(profilePicUrl).into(profilePic)

        profilePic.setOnClickListener {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_DENIED
            ) {
                val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                requestPermissions(permissions, STORAGE_REQ_CODE)
            } else
                pickImageFromGallery()
        }

        updateButton.setOnClickListener {
            val updatedName = nameET.text.toString()
            val updatedAbout = aboutET.text.toString()
            mDbRef = FirebaseDatabase.getInstance().reference
            mDbRef.child("Users").child(loggedInUserMobile)
                .setValue(User(updatedName, loggedInUserMobile, profilePicUrl, updatedAbout))
            Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show()
            intent = Intent(this, ProfileActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.putExtra("profileUrl", profilePicUrl)
            intent.putExtra("userName", updatedName)
            intent.putExtra("userAbout", updatedAbout)
            startActivity(intent)
        }
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

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            if (data == null || data.data == null) {
                return
            } else {
                loadingDialog.startLoading()
                profilePic.setImageURI(data.data)
                val fileName = loggedInUserMobile
                mStorageRef = mFireStore.reference.child("UserProfilePictures/$fileName")
                mStorageRef.putFile(data.data!!).addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener {
                        profilePicUrl = it.toString()
                        loadingDialog.isDismiss()
                    }
                }
                    .addOnFailureListener(OnFailureListener { e ->
                        loadingDialog.isDismiss()
                        Toast.makeText(this, "$e", Toast.LENGTH_SHORT).show()
                    })
            }
        }
    }
}