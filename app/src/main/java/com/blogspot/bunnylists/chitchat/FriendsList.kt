package com.blogspot.bunnylists.chitchat

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    var contacts = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends_list)
        userPic = findViewById(R.id.UserPic)
        massageList = ArrayList()
        mDbRef = FirebaseDatabase.getInstance().reference
        mAuth = FirebaseAuth.getInstance()
        loggedInUserProfileUrl = ""
        loggedInUserName = ""
        loggedInUserAbout = ""

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
        addContacts()
        displayFriendsList()
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
                    if (mobile != loggedInUserMobileNumber) {
                        val name = postSnap.child("name").value.toString()
                        val picUrl = postSnap.child("profilePicUrl").value.toString()
                        mDbRef.child("Chats")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val chatCounts: Int = snapshot.childrenCount.toInt()
                                    if (chatCounts > 0) {
                                        for (postSnap2 in snapshot.children) {
                                            val key = postSnap2.value.toString()
                                            chatKey = key
                                            if (postSnap2.hasChild("User_1") && postSnap2.hasChild("User2") && postSnap2.hasChild(
                                                    "massages"
                                                )
                                            ) {
                                                val user1 =
                                                    postSnap2.child("User_1").value.toString()
                                                val user2 =
                                                    postSnap2.child("User_2").value.toString()
                                                if ((user1 == mobile && user2 == loggedInUserMobileNumber) || (user1 == loggedInUserMobileNumber && user2 == mobile)) {
                                                    for (chatSnap in postSnap2.child("massages").children) {
                                                        lastMassage =
                                                            chatSnap.child("msg").value.toString()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    TODO("Not yet implemented")
                                }

                            })
                        if (!dataSet) {
                            dataSet = true
                            val massage =
                                Massege(name, mobile, lastMassage, unseenMassages, picUrl, chatKey)
                            massageList.add(massage)
                        }
                    }
                }
                recyclerAdapter.notifyDataSetChanged()
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
                    displayFriendsList()
                } else
                    Toast.makeText(this, "We need permission to find friends", Toast.LENGTH_SHORT)
                        .show()
            }
        }
    }

    @SuppressLint("Range")
    fun addContacts() {

        //to store name-number pair
        val phones = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )
        while (phones!!.moveToNext()) {
            var newPhoneNumber : String =
                phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).toString()
            newPhoneNumber = newPhoneNumber.trim()
            if (newPhoneNumber.length>10)
                newPhoneNumber = newPhoneNumber.replace("\\s".toRegex(), "")
            if (newPhoneNumber.length>10)
                newPhoneNumber = newPhoneNumber.replace("-", "")
            if(newPhoneNumber.length == 10)
                newPhoneNumber= "+91$newPhoneNumber"
            contacts.add(newPhoneNumber)
            Log.d("Contacts", newPhoneNumber)
        }
        phones.close()

    }
}