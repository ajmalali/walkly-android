package com.walkly.walkly.ui.battleactivity

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.walkly.walkly.R
import com.walkly.walkly.databinding.ConsumableBinding
import com.walkly.walkly.models.Consumable


class ConsumableAdapter : ListAdapter<Consumable, ConsumableAdapter.ConsumableViewHolder>(ConsumableCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConsumableViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ConsumableBinding.inflate(layoutInflater, parent, false)

        return ConsumableViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ConsumableViewHolder, position: Int) {
        val item = getItem(position)
        holder.consumableImage.setImageResource(R.drawable.consumable1)
        holder.consumableName.text = item.name
        holder.consumableType.text = item.type
        holder.consumableValue.text = item.value.toString()
    }

    class ConsumableViewHolder(binding: ConsumableBinding) : RecyclerView.ViewHolder(binding.root) {
        val consumableName: TextView = binding.consumableName
        val consumableType: TextView = binding.consumableType
        val consumableValue: TextView = binding.consumableValue
        val consumableImage: ImageView = binding.consumableImage
    }

    // TODO: Change name check to ID check
    class ConsumableCallback : DiffUtil.ItemCallback<Consumable>() {
        override fun areItemsTheSame(oldItem: Consumable, newItem: Consumable): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(
            oldItem: Consumable,
            newItem: Consumable
        ): Boolean {
            return oldItem == newItem
        }

    }

}
