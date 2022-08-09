package com.blogspot.bunnylists.chitchat.ProfileMenu

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blogspot.bunnylists.chitchat.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class InviteFriends : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var inviteListAdapter: InviteListAdapter
    private lateinit var invitableContacts : ArrayList<Contact>
    private lateinit var mDbRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var contacts : ArrayList<Contact>
    private var contactsSet = mutableSetOf<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invite_friends)

        invitableContacts = ArrayList()
        contacts = ArrayList()
        mDbRef = FirebaseDatabase.getInstance().reference
        mAuth = FirebaseAuth.getInstance()

        recyclerView = findViewById(R.id.invitableUserListRecyclerView)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        inviteListAdapter = InviteListAdapter(this, invitableContacts)
        recyclerView.adapter = inviteListAdapter

        addContacts()

        mDbRef.child("Users").addListenerForSingleValueEvent(object : ValueEventListener{
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                for (contact in contacts){
                    if (snapshot.hasChild(contact.contactPhone)){
                        continue
                    }
                    else {
                        invitableContacts.add(contact)

                    }
                }
                inviteListAdapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@InviteFriends, "Check contacts permission and try again",Toast.LENGTH_SHORT).show()
            }
        })
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
            var name : String =
                phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
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
            if (newPhoneNumber.length == 10)
                newPhoneNumber = "+91$newPhoneNumber"
            if(contactsSet.contains(newPhoneNumber))
                continue
            else{
                val contact = Contact(name, newPhoneNumber)
                contacts.add(contact)
                contactsSet.add(newPhoneNumber)
            }
        }
        phones.close()
    }
}