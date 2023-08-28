package com.example.firebasemessenger.adapter

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasemessenger.R
import com.example.firebasemessenger.data.ChatMessage
import com.example.firebasemessenger.data.User
import com.example.firebasemessenger.ui.messages.ChatLogActivity
import com.example.firebasemessenger.ui.messages.LatestMessagesActivity.Companion.currentUser
import com.example.firebasemessenger.ui.messages.LatestMessagesActivity.Companion.databaseUrl
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class LatestAdapter(
    private val listReceiver: List<String>
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvProfile: CircleImageView = itemView.findViewById(R.id.profile)
        var tvUname: TextView = itemView.findViewById(R.id.username)
        var tvLatest: TextView = itemView.findViewById(R.id.latest_message)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.row_latest_message,
                    parent,
                    false
                )
        return ListViewHolder(view)
    }

    override fun getItemCount(): Int = listReceiver.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val receiver = listReceiver[position]

        val viewHolder = holder as ListViewHolder

        // access database
        val ref = FirebaseDatabase
            .getInstance(databaseUrl)
            .getReference("/latest-messages/${currentUser?.uid}/$receiver")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val receiverDetail = snapshot.getValue(ChatMessage::class.java)

                if (receiverDetail != null) {
                    getReceiverProfile(receiverDetail.toId, viewHolder)
                    viewHolder.tvLatest.text = receiverDetail?.text.toString()
                }
            }
            override fun onCancelled(error: DatabaseError) { }
        })

    }

    private fun getReceiverProfile(uid: String, viewHolder: LatestAdapter.ListViewHolder) {
        val ref = FirebaseDatabase
            .getInstance(databaseUrl)
            .getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val profile = snapshot.getValue(User::class.java)

                Picasso.get()
                    .load(profile?.profileImage)
                    .into(viewHolder.tvProfile)
                viewHolder.tvUname.text = profile?.uname

                // set holder click listener
                viewHolder.itemView.setOnClickListener {
                    val intent = Intent(viewHolder.itemView.context, ChatLogActivity::class.java)
                    intent.putExtra(UserAdapter.USER_KEY, profile)
                    viewHolder.itemView.context.startActivity(intent)
                }
            }
            override fun onCancelled(error: DatabaseError) { }
        })
    }
}