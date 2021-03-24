package com.shivam.puppyadoption.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shivam.puppyadoption.R
import com.shivam.puppyadoption.databinding.FragmentNotificationBinding
import com.shivam.puppyadoption.ui.adapter.Notification
import com.shivam.puppyadoption.ui.adapter.NotificationAdapter
import com.shivam.puppyadoption.ui.adapter.NotificationViewHolder

class NotificationFragment : Fragment() {
    private lateinit var binding: FragmentNotificationBinding
    private lateinit var currentUserID: String
    private lateinit var adapter: FirestoreRecyclerAdapter<Notification, NotificationViewHolder>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_notification, container, false)

        // init
        currentUserID = FirebaseAuth.getInstance().currentUser?.uid.toString()

        val query = FirebaseFirestore.getInstance().collection("Users").document(currentUserID)
            .collection("Accepted")
        val firestoreRecyclerOptions = FirestoreRecyclerOptions.Builder<Notification>()
            .setQuery(query, Notification::class.java)
            .build()

        adapter = NotificationAdapter(firestoreRecyclerOptions)

        binding.notificationRv.setHasFixedSize(true)
        binding.notificationRv.adapter = adapter

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
}