package com.walkly.walkly.ui.profile

import android.opengl.Visibility
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import com.walkly.walkly.R
import com.walkly.walkly.models.Friend
import com.walkly.walkly.repositories.FriendsRepository
import kotlinx.android.synthetic.main.fragment_friends.*
import kotlinx.android.synthetic.main.list_friend.*

private const val TAG = "FriendsFragment"

class FriendsFragment : Fragment() {

//    // Imported db stuff for the onClick method, this should be moved outside the fragment
//    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val userID = FirebaseAuth.getInstance().currentUser?.uid

//    private lateinit var friendsRecyclerView: RecyclerView
//    private lateinit var searchField: EditText
//    private lateinit var errorMessage: TextView

    private lateinit var adapter: FriendsAdapter

//    private val friendsViewModel: FriendsViewModel by lazy {
//        ViewModelProviders.of(this).get(FriendsViewModel::class.java)
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_friends, container, false)
//        friendsRecyclerView = view.findViewById(R.id.friends_recycler_view)
//        searchField = view.findViewById(R.id.et_search)
//        errorMessage = view.findViewById(R.id.error_no_user_found)
//        errorMessage.visibility = View.GONE
//
//        friendsViewModel.friendsList.observe(this, Observer { list ->
//            list?.let {
//                if (list.isEmpty()) {
//                    errorMessage.text = getString(R.string.no_friends)
//                    errorMessage.visibility = View.VISIBLE
//                } else {
//                    errorMessage.visibility = View.GONE
//                    adapter = FriendsAdapter(list)
//                    friendsRecyclerView.adapter = adapter
//                }
//            }
//        })
//
//        friendsViewModel.searchList.observe(this, Observer {list ->
//            list?.let {
//                if (list.isEmpty()) {
//                    errorMessage.text = getString(R.string.no_user_found)
//                    errorMessage.visibility = View.VISIBLE
//                } else {
//                    errorMessage.visibility = View.GONE
//                    adapter = FriendsAdapter(list)
//                    friendsRecyclerView.adapter = adapter
//                }
//            }
//        })
//
//        searchField.setOnEditorActionListener { v, actionId, event ->
//            when (actionId) {
//                EditorInfo.IME_ACTION_SEARCH -> {
//                    val userName = v.text.toString()
//                    if (userName.isNotBlank()) {
//                        friendsViewModel.searchUser(userName)
//                    }  else {
//                        friendsViewModel.getFriends()
//                    }
//
//                    true
//                } else -> false
//            }
//        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        FriendsRepository.getFriends {
            adapter = FriendsAdapter(it)
            friends_recycler_view.adapter = adapter
        }
        et_search.setOnEditorActionListener { v, actionId, event ->
            val query = et_search.text.toString()
            if (query != "") {
                FriendsRepository.search(query){
                    adapter.friends = it
                    adapter.notifyDataSetChanged()
                }
            } else {
                FriendsRepository.getFriends {
                    adapter.friends = it
                    adapter.notifyDataSetChanged()
                }
            }
            return@setOnEditorActionListener true
        }
    }

//    private fun addFriend(friendId: String){
//        db.collection("users")
//            .document(userID!!).collection("friends").document(friendId).update("status", "friend")
//    }

}
