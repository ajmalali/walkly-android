package com.walkly.walkly.ui.battles

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.walkly.walkly.R
import com.walkly.walkly.models.Invite

class InvitesAdapter(
    var invites: List<Invite>,
    private val onInviteListener: OnInviteListener
) : RecyclerView.Adapter<InvitesAdapter.InviteHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InviteHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.list_host_battles, parent, false)
        return InviteHolder(view, onInviteListener)
    }

    override fun getItemCount(): Int {
        return invites.size
    }

    override fun onBindViewHolder(holder: InviteHolder, position: Int) {
        val invite = invites[position]
        holder.apply {
            // Default image
            val text = "Invite by ${invite.host}"
            hostName.text = text
            battleID = invite.id
        }
    }

    inner class InviteHolder(view: View, private val listener: OnInviteListener) :
        RecyclerView.ViewHolder(view),
        View.OnClickListener {
        val hostName: TextView = itemView.findViewById(R.id.tv_battle_name_host)
        var battleID: String = ""

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            listener.onInviteClick(adapterPosition)
        }
    }

    interface OnInviteListener {
        fun onInviteClick(position: Int)
    }
}