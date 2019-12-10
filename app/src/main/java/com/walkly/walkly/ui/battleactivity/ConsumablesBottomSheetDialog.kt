package com.walkly.walkly.ui.battleactivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.walkly.walkly.R
import com.walkly.walkly.databinding.ConsumablesBottomSheetBinding

class ConsumablesBottomSheetDialog : BottomSheetDialogFragment() {

    private lateinit var battleActivityViewModel: BattleActivityViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding: ConsumablesBottomSheetBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.consumables_bottom_sheet,
            null,
            false
        )

        binding.consumableRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        val adapter = ConsumableAdapter()
        binding.consumableRecyclerView.adapter = adapter

        battleActivityViewModel = ViewModelProviders.of(this).get(BattleActivityViewModel::class.java)
        battleActivityViewModel.consumables.observe(this, Observer { list ->
            list?.let {
                binding.progressBar.visibility = View.GONE

                if (list.isEmpty()) {
                    binding.errorMessage.visibility = View.VISIBLE
                } else {
                    adapter.submitList(list)
                }
            }
        })

        return binding.root
    }
}