package com.walkly.walkly.ui.battleactivity

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.walkly.walkly.R
import com.walkly.walkly.databinding.ConsumablesBottomSheetBinding
import com.walkly.walkly.models.Consumable

class ConsumablesBottomSheetDialog : BottomSheetDialogFragment() {

    private lateinit var binding: ConsumablesBottomSheetBinding
    private lateinit var adapter: ConsumableAdapter
    private var consumableList = mutableListOf<Consumable>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.consumables_bottom_sheet,
            null,
            false
        )

        binding.consumableRecyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        adapter = ConsumableAdapter()
        binding.consumableRecyclerView.adapter = adapter

        return binding.root
    }

    // TODO: FIX
    fun updateList(list: List<Consumable>?) {
        if (consumableList.size != list!!.size) {
            consumableList = list.toMutableList()
            binding.progressBar.visibility = View.GONE
            adapter.submitList(consumableList)
        }
    }
}