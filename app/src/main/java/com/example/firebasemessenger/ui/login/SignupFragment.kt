package com.example.firebasemessenger.ui.login

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.firebasemessenger.data.User
import com.example.firebasemessenger.databinding.FragmentSignupBinding
import com.example.firebasemessenger.ui.messages.LatestMessagesActivity
import com.example.firebasemessenger.ui.messages.LatestMessagesActivity.Companion.databaseUrl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class SignupFragment : Fragment() {

    // binding
    private lateinit var binding: FragmentSignupBinding

    // widgets
    private lateinit var inputUname: com.google.android.material.textfield.TextInputEditText
    private lateinit var inputEmail: com.google.android.material.textfield.TextInputEditText
    private lateinit var inputPass: com.google.android.material.textfield.TextInputEditText
    private lateinit var btnSignup: Button
    private lateinit var btnMove: TextView
    private lateinit var btnPhoto: Button
    private lateinit var prevPhoto: de.hdodenhof.circleimageview.CircleImageView

    // variables
    private var selectedPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        inputUname = binding.inputUsername
        inputEmail = binding.inputEmail
        inputPass = binding.inputPassword

        btnSignup = binding.btnSignup
        btnSignup.setOnClickListener { signUp() }

        btnMove = binding.btnMoveSignin
        btnMove.setOnClickListener {
            val action = SignupFragmentDirections.actionSignupFragmentToSigninFragment()
            this.findNavController().navigate(action)
        }

        btnPhoto = binding.btnPhoto
        btnPhoto.setOnClickListener {
            Log.d("SignUp", "Trying to select profile photo")

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
        prevPhoto = binding.previewPhoto
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            // proceed and check what selected image
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, selectedPhotoUri)

            prevPhoto.setImageBitmap(bitmap)
            btnPhoto.alpha = 0f

            // val bitmapDrawable = BitmapDrawable(bitmap)
            // btnPhoto.setBackgroundDrawable(bitmapDrawable)
        }
    }

    private fun signUp() {
        val email = inputEmail.text.toString()
        val pass = inputPass.text.toString()

        if (email.isEmpty() || pass.isEmpty()) {
            Toast
                .makeText(this.context, "Please enter text in email or password", Toast.LENGTH_SHORT)
                .show()
            return
        }

        // Firebase authentication
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener

                // else if successfull
                Log.d("SignUp", "Successfully created user with uid: ${it.result.user?.uid}")

                // uploading photo to Firebase
                uploadImageToFirebaseStorage()
            }
            .addOnFailureListener {
                Log.d("SignUp", "Failed to create user: ${it.message}")
                Toast
                    .makeText(this.context, "Failed to create user: ${it.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun uploadImageToFirebaseStorage() {
        if (selectedPhotoUri == null) return
        val fileName = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$fileName")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("SignUp", "Successfully upload image: ${it.metadata?.path}")

                // retrieve file location
                ref.downloadUrl
                    .addOnSuccessListener { photoUri ->
                        Log.d("SignUp", "File location: $photoUri")
                        saveUserToFirebaseDatabase(photoUri.toString())
                    }
            }
            .addOnFailureListener {
                // TODO: add failure listener
            }
    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid?:""

        val ref = FirebaseDatabase.getInstance(databaseUrl).getReference("/users/$uid")

        val user = User(uid, inputUname.text.toString(), profileImageUrl, 1)
        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("SignUp", "Finally upload to database")

                val intent = Intent(this.context, LatestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)

            }
            .addOnFailureListener {
                Log.d("SignUp", "Failed to upload into database: ${it.message}")
            }
    }
}