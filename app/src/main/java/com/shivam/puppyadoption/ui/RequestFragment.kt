package com.shivam.puppyadoption.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.shivam.puppyadoption.R
import com.shivam.puppyadoption.databinding.FragmentRequestBinding
import com.shivam.puppyadoption.ui.adapter.Request
import com.shivam.puppyadoption.ui.adapter.RequestAdapter
import com.shivam.puppyadoption.ui.adapter.RequestViewHolder


class RequestFragment : Fragment(), OnButtonClickListener {

    private lateinit var binding: FragmentRequestBinding
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var currentUserID: String
    private lateinit var adapter: FirestoreRecyclerAdapter<Request, RequestViewHolder>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_request, container, false)

        // init
        firebaseFirestore = FirebaseFirestore.getInstance()
        currentUserID = FirebaseAuth.getInstance().currentUser?.uid.toString()

        // firestore query
        val query = FirebaseFirestore.getInstance().collection("Users").document(currentUserID)
            .collection("Requests")
        val firestoreRecyclerOptions = FirestoreRecyclerOptions.Builder<Request>()
            .setQuery(query, Request::class.java)
            .build()

        adapter = RequestAdapter(options = firestoreRecyclerOptions, listener = this)

        binding.requestRv.setHasFixedSize(true)
        binding.requestRv.adapter = adapter

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    override fun OnAcceptClickListener(documentSnapshot: DocumentSnapshot, position: Int) {
        val ownerName = documentSnapshot.getString("username")
        val ownerBio = documentSnapshot.getString("user_bio")
        val ownerImg = documentSnapshot.getString("user_profile_pic")

        val userID = documentSnapshot.getString("userID").toString()

        val fields =
            hashMapOf("ownerName" to ownerName, "ownerBio" to ownerBio, "ownerImg" to ownerImg)
        firebaseFirestore.collection("Users").document(userID).collection("Accepted")
            .document().set(fields)

        firebaseFirestore.collection("Users").document(currentUserID).collection("Requests")
            .document(documentSnapshot.id).delete().addOnSuccessListener {
                Toast.makeText(context, "Request Accepted!", Toast.LENGTH_SHORT).show()
            }
    }

    override fun OnDeclineClickListener(documentSnapshot: DocumentSnapshot, position: Int) {
        firebaseFirestore.collection("Users").document(currentUserID).collection("Requests")
            .document(documentSnapshot.id).delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Request Declined!", Toast.LENGTH_SHORT).show()
            }
    }
}

interface OnButtonClickListener {
    fun OnAcceptClickListener(documentSnapshot: DocumentSnapshot, position: Int)
    fun OnDeclineClickListener(documentSnapshot: DocumentSnapshot, position: Int)
}