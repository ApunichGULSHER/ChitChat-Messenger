package com.blogspot.bunnylists.chitchat.ProfileMenu

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.blogspot.bunnylists.chitchat.R
import com.google.firebase.database.*

class InviteListAdapter(val context: Context, private val inviteList : List<Contact>) :
    RecyclerView.Adapter<InviteListAdapter.MyViewHolder>() {
    private lateinit var mDbRef : DatabaseReference
    private lateinit var appLink : String
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.invite_list_layout, parent, false))
    }

    override fun getItemCount(): Int {
        return inviteList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.contactName.text = inviteList[position].contactName
        holder.contactPhone.text = inviteList[position].contactPhone
        mDbRef = FirebaseDatabase.getInstance().reference
        appLink = ""
        mDbRef.child("AppDownloadLink").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                appLink = snapshot.value.toString()
            }
            override fun onCancelled(error: DatabaseError) {
                appLink = "Server Error, can't get download link"
            }
        })
        holder.rootLayout.setOnClickListener {
            val smsUri = Uri.parse("smsto:${inviteList[position].contactPhone}")
            val smsIntent = Intent(Intent.ACTION_SENDTO, smsUri)
            smsIntent.putExtra("sms_body", "Hey let's chat on ChiChat-Messenger, follow the link to download: $appLink")
            context.startActivity(smsIntent)
        }
    }

    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val contactName: TextView = itemView.findViewById(R.id.contactNameTV)
        val contactPhone: TextView = itemView.findViewById(R.id.contactPhoneTV)
        val rootLayout : RelativeLayout = itemView.findViewById(R.id.rootLayout)
    }

}