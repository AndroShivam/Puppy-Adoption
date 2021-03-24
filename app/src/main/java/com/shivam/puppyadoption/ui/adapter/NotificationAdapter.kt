package com.shivam.puppyadoption.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.textview.MaterialTextView
import com.shivam.puppyadoption.R

class NotificationAdapter(options: FirestoreRecyclerOptions<Notification>) :
    FirestoreRecyclerAdapter<Notification, NotificationViewHolder>(options) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.items_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: NotificationViewHolder,
        position: Int,
        model: Notification
    ) {
        holder.ownerName.text = model.ownerName
        Glide.with(holder.itemView.context).load(model.ownerImg).into(holder.ownerImg)
    }
}

class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val ownerName: MaterialTextView = itemView.findViewById(R.id.notification_name)
    val ownerImg: ImageView = itemView.findViewById(R.id.notification_img)
}

data class Notification(
    val ownerName: String = "ownerName",
    val ownerImg: String = "ownerImg"
)