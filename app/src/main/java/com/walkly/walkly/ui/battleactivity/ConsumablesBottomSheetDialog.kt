package com.walkly.walkly.ui.battleactivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.walkly.walkly.R
import com.walkly.walkly.databinding.ConsumablesBottomSheetBinding
import com.walkly.walkly.models.Consumable

class ConsumablesBottomSheetDialog(val fragment: BattleActivityFragment) : BottomSheetDialogFragment(), ConsumableAdapter.OnConsumableUseListener {

    private lateinit var binding: ConsumablesBottomSheetBinding
    private lateinit var adapter: ConsumableAdapter
    private var consumableList = mutableListOf<Consumable>()
    private lateinit var battleActivityViewModel: BattleActivityViewModel

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

        adapter = ConsumableAdapter(consumableList, this)
        binding.consumableRecyclerView.adapter = adapter
        binding.progressBar.visibility = View.VISIBLE

        battleActivityViewModel = ViewModelProviders.of(fragment).get(BattleActivityViewModel::class.java)

        return binding.root
    }

    // TODO: FIX
    fun updateList(list: List<Consumable>?) {
        binding.progressBar.visibility = View.GONE
        if (consumableList.size != list!!.size) {
            consumableList = list.toMutableList()
            adapter.consumableList = consumableList
            adapter.notifyDataSetChanged()
        }
    }

    override fun onConsumableClick(position: Int) {
        battleActivityViewModel.selectConsumable(consumableList[position])
        dismiss()
    }
}