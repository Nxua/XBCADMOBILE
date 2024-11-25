package com.jetdesk

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ListsAdapter(private val lists: List<ClickUpList>, private val onListClicked: (ClickUpList) -> Unit) :
    RecyclerView.Adapter<ListsAdapter.ListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return ListViewHolder(view, onListClicked)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.bind(lists[position])
    }

    override fun getItemCount(): Int = lists.size

    class ListViewHolder(itemView: View, private val onListClicked: (ClickUpList) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val listName: TextView = itemView.findViewById(R.id.listName)

        fun bind(list: ClickUpList) {
            listName.text = list.name
            itemView.setOnClickListener { onListClicked(list) }
        }
    }
}