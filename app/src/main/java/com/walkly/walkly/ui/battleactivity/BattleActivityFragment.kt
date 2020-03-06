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
import com.walkly.walkly.offlineBattle.ConsumablesBottomSheetDialog
import kotlinx.android.synthetic.main.fragment_battle_activity.*

private const val TAG = "BattleActivityFragment"

class BattleActivityFragment : Fragment() {
    private lateinit var battleActivityViewModel: BattleActivityViewModel
    private lateinit var consumablesBottomSheet: ConsumablesBottomSheetDialog
    private lateinit var binding: FragmentBattleActivityBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_battle_activity, container, false)
        binding.lifecycleOwner = this

//        consumablesBottomSheet = ConsumablesBottomSheetDialog(this)

        battleActivityViewModel = ViewModelProviders.of(this).get(BattleActivityViewModel::class.java)

        binding.useItems.setOnClickListener {
            consumablesBottomSheet.show(fragmentManager!!, consumablesBottomSheet.tag)
        }

        battleActivityViewModel.selectedConsumable.observe(this, Observer {
            useConsumable(it.type, it.value)
            battleActivityViewModel.removeSelectedConsumable()
        })

        return binding.root
    }

    private fun useConsumable(consumableType: String, consumableValue: Int) {
        when (consumableType.toLowerCase()) {
            "attack" -> binding.enemyHealthBar.progress -= consumableValue
            "health" -> binding.playerHealthBar.progress += consumableValue
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        leaveBattle.setOnClickListener {
            view.findNavController()
                .navigate(R.id.action_Battle_Activity_Fragment_to_navigation_map)
        }
    }
}