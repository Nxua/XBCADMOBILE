package com.jetdesk

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FoldersAdapter(private val folders: List<Folder>, private val onFolderClicked: (Folder) -> Unit) :
    RecyclerView.Adapter<FoldersAdapter.FolderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.folder_item, parent, false)
        return FolderViewHolder(view, onFolderClicked)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        holder.bind(folders[position])
    }

    override fun getItemCount(): Int = folders.size

    class FolderViewHolder(itemView: View, private val onFolderClicked: (Folder) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val folderName: TextView = itemView.findViewById(R.id.folderName)

        fun bind(folder: Folder) {
            folderName.text = folder.name
            itemView.setOnClickListener { onFolderClicked(folder) }
        }
    }
}
