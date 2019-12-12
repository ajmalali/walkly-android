package com.walkly.walkly.ui.battleactivity

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.RecyclerView
import com.walkly.walkly.R
import com.walkly.walkly.models.Consumable

class ConsumableAdapter(var consumableList: List<Consumable>, private val listener: OnConsumableUseListener) : RecyclerView.Adapter<ConsumableAdapter.ConsumableViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConsumableViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)
        val view: View = layoutInflater.inflate(R.layout.consumable, parent, false)

        return ConsumableViewHolder(view, listener)
    }

    override fun onBindViewHolder(holder: ConsumableViewHolder, position: Int) {
        val item = consumableList[position]
        holder.consumableImage.setImageResource(R.drawable.consumable1)
        holder.consumableName.text = item.name
        holder.consumableType.text = item.type
        val color =  when (item.type) {
            "attack" -> Color.parseColor("#cc0000")
            "health" -> Color.parseColor("#00cc00")
            else -> Color.BLACK
        }
        holder.consumableType.setTextColor(color)
        holder.consumableValue.setTextColor(color)
        holder.consumableValue.text = item.value.toString()
    }

    class ConsumableViewHolder(itemView: View, private val listener: OnConsumableUseListener): RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val consumableName: TextView = itemView.findViewById(R.id.consumable_name)
        val consumableType: TextView = itemView.findViewById(R.id.consumable_type)
        val consumableValue: TextView = itemView.findViewById(R.id.consumable_value)
        val consumableImage: ImageView = itemView.findViewById(R.id.consumable_image)


        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            listener.onConsumableClick(adapterPosition)
        }
    }

    override fun getItemCount(): Int {
       return consumableList.size
    }

    interface OnConsumableUseListener {
        fun onConsumableClick(position: Int)
    }
}
