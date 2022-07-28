package com.blogspot.bunnylists.chitchat.Chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import com.blogspot.bunnylists.chitchat.R

class ChatActivity : AppCompatActivity() {
    private lateinit var sendButton: Button
    private lateinit var profile_Pic: CircleImageView
    private lateinit var nameTextView: TextView
    private lateinit var onlineTextView: TextView
    private lateinit var massageEditText: EditText
    private lateinit var mDbRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var chatList: ArrayList<ChatModel>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        sendButton = findViewById(R.id.sendButton)
        profile_Pic = findViewById(R.id.profile_pic2)
        nameTextView = findViewById(R.id.PersonName2)
        onlineTextView = findViewById(R.id.Online)
        massageEditText = findViewById(R.id.msgEdtTxt)
        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().reference
        chatList = ArrayList()

        val name = intent.getStringExtra("name").toString()
        val profilePicUrl = intent.getStringExtra("picUrl").toString()
        val mobile = intent.getStringExtra("mobile").toString()
        var chatKey: String = intent.getStringExtra("chatKey").toString()
        nameTextView.text = name
        Picasso.get().load(profilePicUrl).into(profile_Pic)
        val loggedInUserMobile = mAuth.currentUser?.phoneNumber!!

        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        chatRecyclerView.setHasFixedSize(true)
        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        val chatRecyclerAdapter = ChatAdapter(this, chatList, loggedInUserMobile, mobile)
        chatRecyclerView.adapter = chatRecyclerAdapter


        mDbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatList.clear()
                if (chatKey.isNullOrEmpty()) {
                    chatKey = "1"
                    if (snapshot.hasChild("chats"))
                        chatKey = (snapshot.child("Chats").childrenCount + 1).toString()
                }
                if (snapshot.child("Chats").child(chatKey).hasChild("massages")) {
                    for (postSnap in snapshot.child("Chats").child(chatKey)
                        .child("massages").children) {
                        if (postSnap.hasChild("msg") && postSnap.hasChild("senderMobile")&& postSnap.hasChild("receiverMobile") && postSnap.hasChild("Timing")) {

                            val msg = postSnap.child("msg").value.toString()
                            val senderMobile = postSnap.child("senderMobile").value.toString()
                            val receiverMobile = postSnap.child("receiverMobile").value.toString()
                            val timing = postSnap.child("Timing").value.toString()

                            val chatModel = ChatModel(
                                name,
                                senderMobile,
                                msg,
                                timing,
                                receiverMobile
                            )
                            chatList.add(chatModel)
                            chatRecyclerView.scrollToPosition(chatList.size - 1)
                            chatRecyclerAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        sendButton.setOnClickListener {
            val massageText = massageEditText.text.toString()
            val timeStamp = (System.currentTimeMillis()).toString().substring(0, 10)

            val currentDT = Calendar.getInstance().time
            val formatter = SimpleDateFormat("MMM dd, hh:mm aa") //or use getDateInstance()
            val formattedDate = formatter.format(currentDT)


            mDbRef.child("Chats").child(chatKey).child("User_1").setValue(loggedInUserMobile)
            mDbRef.child("Chats").child(chatKey).child("User_2").setValue(mobile)
            mDbRef.child("Chats").child(chatKey).child("massages").child(timeStamp).child("msg").setValue(massageText)
            mDbRef.child("Chats").child(chatKey).child("massages").child(timeStamp).child("senderMobile").setValue(loggedInUserMobile)
            mDbRef.child("Chats").child(chatKey).child("massages").child(timeStamp).child("receiverMobile").setValue(mobile)
            mDbRef.child("Chats").child(chatKey).child("massages").child(timeStamp).child("Timing").setValue(formattedDate)
            massageEditText.setText("")
        }
    }
}