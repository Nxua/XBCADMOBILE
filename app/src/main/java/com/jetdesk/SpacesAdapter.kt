package com.jetdesk

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SpacesAdapter(private val spaces: List<Space>, private val onSpaceClicked: (Space) -> Unit) :
    RecyclerView.Adapter<SpacesAdapter.SpaceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpaceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.space_item, parent, false)
        return SpaceViewHolder(view, onSpaceClicked)
    }

    override fun onBindViewHolder(holder: SpaceViewHolder, position: Int) {
        holder.bind(spaces[position])
    }

    override fun getItemCount(): Int = spaces.size

    class SpaceViewHolder(itemView: View, private val onSpaceClicked: (Space) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val spaceName: TextView = itemView.findViewById(R.id.spaceName)

        fun bind(space: Space) {
            spaceName.text = space.name
            itemView.setOnClickListener { onSpaceClicked(space) }
        }
    }
}
