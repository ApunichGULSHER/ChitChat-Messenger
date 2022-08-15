package com.blogspot.bunnylists.chitchat

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.blogspot.bunnylists.chitchat.Chat.ChatActivity
import com.squareup.picasso.Picasso

class FriendsListAdapter(val context: Context, private val massageList : List<Massege>) : RecyclerView.Adapter<FriendsListAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FriendsListAdapter.MyViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.friends_list_layout, parent, false))
    }

    override fun onBindViewHolder(holder: FriendsListAdapter.MyViewHolder, position: Int) {
        holder.PersonName.text = massageList[position].name
        Picasso.get().load(massageList[position].profilePicUrl).into(holder.profile_pic)
        holder.lastMassage.text = massageList[position].lastMessage
        if(massageList[position].unseenMessages == 0){
            holder.unseen_massages_count.isVisible = false
            holder.lastMassage.isVisible = false
        }
        else{
            holder.unseen_massages_count.isVisible = false
            holder.unseen_massages_count.text = massageList[position].unseenMessages.toString()
            holder.lastMassage.isVisible = true
        }

        holder.rootLayout.setOnClickListener {
            val intent  = Intent(context, ChatActivity::class.java)
            intent.putExtra("name", massageList[position].name)
            intent.putExtra("picUrl", massageList[position].profilePicUrl)
            intent.putExtra("mobile",massageList[position].mobile)
            intent.putExtra("chatKey", massageList[position].chatKey)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return massageList.size
    }

    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val profile_pic = itemView.findViewById<ImageView>(R.id.profile_pic)
        val PersonName = itemView.findViewById<TextView>(R.id.PersonName)
        val lastMassage = itemView.findViewById<TextView>(R.id.lastMassage)
        val unseen_massages_count = itemView.findViewById<TextView>(R.id.unseen_massages_count)
        val rootLayout = itemView.findViewById<LinearLayout>(R.id.root_Layout)
    }

}