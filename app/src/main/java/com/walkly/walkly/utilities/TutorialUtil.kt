package com.walkly.walkly.utilities

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.walkly.walkly.R


class TutorialUtil (val layout: ViewGroup, val activity: Activity) {
    private var PRIVATE_MODE = 0
    private val PREF_NAME = "main-tut-flag"
    private val sharedPref: SharedPreferences = activity.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
    private var layoutInf: LayoutInflater
    private val tutorial_layout = R.layout.tutorial_box // referene for the tutorial layout
    private var tutorial_view: View
    private var textboxlayout: ConstraintLayout
    private var textbox: TextView
    private lateinit var text_array: Array<String>
    private var count = 0
    private var flag_ = false

    init {
        layoutInf = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        tutorial_view = layoutInf.inflate(tutorial_layout, null, false)
        textboxlayout = tutorial_view.findViewById(R.id.tutorial_text_container) as ConstraintLayout
        textbox = tutorial_view.findViewById(R.id.textbox) as TextView
    }

    fun startTutorial(tutorialName: String, flag: Int){
        flag_ = loadData(flag)
        val tutorial_text_id: Int = activity.resources.getIdentifier(tutorialName, "array", activity.packageName)
        text_array = activity.resources.getStringArray(tutorial_text_id)
        textbox.text = text_array[count]

        if(!flag_){
            layout.addView(tutorial_view)
            textboxlayout.setOnClickListener {
                count++
                if(count < text_array.size) {
                    textbox.text = text_array[count]
                } else{
                    this.
                    stopTutorial()
                    saveData(flag)
                }
            }
        }
    }

    fun stopTutorial(){
        layout.removeView(tutorial_view)
        count = 0
    }

    private fun loadData(flag: Int): Boolean {
        val defaultValue = activity.resources.getBoolean(flag)
        val saved_flag = sharedPref.getBoolean(activity.getString(flag), defaultValue)
        return saved_flag
    }

    private fun saveData(flag: Int){
        with (sharedPref.edit()) {
            putBoolean(activity.getString(R.bool.main_bool), true)
            commit()
        }
    }
}