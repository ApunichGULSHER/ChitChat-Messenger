package com.blogspot.bunnylists.chitchat

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.provider.ContactsContract
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blogspot.bunnylists.chitchat.Chat.LoadingDialog
import com.blogspot.bunnylists.chitchat.ProfileMenu.ProfileActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso


class FriendsList : AppCompatActivity() {
    private lateinit var userListRecyclerView: RecyclerView
    private lateinit var recyclerAdapter: FriendsListAdapter
    private lateinit var mDbRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var massageList: ArrayList<Massege>
    private lateinit var userPic: ImageView
    private var lastMassage = ""
    private var unseenMassages = 0
    private var chatKey = ""
    private var dataSet: Boolean = false
    private lateinit var loggedInUserProfileUrl: String
    private lateinit var loggedInUserName: String
    private lateinit var loggedInUserAbout: String
    private val CONTACT_REQ_CODE: Int = 101
    var contactsSet = mutableSetOf<String>()
    private  lateinit var loadingDialog : LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends_list)
        val window = window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.parseColor("#009688")

        userPic = findViewById(R.id.UserPic)
        massageList = ArrayList()
        mDbRef = FirebaseDatabase.getInstance().reference
        mAuth = FirebaseAuth.getInstance()
        loggedInUserProfileUrl = ""
        loggedInUserName = ""
        loggedInUserAbout = ""

        loadingDialog = LoadingDialog(this)

        userListRecyclerView = findViewById(R.id.UserListRecyclerView)
        userListRecyclerView.setHasFixedSize(true)
        userListRecyclerView.layoutManager = LinearLayoutManager(this)
        recyclerAdapter = FriendsListAdapter(this, massageList)
        userListRecyclerView.adapter = recyclerAdapter


        if (checkSelfPermission(android.Manifest.permission.READ_CONTACTS) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            filterFriends()
        } else {
            val permissions = arrayOf(android.Manifest.permission.READ_CONTACTS)
            requestPermissions(permissions, CONTACT_REQ_CODE)
        }

        userPic.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("profileUrl", loggedInUserProfileUrl)
            intent.putExtra("userName", loggedInUserName)
            intent.putExtra("userAbout", loggedInUserAbout)
            startActivity(intent)
        }
    }

    private fun filterFriends() {
        loadingDialog.startLoading()
        addContacts()
        displayFriendsList()
    }

    @SuppressLint("Range")
    fun addContacts() {
        val phones = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )
        while (phones!!.moveToNext()) {
            var newPhoneNumber: String =
                phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    .toString()
            newPhoneNumber = newPhoneNumber.trim()
            if (newPhoneNumber.length > 10)
                newPhoneNumber = newPhoneNumber.replace("\\s".toRegex(), "")
            if (newPhoneNumber.length > 10)
                newPhoneNumber = newPhoneNumber.replace("-", "")
            if(newPhoneNumber.length > 10)
                newPhoneNumber = newPhoneNumber.replace("(", "")
            if(newPhoneNumber.length > 10)
                newPhoneNumber = newPhoneNumber.replace(")", "")
            if(newPhoneNumber.startsWith("0")){
                newPhoneNumber = newPhoneNumber.substring(1)
            }
            if (newPhoneNumber.length == 10)
                newPhoneNumber = "+91$newPhoneNumber"
            contactsSet.add(newPhoneNumber)
        }
        phones.close()
    }

    private fun displayFriendsList() {
        mDbRef.child("Users").addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                massageList.clear()
                unseenMassages = 2
                lastMassage = ""
                chatKey = ""
                val loggedInUserMobileNumber = mAuth.currentUser?.phoneNumber!!.toString()
                for (postSnap in snapshot.children) {
                    val mobile = postSnap.key.toString()
                    if (mobile == loggedInUserMobileNumber) {
                        loggedInUserProfileUrl = postSnap.child("profilePicUrl").value.toString()
                        loggedInUserName = postSnap.child("name").value.toString()
                        loggedInUserAbout = postSnap.child("about").value.toString()
                        if (loggedInUserProfileUrl.isNotEmpty()) {
                            Picasso.get().load(loggedInUserProfileUrl).into(userPic)
                        }
                    }
                    dataSet = false
                    if (mobile != loggedInUserMobileNumber && contactsSet.contains(mobile)) {
                        val name = postSnap.child("name").value.toString()
                        val picUrl = postSnap.child("profilePicUrl").value.toString()
                        val about = postSnap.child("about").value.toString()
                        mDbRef.child("Chats")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val chatCounts: Int = snapshot.childrenCount.toInt()
                                    if (chatCounts > 0) {
                                        for (postSnap2 in snapshot.children) {
                                            val key = postSnap2.value.toString()
                                            chatKey = key
                                        }
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(this@FriendsList, "Server error", Toast.LENGTH_SHORT).show()
                                }
                            })
                        if (!dataSet) {
                            dataSet = true
                            val massage =
                                Massege(name, mobile, picUrl, chatKey, about)
                            massageList.add(massage)
                        }
                    }
                }
                recyclerAdapter.notifyDataSetChanged()
                loadingDialog.isDismiss()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@FriendsList, "Could not get User data", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CONTACT_REQ_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    filterFriends()
                } else
                    Toast.makeText(this, "We need permission to find friends", Toast.LENGTH_SHORT)
                        .show()
            }
        }
    }
}