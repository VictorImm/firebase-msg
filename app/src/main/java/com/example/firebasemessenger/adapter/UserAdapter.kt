package com.example.firebasemessenger.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasemessenger.R
import com.example.firebasemessenger.data.User
import com.example.firebasemessenger.ui.messages.ChatLogActivity
import com.example.firebasemessenger.ui.messages.NewMessagesActivity
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter(
    private val listUser: List<User>,
    private val userId: String
    ): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val USER_KEY = "USER_KEY"
    }

    class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvProfile: CircleImageView = itemView.findViewById(R.id.profile)
        var tvUname: TextView = itemView.findViewById(R.id.username)
    }

    class EmptyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {}

    override fun getItemViewType(position: Int): Int {
        val user = listUser[position]
        return if (user.uid == userId) {
            // if user is in your device
            0
        } else {
            // if user is in other device
            1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            // if user is in your device
            0 -> {
                val view: View = View(parent.context)
                EmptyViewHolder(view)
            }
            // if user is in other device
            else -> {
                val view: View =
                    LayoutInflater.from(parent.context)
                        .inflate(
                            R.layout.row_user,
                            parent,
                            false
                        )
                ListViewHolder(view)
            }
        }
    }

    override fun getItemCount(): Int = listUser.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val user = listUser[position]

        when (holder.itemViewType) {
            1 -> {
                val viewHolder = holder as ListViewHolder

                // show data into layout
                Picasso.get()
                    .load(user.profileImage)
                    .into(viewHolder.tvProfile)
                viewHolder.tvUname.text = user.uname

                // set holder click listener
                viewHolder.itemView.setOnClickListener {
                    val intent = Intent(viewHolder.itemView.context, ChatLogActivity::class.java)
                    intent.putExtra(USER_KEY, user)
                    viewHolder.itemView.context.startActivity(intent)

                    // Finish the current activity
                    if (viewHolder.itemView.context is NewMessagesActivity) {
                        val currentActivity = viewHolder.itemView.context as NewMessagesActivity
                        currentActivity.finish()
                    }
                }
            }
        }
    }
}