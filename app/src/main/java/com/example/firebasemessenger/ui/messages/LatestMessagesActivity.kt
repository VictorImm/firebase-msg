package com.example.firebasemessenger.ui.messages

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasemessenger.MainActivity
import com.example.firebasemessenger.R
import com.example.firebasemessenger.adapter.ChatAdapter
import com.example.firebasemessenger.adapter.LatestAdapter
import com.example.firebasemessenger.data.ChatMessage
import com.example.firebasemessenger.data.User
import com.example.firebasemessenger.databinding.ActivityChatLogBinding
import com.example.firebasemessenger.databinding.ActivityLatestMessagesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue

class LatestMessagesActivity : AppCompatActivity() {

    companion object {
        var currentUser: User? = null
        // TODO: Your own database url
        const val databaseUrl = ""
    }

    // binding
    private lateinit var binding: ActivityLatestMessagesBinding

    // widgets
    private lateinit var rvLatest: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLatestMessagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fetchCurrentUser()

        verifyUserIsLoggedIn()

        rvLatest = binding.rvLatestMeseji
        rvLatest.setHasFixedSize(true)

        listenOnLatestMessages()
    }

    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance(databaseUrl).getReference("/users/$uid")

        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                currentUser = snapshot.getValue(User::class.java)
                Log.d("CurrentUser", "Current user: ${currentUser?.uname}")
            }

            override fun onCancelled(error: DatabaseError) { }
        })
    }

    private fun verifyUserIsLoggedIn() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    private fun listenOnLatestMessages() {
        getReceiverData()

        val ref = FirebaseDatabase.getInstance(databaseUrl).getReference("/latest-messages/${currentUser?.uid}")
        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) { getReceiverData() }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) { getReceiverData() }

            override fun onChildRemoved(snapshot: DataSnapshot) { }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) { }

            override fun onCancelled(error: DatabaseError) { }
        })
    }

    private fun getReceiverData() {
        val listMsg = ArrayList<String>()

        val ref = FirebaseDatabase.getInstance(databaseUrl).getReference("/latest-messages/${currentUser?.uid}")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (childSnapshot in snapshot.children) {
                    val msg = childSnapshot.getValue(ChatMessage::class.java)
                    if (msg != null) {
                        Log.d("LatestMeseji", "${msg.toId}")
                        listMsg.add(msg.toId)
                        Log.d("LatestMeseji", "$listMsg")
                    }
                }

                showRecyclerList(listMsg)
            }
            override fun onCancelled(error: DatabaseError) { }
        })
    }

    private fun showRecyclerList(listMsg: List<String>) {
        rvLatest.layoutManager = LinearLayoutManager(this)
        val latestAdapter = LatestAdapter(listMsg)
        rvLatest.adapter = latestAdapter
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            R.id.menu_new_message -> {
                val intent = Intent(this, NewMessagesActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
}