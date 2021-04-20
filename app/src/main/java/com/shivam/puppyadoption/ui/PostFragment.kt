package com.shivam.puppyadoption.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.shivam.puppyadoption.R
import com.shivam.puppyadoption.databinding.FragmentPostBinding
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.util.*


class PostFragment : Fragment() {

    private lateinit var binding: FragmentPostBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var storageReference: StorageReference
    private lateinit var currentUserID: String
    private lateinit var gender: String
    private lateinit var breed: String
    private var dogImgURI = Uri.EMPTY

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_post, container, false)

        // init
        firebaseFirestore = FirebaseFirestore.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        auth = FirebaseAuth.getInstance()
        currentUserID = auth.currentUser?.uid.toString()

        // image selection
        binding.newDogImg.setOnClickListener {
            openGallery()
        }

        // Breed Selection
        val breeds = resources.getStringArray(R.array.breeds)
        val arrayAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, breeds)
        binding.newDogBreed.setAdapter(arrayAdapter)

        // Breed Drop Down Menu
        binding.newDogBreed.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, view, position, l ->
                breed = adapterView.getItemAtPosition(position).toString()
            }

        // Gender Selection
        binding.maleContainer.setOnClickListener {
            binding.maleContainer.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.grey
                )
            )
            binding.femaleContainer.setBackgroundColor(Color.TRANSPARENT)
            gender = resources.getString(R.string.radio_male)
        }

        binding.femaleContainer.setOnClickListener {
            binding.femaleContainer.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.grey
                )
            )
            binding.maleContainer.setBackgroundColor(Color.TRANSPARENT)
            gender = resources.getString(R.string.radio_female)
        }

        // Post button
        binding.newPostBtn.setOnClickListener {
            val name = binding.newDogName.text.toString().trim()
            val desc = binding.newDogDesc.text.toString().trim()
            val age = binding.newDogAge.text.toString().trim()
            val weight = binding.newDogWeight.text.toString().trim()
            val color = binding.newColor.text.toString().capitalize(Locale.ROOT).trim()

            // chips
            val tagString = binding.chipGroup.children.toList().filter { (it as Chip).isChecked }
                .joinToString(", ") { (it as Chip).text }
            val tagsList = tagString.split(", ")

            if (!isEmpty(name) && !isEmpty(desc) && !isEmpty(age) && !isEmpty(gender) && !isEmpty(
                    breed
                ) && !isEmpty(weight) && !isEmpty(color) && tagsList.isNotEmpty() && dogImgURI != Uri.EMPTY
            ) {
                createPost(name, desc, age, weight, color, gender, breed, tagsList)
            }


        }
        return binding.root
    }

    private fun openGallery() {
        CropImage.activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(1, 1)
            .start(requireContext(), this);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                dogImgURI = result.uri
                binding.newDogImg.setImageURI(dogImgURI)
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                Toast.makeText(context, "Error : $error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createPost(
        name: String,
        desc: String,
        age: String,
        weight: String,
        color: String,
        gender: String,
        breed: String,
        tagsList: List<String>
    ) {

        val fields = hashMapOf(
            "dog_name" to name,
            "dog_desc" to desc,
            "dog_age" to age,
            "dog_weight" to weight,
            "dog_color" to color,
            "dog_gender" to gender,
            "dog_breed" to breed,
            "post_tags" to tagsList,
            "owner_uid" to currentUserID
        )

        val imagePath = storageReference.child("Posts").child(UUID.randomUUID().toString())
        imagePath.putFile(dogImgURI).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                imagePath.downloadUrl.addOnSuccessListener { uri ->
                    fields["dog_image"] = uri.toString()
                    firebaseFirestore.collection("Posts").document().set(fields, SetOptions.merge())
                }
            } else {
                Toast.makeText(context, "Error : ${task.exception?.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun isEmpty(input: String): Boolean = input.trim().isEmpty()
}