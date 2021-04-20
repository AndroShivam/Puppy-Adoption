package com.shivam.puppyadoption.ui

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.shivam.puppyadoption.R
import com.shivam.puppyadoption.data.model.Chat
import com.shivam.puppyadoption.databinding.FragmentChatBinding
import com.shivam.puppyadoption.ui.adapter.ChatAdapter
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle

class ChatFragment : Fragment() {

    private lateinit var binding: FragmentChatBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var database: DatabaseReference

    private lateinit var chatReference: DatabaseReference
    private lateinit var adapter: ChatAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat, container, false)

        // init
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        val currentUserID = auth.currentUser?.uid.toString()
        database = Firebase.database.reference.child("chats")

        // args
        val args = arguments?.let { ChatFragmentArgs.fromBundle(it) }
        val friendID = args?.friendID.toString()

        // Unique ID by combining currentUserID and friendID
        val comboID: String = generateComboID(currentUserID, friendID)

        loadMessages(comboID)

        binding.chatSendBtn.setOnClickListener {
            val message: String = binding.chatEditTxt.text.toString()
            if (!TextUtils.isEmpty(message))
                sendMessage(message, currentUserID, friendID, comboID)
        }

        return binding.root
    }

    private fun loadMessages(comboID: String) {
        chatReference = FirebaseDatabase.getInstance().getReference("chats").child(comboID)

        val chatList = ArrayList<Chat>()

        adapter = ChatAdapter(chatList)
        binding.chatRv.adapter = adapter

        val chatListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatList.clear()
                for (snap in snapshot.children) {
                    val chat: Chat? = snap.getValue(Chat::class.java)
                    if (chat != null) {
                        chatList.add(chat)
                    }
                    adapter.notifyDataSetChanged()
                    binding.chatRv.smoothScrollToPosition(adapter.itemCount)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        }
        chatReference.addValueEventListener(chatListener)
    }

    private fun sendMessage(
        message: String,
        senderID: String,
        receiverID: String,
        comboID: String
    ) {
        binding.chatEditTxt.setText("")
        val chat = Chat(message, senderID, receiverID, getCurrentTime())
        database.child(comboID).push().setValue(chat)
    }

    private fun getCurrentTime() =
        LocalDateTime.now().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))

    private fun generateComboID(currUserID: String, friendID: String): String {
        var index = 0
        return if (currUserID[index] > friendID[index]) currUserID.plus(friendID)
        else if (currUserID[index] == friendID[index]) {
            while (currUserID[index] == friendID[index]) index++
            if (currUserID[index] > friendID[index]) return currUserID.plus(friendID)
            else friendID.plus(currUserID)
        } else friendID.plus(currUserID)
    }
}