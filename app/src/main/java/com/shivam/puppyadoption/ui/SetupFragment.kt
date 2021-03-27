package com.shivam.puppyadoption.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.shivam.puppyadoption.R
import com.shivam.puppyadoption.databinding.FragmentSetupBinding
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView

class SetupFragment : Fragment() {

    private lateinit var binding: FragmentSetupBinding
    private lateinit var currentUserID: String
    private var profileImageURI = Uri.EMPTY
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var storageReference: StorageReference


    companion object {
        private const val STORAGE_PERMISSION_CODE = 123
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_setup, container, false)

        // init
        auth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        currentUserID = auth.currentUser?.uid.toString()

        binding.setupPicture.setOnClickListener {
            checkPermission()
        }

        binding.setupBtn.setOnClickListener {
            val name = binding.setupName.text.toString()
            val bio = binding.setupBio.text.toString()
            if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(bio) && profileImageURI != Uri.EMPTY)
                saveToFirestore(name, bio);
        }

        return binding.root
    }

    private fun saveToFirestore(name: String, bio: String) {

        // show progress bar
        toggleProgressBar()

        // save name and bio
        val fields = hashMapOf(
            "username" to name,
            "user_bio" to bio
        )

        // store image
        val imagePath = storageReference.child("User_Profile_Pictures").child("$currentUserID.jpg")
        imagePath.putFile(profileImageURI).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // get uri
                imagePath.downloadUrl.addOnSuccessListener { uri ->
                    fields["user_profile_pic"] = uri.toString()
                    // store to firestore
                    firebaseFirestore.collection("Users")
                        .document(currentUserID)
                        .set(fields, SetOptions.merge())
                    // open main activity
                    view?.findNavController()?.navigate(R.id.action_setupFragment_to_mainActivity)
                    findNavController().popBackStack()
                }
            } else {
                toggleProgressBar() // hide progress bar
                Toast.makeText(context, "Something went wrong!", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openGallery()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE
            )
        }
    }


    private fun openGallery() {
        CropImage.activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(1, 1)
            .start(requireContext(), this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                profileImageURI = result.uri
                binding.setupPicture.setImageURI(profileImageURI)
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(requireContext(), "Error : ${result.error}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    fun toggleProgressBar() {
        if (binding.setupProgressBar.visibility == View.VISIBLE) {
            binding.setupProgressBar.visibility = View.INVISIBLE
            binding.setupBtn.isEnabled = true
        } else {
            binding.setupProgressBar.visibility = View.VISIBLE
            binding.setupBtn.isEnabled = false
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            STORAGE_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    openGallery()
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
}