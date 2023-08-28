package com.example.firebasemessenger.ui.messages

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasemessenger.R
import com.example.firebasemessenger.adapter.ChatAdapter
import com.example.firebasemessenger.adapter.UserAdapter
import com.example.firebasemessenger.data.ChatMessage
import com.example.firebasemessenger.data.User
import com.example.firebasemessenger.databinding.ActivityChatLogBinding
import com.example.firebasemessenger.ui.messages.LatestMessagesActivity.Companion.currentUser
import com.example.firebasemessenger.ui.messages.LatestMessagesActivity.Companion.databaseUrl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class ChatLogActivity : AppCompatActivity() {

    // binding
    private lateinit var binding: ActivityChatLogBinding

    // widgets
    private lateinit var rvMsg: RecyclerView
    private lateinit var inputMsg: EditText
    private lateinit var sendMsg: Button

    // variables
    private lateinit var messages: ArrayList<ChatMessage>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatLogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // get extra
        val user = intent.getParcelableExtra<User>(UserAdapter.USER_KEY)

        supportActionBar?.title = user?.uname

        rvMsg = binding.rvChat
        rvMsg.setHasFixedSize(true)

        messages = ArrayList()

        listenForMessage()

        sendMsg = binding.btnSend
        sendMsg.setOnClickListener {
            Log.d("ChatLog", "Attempt to send message")
            performSendMessage()

            inputMsg.text.clear()
        }
    }

    private fun performSendMessage() {
        inputMsg = binding.inputMsg
        val text = inputMsg.text.toString()

        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(UserAdapter.USER_KEY)
        val toId = user?.uid

        if (fromId == null) return
        if (toId == null) return

        // do push() to automatically create new node
        val ref = FirebaseDatabase.getInstance(databaseUrl).getReference("/messages").push()

        val chatMessage =  ChatMessage(
            ref.key!!,
            text,
            fromId,
            toId,
            System.currentTimeMillis()/1000)
        ref.setValue(chatMessage)

        Log.d("ChatLog", "Chat saved: ${ref.key}")

        // set up latest messages database
        // TODO: do some check, if user == group, user different type of latestMessage
        val latestRefFrom = FirebaseDatabase
            .getInstance(databaseUrl)
            .getReference("/latest-messages/$fromId/$toId")
        latestRefFrom.setValue(chatMessage)

        val latestRefTo = FirebaseDatabase
            .getInstance(databaseUrl)
            .getReference("/latest-messages/$toId/$fromId")
        latestRefTo.setValue(chatMessage)
    }

    private fun listenForMessage() {
        val ref = FirebaseDatabase.getInstance(databaseUrl).getReference("/messages")

        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)

                // add to temporary list if user fetched is not null
                if (chatMessage != null) {
                    messages.add(chatMessage)
                }

                showRecyclerList(messages)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) { }

            override fun onChildRemoved(snapshot: DataSnapshot) { }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) { }

            override fun onCancelled(error: DatabaseError) { }

        })
    }

    private fun showRecyclerList(messages: List<ChatMessage>) {
        val fromId = currentUser?.uid
        val user = intent.getParcelableExtra<User>(UserAdapter.USER_KEY)
        val toId = user?.uid
        val toIdType = user?.type

        if (fromId == null) return
        if (toId == null) return
        if (toIdType == null) return

        rvMsg.layoutManager = LinearLayoutManager(this)
        val chatAdapter = ChatAdapter(messages, fromId, toId, toIdType)
        rvMsg.adapter = chatAdapter
    }
}