package com.walkly.walkly.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.walkly.walkly.R
import com.walkly.walkly.models.Quest
import kotlinx.android.synthetic.main.fragment_quests.*
import kotlinx.android.synthetic.main.list_quest.view.*
import java.util.ArrayList

class QuestsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_quests, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        quests_recycler_view.adapter = QuestAdapter(
            ArrayList<Quest>().apply {
                add(Quest("Largest Mosque", 500))
                add(Quest("Tallest Red Building", 1750))
            }
        )
    }
}


class QuestAdapter(private val quests: List<Quest>) : RecyclerView.Adapter<QuestViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_quest, parent, false)
        return QuestViewHolder(view)
    }

    override fun getItemCount() = quests.size

    override fun onBindViewHolder(holder: QuestViewHolder, position: Int) {
        holder.bind(quests[position])
    }

}

class QuestViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val questName: TextView = view.tv_quest_name
    private val questDistance: TextView = view.tv_quest_distance

    fun bind(quest: Quest){
        questName.text = quest.name
        questDistance.text = meter2text(quest.distance)
    }
    private fun meter2text(distance: Int) : String{
        if (distance > 1000){
            val d = distance / 1000.0
            return "$d KM"
        } else {
            return "$distance Meter"
        }
    }
}

