package com.walkly.walkly.ui.profile

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.walkly.walkly.R
import com.walkly.walkly.auth.LoginActivity
import com.walkly.walkly.models.Consumable
import com.walkly.walkly.models.Equipment
import kotlinx.android.synthetic.main.dialog_wear_equipment.view.*
import kotlinx.android.synthetic.main.fragment_profile.*
import java.lang.NullPointerException


private const val TAG = "ProfileFragment"

class ProfileFragment : Fragment(), EquipmentAdapter.OnEquipmentUseListener {

    private lateinit var auth: FirebaseAuth

    private lateinit var wearEquipmentDialog: AlertDialog
    private lateinit var wearEquipmentBuilder: AlertDialog.Builder
    private lateinit var adapter: EquipmentAdapter

    private val profileViewModel: ProfileViewModel by viewModels()
    private var equipmentList = mutableListOf<Equipment>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // Showing user name
        val userName = auth.currentUser?.displayName
        val text: String
        if (userName != null) {
            text = "Hello $userName"
        } else {
            text = "Error: could not retrieve user name"
            tv_welcome.setTextColor(Color.RED)
        }

        tv_welcome.text = text

        // Wear Equipment Dialog
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_wear_equipment, null) as View
        wearEquipmentBuilder = AlertDialog.Builder(this.context)
            .setView(dialogView)

        val rv = dialogView.findViewById(R.id.equipment_recycler_view) as RecyclerView
        rv.layoutManager = GridLayoutManager(context, 2, GridLayoutManager.HORIZONTAL, false)

        profileViewModel.equipments.observe(viewLifecycleOwner, Observer { list ->
            dialogView.progressBar.visibility = View.GONE
            if (list.isEmpty()) {
                Log.e(TAG, "EMPTYYY")
            } else {
                Log.d(TAG, "$list")
                adapter.equipmentList = list
                if (list.size < 5) {
                    rv.layoutManager =
                        GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
                } else {
                    rv.layoutManager =
                        GridLayoutManager(context, 2, GridLayoutManager.HORIZONTAL, false)
                }
                adapter.notifyDataSetChanged()
            }
        })

        adapter = EquipmentAdapter(equipmentList, this)
        rv.adapter = adapter
        wearEquipmentDialog = wearEquipmentBuilder.create()

        // click listeners
        menu_item_quests.setOnClickListener {
            view.findNavController().navigate(R.id.action_navigation_profile_to_questsFragment)
        }

        menu_item_leaderboard.setOnClickListener {
            view.findNavController().navigate(R.id.action_navigation_home_to_leaderboardFragment)
        }

        menu_item_friends.setOnClickListener {
            view.findNavController().navigate(R.id.action_navigation_profile_to_friendsFragment)
        }

        menu_item_statistics.setOnClickListener {
            view.findNavController().navigate(R.id.action_navigation_profile_to_statistics)
        }
        btn_change_equipment.setOnClickListener {
            wearEquipmentDialog.show()
            //To make the background for the dialog Transparent
            wearEquipmentDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        menu_item_achievements.setOnClickListener {
            view.findNavController().navigate(R.id.action_navigation_profile_to_achievementFragment)

        }

        // TODO make it faster
        Glide.with(this)
            .load(profileViewModel.currentPlayer.photoURL)
            .into(img_avatar)


        // TODO: Refactor
        try {
            Glide.with(this)
                .load(profileViewModel.currentPlayer.currentEquipment?.image)
                .into(img_equipment)
        } catch (npe: NullPointerException){
            // do nothing
        }

//        var equipmentUri: Uri
//        FirebaseStorage.getInstance()
//            .getReference("equipments/${profileViewModel.currentPlayer.currentEquipment?.id}.png")
//            .downloadUrl
//            .addOnSuccessListener {
//                equipmentUri = it
//                Glide.with(this)
//                    .load(equipmentUri)
//                    .into(img_equipment)
//            }
    }

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(activity, LoginActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }

    override fun onEquipmentClick(position: Int) {
        val equipment = adapter.equipmentList[position]
        profileViewModel.selectEquipment(equipment)
        wearEquipmentDialog.dismiss()
    }
}