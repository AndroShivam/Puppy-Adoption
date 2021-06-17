package com.shivam.puppyadoption.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.shivam.puppyadoption.R
import com.shivam.puppyadoption.databinding.FragmentHomeBinding
import com.shivam.puppyadoption.ui.adapter.HomeAdapter
import com.shivam.puppyadoption.ui.adapter.Post
import com.shivam.puppyadoption.ui.adapter.PostViewHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment(), OnItemClickListener {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapter: FirestoreRecyclerAdapter<Post, PostViewHolder>
    private lateinit var currentUserID: String
    private lateinit var query: Query

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)

        // init
        currentUserID = FirebaseAuth.getInstance().currentUser?.uid.toString()

        // query
        query = FirebaseFirestore.getInstance().collection("Posts")
        val firestoreRecyclerOptions = FirestoreRecyclerOptions.Builder<Post>()
            .setQuery(query, Post::class.java)
            .build()

        adapter = HomeAdapter(options = firestoreRecyclerOptions, listener = this)

        binding.homeRv.setHasFixedSize(true)
        binding.homeRv.adapter = adapter

        binding.homeSwipeRefresh.setOnRefreshListener {
            binding.homeSwipeRefresh.isRefreshing = true
            adapter.notifyDataSetChanged()
            binding.homeSwipeRefresh.isRefreshing = false
        }

        binding.homeFab.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_filterBottomSheetFragment)
        }

        return binding.root
    }

    override fun onItemClick(documentSnapshot: DocumentSnapshot, position: Int) {

        GlobalScope.launch(Dispatchers.IO) {
            val dogName = documentSnapshot.getString("dog_name").toString()
            val dogAge = documentSnapshot.getString("dog_age").toString()
            val dogDesc = documentSnapshot.getString("dog_desc").toString()
            val dogGender = documentSnapshot.getString("dog_gender").toString()
            val dogBreed = documentSnapshot.getString("dog_breed").toString()
            val dogWeight = documentSnapshot.getString("dog_weight").toString()
            val dogColor = documentSnapshot.getString("dog_color").toString()
            val dogImg = documentSnapshot.getString("dog_image").toString()
            val ownerID = documentSnapshot.getString("owner_uid").toString()
            val geoPoint = documentSnapshot.getGeoPoint("coordinates")

            val lat = geoPoint?.latitude.toString()
            val lon = geoPoint?.longitude.toString()

            val action = HomeFragmentDirections.actionHomeFragmentToHomeDetailFragment(
                dogName = dogName,
                dogAge = dogAge,
                dogDesc = dogDesc,
                dogGender = dogGender,
                dogColor = dogColor,
                dogBreed = dogBreed,
                dogWeight = dogWeight,
                dogImg = dogImg,
                ownerID = ownerID,
                latitude = lat,
                longitude = lon
            )
            withContext(Dispatchers.Main) {
                findNavController().navigate(action)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }
}

interface OnItemClickListener {
    fun onItemClick(documentSnapshot: DocumentSnapshot, position: Int)
}