package com.example.firebasemessenger.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasemessenger.R
import com.example.firebasemessenger.data.ChatMessage
import com.example.firebasemessenger.data.User
import com.example.firebasemessenger.ui.messages.LatestMessagesActivity.Companion.currentUser
import com.example.firebasemessenger.ui.messages.LatestMessagesActivity.Companion.databaseUrl
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ChatAdapter(
    private val listChat: List<ChatMessage>,
    private val fromId: String,
    private val toId: String,
    private val toIdType: Int
    ): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // View types constants
    private val VIEW_TYPE_ROW_FROM = 1
    private val VIEW_TYPE_ROW_TO = 2

    class ListViewHolderFrom(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvProfile: CircleImageView = itemView.findViewById(R.id.profile)
        var tvChat: TextView = itemView.findViewById(R.id.message)
    }

    class ListViewHolderTo(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvProfile: CircleImageView = itemView.findViewById(R.id.profile)
        var tvChat: TextView = itemView.findViewById(R.id.message)
    }

    class EmptyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {}

    override fun getItemViewType(position: Int): Int {
        val chat = listChat[position]

        if (toIdType == 2) {
            // check if user == group
            return if (chat.toId == toId) {
                Log.d("ChatGroup", "1 message masuk ke grup")
                if (chat.fromId == fromId) {
                    VIEW_TYPE_ROW_FROM
                } else {
                    VIEW_TYPE_ROW_TO
                }
            } else {
                0
            }
        } else {
            // check if user == rill person
            return if (chat.fromId == fromId && chat.toId == toId || chat.fromId == toId && chat.toId == fromId) {
                if (chat.fromId == fromId) {
                    VIEW_TYPE_ROW_FROM
                } else {
                    VIEW_TYPE_ROW_TO
                }
            } else {
                0
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ROW_FROM -> {
                val view: View =
                    LayoutInflater.from(parent.context)
                        .inflate(
                            R.layout.row_chat_from,
                            parent,
                            false
                        )
                ListViewHolderFrom(view)
            }
            VIEW_TYPE_ROW_TO -> {
                val view: View =
                    LayoutInflater.from(parent.context)
                        .inflate(
                            R.layout.row_chat_to,
                            parent,
                            false
                        )
                ListViewHolderTo(view)
            }
            else -> {
                val view: View = View(parent.context)
                EmptyViewHolder(view)
            }
        }

    }

    override fun getItemCount(): Int = listChat.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val chat = listChat[position]

        when (toIdType) {
            1 -> {
                when (holder.itemViewType) {
                    VIEW_TYPE_ROW_FROM -> {
                        val viewHolderFrom = holder as ListViewHolderFrom

                        viewHolderFrom.tvChat.text = chat.text
                        Picasso.get()
                            .load(currentUser?.profileImage)
                            .into(viewHolderFrom.tvProfile)
                    }
                    VIEW_TYPE_ROW_TO -> {
                        val viewHolderTo = holder as ListViewHolderTo

                        // access user detail from uid
                        val toIdRef = FirebaseDatabase.getInstance(databaseUrl).getReference("/users/$toId")
                        toIdRef.addListenerForSingleValueEvent(object: ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val profileImage = snapshot.getValue(User::class.java)

                                viewHolderTo.tvChat.text = chat.text
                                Picasso.get()
                                    .load(profileImage?.profileImage)
                                    .into(viewHolderTo.tvProfile)
                            }
                            override fun onCancelled(error: DatabaseError) { }
                        })
                    }
                }
            }
            2 -> {
                when (holder.itemViewType) {
                    VIEW_TYPE_ROW_FROM -> {
                        val viewHolderFrom = holder as ListViewHolderFrom

                        viewHolderFrom.tvChat.text = chat.text
                        Picasso.get()
                            .load(currentUser?.profileImage)
                            .into(viewHolderFrom.tvProfile)
                    }
                    VIEW_TYPE_ROW_TO -> {
                        val viewHolderTo = holder as ListViewHolderTo

                        // access user detail from uid
                        val toIdRef = FirebaseDatabase.getInstance(databaseUrl).getReference("/users/${chat.fromId}")
                        toIdRef.addListenerForSingleValueEvent(object: ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val profileImage = snapshot.getValue(User::class.java)

                                viewHolderTo.tvChat.text = chat.text
                                Picasso.get()
                                    .load(profileImage?.profileImage)
                                    .into(viewHolderTo.tvProfile)
                            }
                            override fun onCancelled(error: DatabaseError) { }
                        })
                    }
                }
            }
        }


    }
}