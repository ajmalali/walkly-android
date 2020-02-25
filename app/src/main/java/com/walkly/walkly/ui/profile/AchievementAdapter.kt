package com.walkly.walkly.ui.profile

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.walkly.walkly.R


class AchievementAdapter(var friends: List<Friend>): RecyclerView.Adapter<FriendsHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsHolder {
        val view = layoutInflater.inflate(R.layout.list_friend, parent, false)
        return FriendsHolder(view)
    }

    override fun getItemCount(): Int {
        return friends.size
    }

    override fun onBindViewHolder(holder: FriendsHolder, position: Int) {
        val friend = friends[position]
        holder.apply {
            // Default image
            friendImage.setImageResource(R.drawable.fui_ic_check_circle_black_128dp)
            friendName.text = friend.name
            friendLevel.text = "Level: " + friend.level.toString()

            // refactor later, add listners to buttons and display buttons for pending requests
            if(friend.status == "pending"){
                btnAccept.visibility = View.VISIBLE
                btnAccept.setOnClickListener{
                    db.collection("users")
                        .document(userID!!).collection("friends").document(friend.id).update("status", "friend")
                    db.collection("users")
                        .document(friend.id).collection("friends").document(userID!!).set(
                            hashMapOf("status" to "friend")
                        )
                }
                btnReject.visibility = View.VISIBLE
                btnReject.setOnClickListener { db.collection("users")
                    .document(userID!!).collection("friends").document(friend.id).delete() }

            } else if(friend.status ==""){
                btnAdd.visibility = View.VISIBLE
                btnAdd.setOnClickListener {
                    db.collection("users")
                        .document(friend.id).collection("friends").document(userID!!).set(
                            hashMapOf("status" to "pending")
                        )
                }
            }
        }
    }

}
