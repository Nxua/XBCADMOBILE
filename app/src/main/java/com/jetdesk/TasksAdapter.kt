package com.jetdesk

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TasksAdapter(private val tasks: MutableList<Task>, private val deleteTaskCallback: (Task) -> Unit) :
    RecyclerView.Adapter<TasksAdapter.TaskViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_item, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.bind(task)

        holder.deleteButton.setOnClickListener {
            deleteTaskCallback(task)
        }
    }

    override fun getItemCount(): Int = tasks.size

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val taskName: TextView = itemView.findViewById(R.id.taskName)
        private val taskDescription: TextView = itemView.findViewById(R.id.taskDescription)
        private val taskPriority: TextView = itemView.findViewById(R.id.taskPriority)
        val deleteButton: Button = itemView.findViewById(R.id.deleteTaskButton)

        fun bind(task: Task) {
            taskName.text = task.name
            taskDescription.text = task.description ?: "No description"
            taskPriority.text = task.priority?.priority ?: "No priority set"
        }
    }
}