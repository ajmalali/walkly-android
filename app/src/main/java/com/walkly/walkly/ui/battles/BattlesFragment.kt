package com.walkly.walkly.ui.battles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.walkly.walkly.R
import kotlinx.android.synthetic.main.fragment_battles.*


class BattlesFragment : Fragment() {

    private lateinit var battlesViewModel: BattlesViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        battlesViewModel =
            ViewModelProviders.of(this).get(BattlesViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_battles, container, false)
        /*val textView: TextView = root.findViewById(R.id.text_notifications)
        battlesViewModel.text.observe(this, Observer {
            textView.text = it
        })*/
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var list1 = arrayOf(
            "One",
            "Two",
            "Three",
            "Four",
            "Five"
        )
        var list2 = arrayOf(
            "One",
            "Two",
            "Three"
        )

        var adapter1 = ArrayAdapter (activity, R.layout.battleboard_item, R.id.label, list1)
        var adapter2 = ArrayAdapter (activity, R.layout.battleboard_item, R.id.label, list2)


        battlesQuestsRadioButtons.setOnCheckedChangeListener { group, checkedId ->
            if (battlesButton.isChecked) {
                textView1.text = "Battles"
                list_view.adapter = adapter1
            } else if (questsButton.isChecked) {
                textView1.text = "Quests"
                list_view.adapter = adapter2
            }
        }
    }
}