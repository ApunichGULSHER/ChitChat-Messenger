package com.blogspot.bunnylists.chitchat.Chat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.blogspot.bunnylists.chitchat.R

class ChatAdapter(
    val context: Context,
    private val chatList: List<ChatModel>,
    private val loggedInUserMobile: String,
    private val mobile: String
) : RecyclerView.Adapter<ChatAdapter.MyViewHolder>() {
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val myMassageTV : TextView = itemView.findViewById(R.id.myMassageTextView)
        val yourMassageTV : TextView = itemView.findViewById(R.id.youMassageTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatAdapter.MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(R.layout.chatadapterlayout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentChat = chatList[position]
        if((currentChat.senderMobile == loggedInUserMobile && currentChat.receiverMobile == mobile) || (currentChat.senderMobile == mobile && currentChat.receiverMobile == loggedInUserMobile)){
            if (currentChat.senderMobile == loggedInUserMobile) {
                holder.myMassageTV.isVisible = true
                holder.yourMassageTV.isVisible = false
                holder.myMassageTV.text = currentChat.massage
            } else {
                holder.yourMassageTV.isVisible = true
                holder.myMassageTV.isVisible = false
                holder.yourMassageTV.text = currentChat.massage
            }
        }else{
            holder.myMassageTV.isVisible = false
            holder.yourMassageTV.isVisible = false
        }
    }

    override fun getItemCount(): Int {
        return chatList.size
    }
}