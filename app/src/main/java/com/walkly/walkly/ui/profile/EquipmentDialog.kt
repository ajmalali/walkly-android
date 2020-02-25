package com.walkly.walkly.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.GridLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.walkly.walkly.R
import com.walkly.walkly.databinding.DialogWearEquipmentBindingImpl
import com.walkly.walkly.databinding.EquipmentBinding
import com.walkly.walkly.models.Equipment
import com.walkly.walkly.offlineBattle.OfflineBattle
import com.walkly.walkly.ui.profile.EquipmentAdapter.*
import kotlinx.android.synthetic.main.dialog_wear_equipment.view.*

class EquipmentDialog: DialogFragment(), OnEquipmentUseListener{

    val TAG = "EDialog"
    private lateinit var binding: DialogWearEquipmentBindingImpl
    private lateinit var adapter: EquipmentAdapter
    private var equipmentList = mutableListOf<Equipment>()
    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.dialog_wear_equipment,
            null,
            false
        )
        binding.root.equipment_recycler_view.layoutManager = GridLayoutManager(context,2)


        adapter = EquipmentAdapter(
            equipmentList,
            this
        )
        binding.root.equipment_recycler_view.adapter = adapter

        profileViewModel = ViewModelProviders.of(this)
        .get(ProfileViewModel::class.java)

        profileViewModel.equipments.observe(this, Observer { list ->
            if (list.isEmpty()) {
                Log.e(TAG,"EMPTYYY")
            } else {
                adapter.equipmentList = list
                adapter.notifyDataSetChanged()
            }
        })

        return binding.root
    }

    override fun onEquipmentClick(position: Int) {
        val equipment = adapter.equipmentList[position]
        profileViewModel.selectEquipment(equipment)
        dismiss()    }


}