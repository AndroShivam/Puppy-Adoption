package com.shivam.puppyadoption.ui

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shivam.puppyadoption.R
import com.shivam.puppyadoption.databinding.FragmentContactBottomSheetBinding

class ContactOwnerBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentContactBottomSheetBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var ownerID: String
    private lateinit var currentUserID: String
    private lateinit var currentUserName: String
    private lateinit var currentUserBio: String
    private lateinit var currentUserImg: String
    private lateinit var firebaseFirestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_contact_bottom_sheet,
            container,
            false
        )

        // init
        auth = FirebaseAuth.getInstance()
        currentUserID = auth.currentUser?.uid.toString()
        firebaseFirestore = FirebaseFirestore.getInstance()

        firebaseFirestore.collection("Users").document(currentUserID)
            .get().addOnSuccessListener { documentSnapshot ->
                currentUserName = documentSnapshot.getString("username").toString()
                currentUserBio = documentSnapshot.getString("user_bio").toString()
                currentUserImg = documentSnapshot.getString("user_profile_pic").toString()
            }


        // args
        val args = arguments?.let { ContactOwnerBottomSheetFragmentArgs.fromBundle(it) }
        ownerID = args?.ownerID.toString()
        binding.contactName.text = args?.ownerName
        binding.contactBio.text = args?.ownerBio
        Glide.with(this).load(args?.ownerImg).into(binding.contactProfilePic)

        // btn
        binding.askForAdoptionBtn.setOnClickListener {
            val reason = binding.contactWhy.text.toString()
            if (!TextUtils.isEmpty(reason))
                storeToFirestore(reason, args?.dogName.toString())
        }
        return binding.root
    }

    private fun storeToFirestore(reason: String, dogName: String) {
        val fields = hashMapOf(
            "userID" to currentUserID,
            "reason" to reason,
            "username" to currentUserName,
            "user_bio" to currentUserBio,
            "user_profile_pic" to currentUserImg,
            "dog_name" to dogName
        )

        firebaseFirestore.collection("Users").document(ownerID).collection("Requests")
            .document().set(fields)

        Toast.makeText(context, "Request Sent!", Toast.LENGTH_SHORT).show()
    }
}