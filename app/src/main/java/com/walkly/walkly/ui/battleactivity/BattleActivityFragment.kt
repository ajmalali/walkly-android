package com.walkly.walkly.ui.battleactivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.walkly.walkly.R
import com.walkly.walkly.databinding.FragmentBattleActivityBinding
import kotlinx.android.synthetic.main.fragment_battle_activity.*

class BattleActivityFragment : Fragment() {
    private lateinit var battleActivityViewModel: BattleActivityViewModel
    private val consumablesBottomSheet = ConsumablesBottomSheetDialog()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding: FragmentBattleActivityBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_battle_activity, container, false)
        binding.lifecycleOwner = this

        battleActivityViewModel = ViewModelProviders.of(this).get(BattleActivityViewModel::class.java)
        battleActivityViewModel.consumables.observe(this, Observer { list ->
            list?.let {

            }
        })

        binding.useItems.setOnClickListener {
            consumablesBottomSheet.show(fragmentManager, "ConsumablesBottomSheet")
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        leaveBattle.setOnClickListener {
            view.findNavController()
                .navigate(R.id.action_Battle_Activity_Fragment_to_navigation_map)
        }
    }

}