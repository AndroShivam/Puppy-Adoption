package com.shivam.puppyadoption.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.shivam.puppyadoption.R
import com.shivam.puppyadoption.databinding.FragmentProfileBinding
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView


class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var storageReference: StorageReference
    private lateinit var currentUserID: String
    private var profileImageURI = Uri.EMPTY

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)

        // init
        auth = FirebaseAuth.getInstance()
        currentUserID = auth.currentUser?.uid.toString()
        firebaseFirestore = FirebaseFirestore.getInstance()
        storageReference = FirebaseStorage.getInstance().reference

        val source = Source.CACHE
        firebaseFirestore.collection("Users").document(currentUserID).get(source)
            .addOnSuccessListener { documentSnapshot ->
                val name = documentSnapshot.getString("username").toString()
                val bio = documentSnapshot.getString("user_bio")
                val img = documentSnapshot.getString("user_profile_pic")

                binding.profileName.setText(name)
                binding.profileBio.setText(bio)
                Glide.with(this).load(img).into(binding.profileImg)
            }


        binding.profileImg.setOnClickListener {
            openGallery()
        }

        binding.profileSaveBtn.setOnClickListener {
            val name = binding.profileName.text.toString()
            val bio = binding.profileBio.text.toString()

            if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(bio))
                saveToFirebase(name, bio)
        }

        binding.profileLogoutBtn.setOnClickListener {
            auth.signOut()
            findNavController().popBackStack()
        }

        return binding.root
    }

    private fun openGallery() {
        CropImage.activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(1, 1)
            .start(requireContext(), this)
    }

    private fun saveToFirebase(name: String, bio: String) {
        if (profileImageURI != Uri.EMPTY) {
            saveProfileImage()
            saveInfo(name, bio)
        } else {
            saveInfo(name, bio)
        }
    }

    private fun saveInfo(name: String, bio: String) {
        val fields = hashMapOf(
            "username" to name,
            "user_bio" to bio
        )

        firebaseFirestore.collection("Users").document(currentUserID)
            .set(fields, SetOptions.merge())
    }

    private fun saveProfileImage() {
        val imagePath = storageReference.child("User_Profile_Pictures").child("$currentUserID.jpg")

        imagePath.putFile(profileImageURI).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                imagePath.downloadUrl.addOnSuccessListener { uri ->
                    val field = hashMapOf("user_profile_pic" to uri.toString())
                    firebaseFirestore.collection("Users").document(currentUserID)
                        .set(field, SetOptions.merge())
                }
            } else {
                Toast.makeText(context, "Error : ${task.exception?.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                profileImageURI = result.uri
                binding.profileImg.setImageURI(profileImageURI)
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(requireContext(), "Error : ${result.error}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}