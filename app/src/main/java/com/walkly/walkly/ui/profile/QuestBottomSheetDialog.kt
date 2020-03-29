package com.walkly.walkly.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.walkly.walkly.R
import com.walkly.walkly.models.Quest
import com.walkly.walkly.repositories.QuestsRepository
import kotlinx.android.synthetic.main.bottom_sheet_quest.*

class QuestBottomSheetDialog(val quest: Quest, val callback: (Quest) -> Unit ) : BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_quest, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tv_quest_hint.text = quest.hint
        btn_complete.setOnClickListener {
            if (quest.closeEnough) {
                QuestsRepository.completeQuest(quest) {
                    if (it) {
                        callback(quest)
                        dismiss()
                    }
                }
            } else {
                Toast.makeText(context,"You need to be withing 10 meters to complete the quest", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }
}