package com.walkly.walkly.ui.battleactivity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.walkly.walkly.R
import kotlinx.android.synthetic.main.bottom_sheet_layout.view.*

class BattleItemAdapter (val itemList: ArrayList<BattleItem>) : RecyclerView.Adapter<BattleItemAdapter.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent?.context).inflate(R.layout.items_list, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: BattleItem = itemList[position]

        holder.BattleItemImage.setImageResource(R.drawable.bitmoji1)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val BattleItemImage: ImageView = itemView.findViewById(R.id.battleItem1)

    }
}
