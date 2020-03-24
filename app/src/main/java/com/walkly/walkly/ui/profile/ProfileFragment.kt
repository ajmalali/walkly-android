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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.walkly.walkly.R
import com.walkly.walkly.auth.LoginActivity
import com.walkly.walkly.models.Player
import com.walkly.walkly.repositories.EquipmentRepository.equipmentList
import kotlinx.android.synthetic.main.dialog_wear_equipment.view.*
import kotlinx.android.synthetic.main.fragment_profile.*


@SuppressLint("Registered")
class ProfileFragment : Fragment(), EquipmentAdapter.OnEquipmentUseListener {
    lateinit var v: View
    private lateinit var auth: FirebaseAuth

    private lateinit var wearEquipmentDialog: AlertDialog
    private lateinit var wearEquipmentBuilder: AlertDialog.Builder
    private val adapter = EquipmentAdapter(equipmentList,this)
    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_profile, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // showing user name
        val userName = auth.currentUser?.displayName

        if (userName != null) {
            tv_welcome.text = "Hi $userName"
        } else {
            Log.e(TAG, "failed to retrieve user name")
            tv_welcome.text = "Hello"
        }

        //Wear Equipment Dialog

        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_wear_equipment, null) as View
        wearEquipmentBuilder = AlertDialog.Builder(this.context)
            .setView(dialogView)

        profileViewModel = ViewModelProviders.of(this)
            .get(ProfileViewModel::class.java)

        val rv = dialogView.findViewById(R.id.equipment_recycler_view) as RecyclerView
        rv.layoutManager = GridLayoutManager(context,2,GridLayoutManager.HORIZONTAL,false)

        profileViewModel.equipments.observe(viewLifecycleOwner, Observer { list ->
            dialogView.progressBar.visibility = View.GONE
            if (list.isEmpty()) {
                Log.e("here","EMPTYYY")
            } else {
                Log.d("here",list.toString())
                adapter.equipmentList = list
                if(list.size < 5){
                    rv.layoutManager = GridLayoutManager(context,2,GridLayoutManager.VERTICAL,false)
                }else{
                    rv.layoutManager = GridLayoutManager(context,2,GridLayoutManager.HORIZONTAL,false)
                }
                adapter.notifyDataSetChanged()
            }
        })


        rv.adapter = adapter
        wearEquipmentDialog = wearEquipmentBuilder.create()


        // click listeners

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
        // TODO refactor

        Glide.with(this)
            .load(
                auth.currentUser?.photoUrl
            )
            .into(img_avatar)

        var equipmentUri: Uri
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(auth.uid!!)
            .get()
            .addOnSuccessListener {
                val equipmentId = it.data?.get("equipped_weapon") as String
                 FirebaseStorage.getInstance()
                    .getReference("equipments/${equipmentId}.png")
                    .downloadUrl
                     .addOnSuccessListener {
                         equipmentUri = it
                         Glide.with(this)
                             .load(equipmentUri)
                             .into(img_equipment)
                     }
            }
    }



    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        updateUI()
    }

    private fun updateUI() {

        val intent = Intent(activity, LoginActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }

    override fun onEquipmentClick(position: Int) {
        val equipment = adapter.equipmentList[position]
        profileViewModel.selectEquipment(equipment)
        Player.equipment.value = equipment
        wearEquipmentDialog.dismiss()
        }

    companion object{
        private const val TAG = "profile_fragment"
    }
}