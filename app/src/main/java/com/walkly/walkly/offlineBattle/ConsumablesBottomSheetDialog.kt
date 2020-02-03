package com.walkly.walkly.offlineBattle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.walkly.walkly.R
import com.walkly.walkly.databinding.ConsumablesBottomSheetBinding
import com.walkly.walkly.models.Consumable
import com.walkly.walkly.offlineBattle.ConsumableAdapter
import com.walkly.walkly.offlineBattle.OfflineBattle
import com.walkly.walkly.offlineBattle.OfflineBattleViewModel

class ConsumablesBottomSheetDialog(val activity: AppCompatActivity) : BottomSheetDialogFragment(), ConsumableAdapter.OnConsumableUseListener {

    private lateinit var binding: ConsumablesBottomSheetBinding
    private lateinit var adapter: ConsumableAdapter
    private var consumableList = mutableListOf<Consumable>()
    private lateinit var battleActivityViewModel: OfflineBattleViewModel

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

        adapter = ConsumableAdapter(
            consumableList,
            this
        )
        binding.consumableRecyclerView.adapter = adapter

        binding.progressBar.visibility = View.VISIBLE

//        battleActivityViewModel = ViewModelProviders.of(activity).get(BattleActivityViewModel::class.java)

        battleActivityViewModel = (activity as OfflineBattle).viewModel

        battleActivityViewModel.consumables.observe(this, Observer { list ->
            binding.progressBar.visibility = View.GONE
            binding.errorMessage.visibility = View.GONE

            if (list.isEmpty()) {
                binding.errorMessage.visibility = View.VISIBLE
            } else {
                adapter.consumableList = list
                adapter.notifyDataSetChanged()
            }
        })

        return binding.root
    }

    override fun onConsumableClick(position: Int) {
        val consumable = adapter.consumableList[position]
        battleActivityViewModel.selectConsumable(consumable)
        dismiss()
    }
}