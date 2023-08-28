package com.example.firebasemessenger.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.firebasemessenger.databinding.FragmentSigninBinding
import com.example.firebasemessenger.ui.messages.LatestMessagesActivity
import com.google.firebase.auth.FirebaseAuth

class SigninFragment : Fragment() {

    // binding
    private lateinit var binding: FragmentSigninBinding

    // widgets
    private lateinit var inputEmail: com.google.android.material.textfield.TextInputEditText
    private lateinit var inputPass: com.google.android.material.textfield.TextInputEditText
    private lateinit var btnSignin: Button
    private lateinit var btnMove: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSigninBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        inputEmail = binding.inputEmail
        inputPass = binding.inputPassword

        btnSignin = binding.btnSignin
        btnSignin.setOnClickListener { signIn() }

        btnMove = binding.btnMoveSignup
        btnMove.setOnClickListener { closeFragment() }
    }

    private fun signIn() {
        val email = inputEmail.text.toString()
        val pass = inputPass.text.toString()

        if (email.isEmpty() || pass.isEmpty()) {
            Toast
                .makeText(this.context, "Please enter text in email or password", Toast.LENGTH_SHORT)
                .show()
            return
        }

        // Firebase authentication
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener

                // else if successful
                Log.d("Sign In", "Successfully login user with uid: ${it.result.user?.uid}")

                val intent = Intent(this.context, LatestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                Log.d("Sign In", "Failed to login user: ${it.message}")
                Toast
                    .makeText(this.context, "Failed to login user: ${it.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun closeFragment() {
        val navController = findNavController()
        navController.popBackStack()
    }
}