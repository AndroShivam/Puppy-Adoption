package com.shivam.puppyadoption.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.shivam.puppyadoption.R
import com.shivam.puppyadoption.ui.OnButtonClickListener
import com.shivam.puppyadoption.ui.OnItemClickListener

class RequestAdapter(
    private val options: FirestoreRecyclerOptions<Request>,
    private val listener: OnButtonClickListener
) : FirestoreRecyclerAdapter<Request, RequestViewHolder>(options) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.items_request, parent, false)
        return RequestViewHolder(view, options, listener)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int, model: Request) {
        holder.userName.text = model.username
        holder.userBio.text = model.user_bio
        holder.reason.text = model.reason
        Glide.with(holder.itemView.context).load(model.user_profile_pic).into(holder.userImg)
    }
}

class RequestViewHolder(
    itemView: View,
    private val options: FirestoreRecyclerOptions<Request>,
    private val listener: OnButtonClickListener
) :
    RecyclerView.ViewHolder(itemView) {

    init {
        itemView.findViewById<MaterialButton>(R.id.request_accept).setOnClickListener {
            listener.OnAcceptClickListener(
                options.snapshots.getSnapshot(adapterPosition),
                adapterPosition
            )
        }
        itemView.findViewById<MaterialButton>(R.id.request_decline).setOnClickListener {
            listener.OnDeclineClickListener(
                options.snapshots.getSnapshot(adapterPosition),
                adapterPosition
            )
        }
    }

    val userName: MaterialTextView = itemView.findViewById(R.id.request_name)
    val userBio: MaterialTextView = itemView.findViewById(R.id.request_bio)
    val userImg: ImageView = itemView.findViewById(R.id.request_img)
    val reason: MaterialTextView = itemView.findViewById(R.id.request_reason)
}

data class Request(
    val userID: String = "userID",
    val username: String = "username",
    val user_bio: String = "user_bio",
    val user_profile_pic: String = "user_profile_pic",
    val reason: String = "reason",
)