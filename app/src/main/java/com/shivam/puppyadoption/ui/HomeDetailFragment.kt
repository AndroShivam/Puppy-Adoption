package com.shivam.puppyadoption.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shivam.puppyadoption.R
import com.shivam.puppyadoption.databinding.FragmentHomeDetailBinding

class HomeDetailFragment : Fragment() {

    private lateinit var binding: FragmentHomeDetailBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var currentUserID: String
    private lateinit var name: String
    private lateinit var bio: String
    private lateinit var img: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home_detail, container, false)

        // init
        auth = FirebaseAuth.getInstance()
        currentUserID = auth.currentUser?.uid.toString()
        firebaseFirestore = FirebaseFirestore.getInstance()

        val args = arguments?.let { HomeDetailFragmentArgs.fromBundle(it) }
        binding.detailDogName.text = args?.dogName
        binding.detailDogBreed.text = args?.dogBreed
        binding.detailDogDesc.text = args?.dogDesc
        binding.detailDogColor.text = args?.dogColor
        binding.detailDogWeight.text = resources.getString(R.string.get_weight, args?.dogWeight)
        binding.detailDogAge.text = resources.getString(R.string.get_age, args?.dogAge)
        Glide.with(this).load(args?.dogImg).into(binding.detailDogImg)
        if (args?.dogGender.equals(resources.getString(R.string.radio_male)))
            setGenderSymbol(R.drawable.male)
        else
            setGenderSymbol(R.drawable.female)

        // disable adoption button for owner
        if (currentUserID == args?.ownerID.toString())
            binding.detailContactBtn.isEnabled = false

        firebaseFirestore.collection("Users").document(args?.ownerID.toString()).get()
            .addOnSuccessListener { documentSnapshot ->
                name = documentSnapshot.getString("username").toString()
                bio = documentSnapshot.getString("user_bio").toString()
                img = documentSnapshot.getString("user_profile_pic").toString()
            }


        // btn click
        binding.detailContactBtn.setOnClickListener {
            val action =
                HomeDetailFragmentDirections.actionHomeDetailFragmentToContactOwnerBottomSheetFragment(
                    ownerName = name,
                    ownerBio = bio,
                    ownerImg = img,
                    ownerID = args?.ownerID.toString(),
                    dogName = args?.dogName.toString()
                )

            view?.findNavController()?.navigate(action)
        }

        return binding.root
    }

    private fun setGenderSymbol(sym: Int) {
        binding.detailGenderSym.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(), sym
            )
        )
    }
}