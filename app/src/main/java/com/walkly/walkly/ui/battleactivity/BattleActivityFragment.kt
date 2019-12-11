package com.walkly.walkly.ui.battleactivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.walkly.walkly.R
import kotlinx.android.synthetic.main.fragment_battle_activity.*

class BattleActivityFragment: Fragment() {
    lateinit var v: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_battle_activity, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        leaveBattle.setOnClickListener {
            view.findNavController().navigate(R.id.action_Battle_Activity_Fragment_to_navigation_map)
        }
    }

}