package com.walkly.walkly.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.walkly.walkly.R
import com.walkly.walkly.models.Quest
import com.walkly.walkly.repositories.QuestsRepository
import kotlinx.android.synthetic.main.fragment_quests.*
import kotlinx.android.synthetic.main.list_quest.view.*

class QuestsFragment : Fragment(), QuestAdapter.QuestClickListener {

    private lateinit var quests: MutableList<Quest>
    private lateinit var adapter: QuestAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_quests, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        QuestsRepository.getQuests {
            quests = it.toMutableList()
            adapter = QuestAdapter(quests, this)
            quests_recycler_view.adapter = adapter
        }
    }

    override fun onQuestClicker(postion: Int) {
        val bottomSheet = QuestBottomSheetDialog(
            quests[postion]
        ) {
            quests.remove(it)
            adapter.notifyDataSetChanged()
        }
        bottomSheet.show(parentFragmentManager, "qbs")
    }
}
class QuestAdapter(private val quests: List<Quest>, val clickListener: QuestClickListener) : RecyclerView.Adapter<QuestViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_quest, parent, false)
        return QuestViewHolder(view, clickListener)
    }

    override fun getItemCount() = quests.size

    override fun onBindViewHolder(holder: QuestViewHolder, position: Int) {
        holder.bind(quests[position])
    }
    interface QuestClickListener{
        fun onQuestClicker(postion: Int)
    }

}

class QuestViewHolder(view: View, val clickListener: QuestAdapter.QuestClickListener) : RecyclerView.ViewHolder(view), View.OnClickListener {
    private val questName: TextView = view.tv_quest_name
    private val questDistance: TextView = view.tv_quest_distance

    init {
        view.setOnClickListener(this)
    }

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

    override fun onClick(v: View?) {
        clickListener.onQuestClicker(adapterPosition)
    }
}



