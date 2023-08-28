package com.example.firebasemessenger.ui.messages

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasemessenger.adapter.UserAdapter
import com.example.firebasemessenger.data.User
import com.example.firebasemessenger.databinding.ActivityNewMessagesBinding
import com.example.firebasemessenger.ui.messages.LatestMessagesActivity.Companion.databaseUrl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NewMessagesActivity : AppCompatActivity() {

    // binding
    private lateinit var binding: ActivityNewMessagesBinding

    // widgets
    private lateinit var rvUser: RecyclerView

    // variables
    private lateinit var users: ArrayList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewMessagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Select User"

        rvUser = binding.rvUser
        rvUser.setHasFixedSize(true)

        users = ArrayList()

        fetchUsers()
    }

    private fun fetchUsers() {
        // get item from Firebase
        val ref = FirebaseDatabase.getInstance(databaseUrl).getReference("/users")
        Log.d("UsersView", "Data fetched: $ref")

        ref.addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

               // access each data in Firebase
                snapshot.children.forEach {
                    Log.d("UsersView", "${it.toString()}")

                    // get value
                    val user = it.getValue(User::class.java)

                    // add to temporary list if user fetched is not null
                    if (user != null) {
                        users.add(user)
                    }
                }
                Log.d("UsersView", "RecycleView created with data: $users")
                showRecyclerList(users)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("UsersView", "Error $error")
            }
        })
    }

    private fun showRecyclerList(users: List<User>) {
        val userId = FirebaseAuth.getInstance().uid

        if (userId == null) return

        rvUser.layoutManager = LinearLayoutManager(this)
        val userAdapter = UserAdapter(users, userId)
        rvUser.adapter = userAdapter
    }
}