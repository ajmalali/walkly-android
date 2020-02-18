package com.walkly.walkly.ui.profile

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView

import com.walkly.walkly.R
import com.walkly.walkly.models.Friend

private const val TAG = "FriendsFragment"

class FriendsFragment : Fragment() {

    private lateinit var friendsRecyclerView: RecyclerView
    private lateinit var searchField: EditText

    private var adapter: FriendsAdapter? = null

    private val friendsViewModel: FriendsViewModel by lazy {
        ViewModelProviders.of(this).get(FriendsViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_friends, container, false)
        friendsRecyclerView = view.findViewById(R.id.friends_recycler_view)
        searchField = view.findViewById(R.id.et_search)

        friendsViewModel.friendsList.observe(this, Observer { list ->
            list?.let {
                adapter = FriendsAdapter(list)
                friendsRecyclerView.adapter = adapter
            }
        })

        friendsViewModel.searchList.observe(this, Observer {list ->
            list?.let {
                adapter = FriendsAdapter(list)
                friendsRecyclerView.adapter = adapter
            }
        })

        searchField.setOnEditorActionListener { v, actionId, event ->
            when (actionId) {
                EditorInfo.IME_ACTION_SEARCH -> {
                    val userName = v.text.toString()
                    if (userName.isNotBlank()) {
                        friendsViewModel.searchUser(userName)
                    }  else {
                        friendsViewModel.getFriends()
                    }

                    true
                } else -> false
            }
        }

        return view
    }

    private inner class FriendsHolder(view: View): RecyclerView.ViewHolder(view) {
        val friendName: TextView = itemView.findViewById(R.id.tv_username)
        val friendLevel: TextView = itemView.findViewById(R.id.tv_level)
        val friendPoints: TextView = itemView.findViewById(R.id.tv_points)
        val friendImage: ImageView = itemView.findViewById(R.id.img_avatar)
    }

    private inner class FriendsAdapter(var friends: List<Friend>): RecyclerView.Adapter<FriendsHolder>() {
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
                friendImage.setImageResource(R.drawable.fui_ic_anonymous_white_24dp)
                friendName.text = friend.name
                friendLevel.text = friend.level.toString()
                friendPoints.text = friend.points.toString()
            }
        }

    }
}
