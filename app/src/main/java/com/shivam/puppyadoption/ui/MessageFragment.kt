package com.shivam.puppyadoption.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.shivam.puppyadoption.R
import com.shivam.puppyadoption.databinding.FragmentMessageBinding
import com.shivam.puppyadoption.ui.adapter.Friend
import com.shivam.puppyadoption.ui.adapter.FriendViewHolder
import com.shivam.puppyadoption.ui.adapter.MessageAdapter

class MessageFragment : Fragment(), OnItemClickListener {

    private lateinit var binding: FragmentMessageBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUserID: String

    private lateinit var adapter: FirestoreRecyclerAdapter<Friend, FriendViewHolder>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_message, container, false)

        // init
        auth = FirebaseAuth.getInstance()
        currentUserID = auth.currentUser?.uid.toString()

        // query
        val query = FirebaseFirestore.getInstance().collection("Users").document(currentUserID)
            .collection("Friends")
        val firestoreRecyclerOptions = FirestoreRecyclerOptions.Builder<Friend>()
            .setQuery(query, Friend::class.java)
            .build()

        adapter = MessageAdapter(options = firestoreRecyclerOptions, listener = this)

        binding.messageRv.setHasFixedSize(true)
        binding.messageRv.adapter = adapter
        return binding.root
    }

    override fun onItemClick(documentSnapshot: DocumentSnapshot, position: Int) {
        val friendID: String = documentSnapshot.getString("friend_id").toString()
        val action = MessageFragmentDirections.actionMessageFragmentToChatFragment(
            friendID = friendID
        )
        findNavController().navigate(action)
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