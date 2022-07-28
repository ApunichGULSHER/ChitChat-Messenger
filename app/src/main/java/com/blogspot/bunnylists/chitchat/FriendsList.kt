package com.blogspot.bunnylists.chitchat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blogspot.bunnylists.chitchat.ProfileMenu.ProfileActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

class FriendsList : AppCompatActivity() {
    private lateinit var userListRecyclerView: RecyclerView
    private lateinit var mDbRef : DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var massageList : ArrayList<Massege>
    private lateinit var userPic : ImageView
    private var lastMassage = ""
    private var unseenMassages = 0
    private var chatKey = ""
    private var dataSet : Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends_list)
        userPic = findViewById(R.id.UserPic)
        userPic.setOnClickListener {
            val intent : Intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
        massageList = ArrayList()
        mDbRef = FirebaseDatabase.getInstance().reference
        mAuth = FirebaseAuth.getInstance()

        userListRecyclerView = findViewById(R.id.UserListRecyclerView)
        userListRecyclerView.setHasFixedSize(true)
        userListRecyclerView.layoutManager = LinearLayoutManager(this)
        val recyclerAdapter = FriendsListAdapter(this, massageList)
        userListRecyclerView.adapter = recyclerAdapter

        mDbRef.child("Users").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                massageList.clear()
                unseenMassages = 2
                lastMassage = ""
                chatKey = ""
                val loggedInUserMobileNumber = mAuth.currentUser?.phoneNumber!!.toString()
                for(postSnap in snapshot.children){
                    val mobile = postSnap.key.toString()
                    if(mobile==loggedInUserMobileNumber){
                        val picUrl = postSnap.child("profilePicUrl").value.toString()
                        if(picUrl.isNotEmpty()){
                         Picasso.get().load(picUrl).into(userPic)
                        }
                    }
                    dataSet = false
                    if(mobile!=loggedInUserMobileNumber){
                        val name = postSnap.child("name").value.toString()
                        val picUrl = postSnap.child("profilePicUrl").value.toString()
                        mDbRef.child("Chats").addListenerForSingleValueEvent(object : ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val chatCounts : Int = snapshot.childrenCount.toInt()
                                if(chatCounts>0){
                                    for (postSnap2 in snapshot.children){
                                        val key = postSnap2.value.toString()
                                        chatKey = key
                                        if(postSnap2.hasChild("User_1") && postSnap2.hasChild("User2") && postSnap2.hasChild("massages")){
                                            val user1 = postSnap2.child("User_1").value.toString()
                                            val user2 = postSnap2.child("User_2").value.toString()
                                            if((user1 == mobile && user2 == loggedInUserMobileNumber) || (user1 == loggedInUserMobileNumber && user2 == mobile)){
                                                for(chatSnap in postSnap2.child("massages").children){
                                                    lastMassage = chatSnap.child("msg").value.toString()
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
                        if(!dataSet){
                            dataSet = true
                            val massage = Massege(name, mobile, lastMassage, unseenMassages, picUrl, chatKey)
                            massageList.add(massage)
                        }
                    }
                }
                recyclerAdapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@FriendsList, "Could not get User data", Toast.LENGTH_SHORT).show()
            }
        })
    }
}