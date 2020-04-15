package com.walkly.walkly.ui.profile

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.walkly.walkly.R
import com.walkly.walkly.models.Equipment
import kotlinx.android.synthetic.main.fragment_profile.*

class EquipmentAdapter(
    var equipmentList: List<Equipment>,
    private val listener: OnEquipmentUseListener
) : RecyclerView.Adapter<EquipmentAdapter.EquipmentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EquipmentViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)
        val view: View = layoutInflater.inflate(R.layout.equipment, parent, false)

        return EquipmentViewHolder(
            view,
            listener
        )
    }

    override fun onBindViewHolder(holder: EquipmentViewHolder, position: Int) {
        val item = equipmentList[position]
        Glide.with(holder.itemView.context)
            .load(item.image)
            .into(holder.equipmentImage)
        holder.equipmentName.text = item.name
        holder.equipmentType.text = item.type
        val color = when (item.type) {
            "attack" -> Color.parseColor("#cc0000")
            "health" -> Color.parseColor("#00cc00")
            else -> Color.BLACK
        }
        holder.equipmentType.setTextColor(color)
        holder.equipmentValue.setTextColor(color)
        holder.equipmentValue.text = item.value.toString()
    }

    class EquipmentViewHolder(itemView: View, private val listener: OnEquipmentUseListener) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val equipmentName: TextView = itemView.findViewById(R.id.equipment_name)
        val equipmentType: TextView = itemView.findViewById(R.id.equipment_type)
        val equipmentValue: TextView = itemView.findViewById(R.id.equipment_value)
        val equipmentImage: ImageView = itemView.findViewById(R.id.equipment_image)


        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            listener.onEquipmentClick(adapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return equipmentList.size
    }

    interface OnEquipmentUseListener {
        fun onEquipmentClick(position: Int)
    }
}