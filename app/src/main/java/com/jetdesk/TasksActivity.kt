package com.jetdesk

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TasksActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var createTaskButton: Button
    private lateinit var tasksAdapter: TasksAdapter
    private val tasksList = mutableListOf<Task>()

    private val clickUpAuthToken = "81853189_87110273f3e75c210ac57a2d8d1d0336eea54e1508fe5f3bf246af0b68b51c8a" // Replace this with the actual token
    private val listId: String by lazy {
        intent.getStringExtra("LIST_ID") ?: ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tasks)

        // Log to check the received listId
        Log.d("TasksActivity", "Received listId: $listId")

        if (listId.isEmpty()) {
            Log.e("TasksActivity", "List ID is empty. Cannot proceed.")
            Toast.makeText(this, "Invalid list. Please try again.", Toast.LENGTH_SHORT).show()
            finish() // Close the activity if listId is invalid
            return
        }

        // Initialize UI components
        recyclerView = findViewById(R.id.tasksRecyclerView)
        createTaskButton = findViewById(R.id.createTaskButton)

        recyclerView.layoutManager = LinearLayoutManager(this)
        tasksAdapter = TasksAdapter(tasksList) { task ->
            deleteTask(task)
        }
        recyclerView.adapter = tasksAdapter

        // Set up button to create a new task
        createTaskButton.setOnClickListener {
            showCreateTaskDialog()
        }

        // Fetch tasks for the list
        fetchTasks()
    }

    private fun fetchTasks() {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.clickup.com/api/v2/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val clickUpApi = retrofit.create(ClickUpApi::class.java)

        clickUpApi.getTasks(clickUpAuthToken, listId).enqueue(object : Callback<TasksResponse> {
            override fun onResponse(call: Call<TasksResponse>, response: Response<TasksResponse>) {
                if (response.isSuccessful) {
                    val tasks = response.body()?.tasks ?: emptyList()
                    tasksList.clear()
                    tasksList.addAll(tasks)
                    tasksAdapter.notifyDataSetChanged()
                } else {
                    Log.e("TasksActivity", "Failed to fetch tasks: ${response.code()} - ${response.errorBody()?.string()}")
                    Toast.makeText(this@TasksActivity, "Failed to fetch tasks", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TasksResponse>, t: Throwable) {
                Log.e("TasksActivity", "Error fetching tasks", t)
                Toast.makeText(this@TasksActivity, "Error fetching tasks", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showCreateTaskDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Create New Task")

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_task, null)

        val nameInput = dialogView.findViewById<EditText>(R.id.taskNameInput)
        val descriptionInput = dialogView.findViewById<EditText>(R.id.taskDescriptionInput)
        val assigneeInput = dialogView.findViewById<EditText>(R.id.taskAssigneeInput)
        val startDateInput = dialogView.findViewById<EditText>(R.id.taskStartDateInput)
        val endDateInput = dialogView.findViewById<EditText>(R.id.taskEndDateInput)
        val prioritySpinner = dialogView.findViewById<Spinner>(R.id.taskPrioritySpinner)
        val statusSpinner = dialogView.findViewById<Spinner>(R.id.taskStatusSpinner)

        // Set up Priority Spinner
        val priorityAdapter = ArrayAdapter.createFromResource(this, R.array.priority_options, android.R.layout.simple_spinner_item)
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        prioritySpinner.adapter = priorityAdapter

        // Set up Status Spinner
        val statusAdapter = ArrayAdapter.createFromResource(this, R.array.status_options, android.R.layout.simple_spinner_item)
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        statusSpinner.adapter = statusAdapter

        // Date Picker for Start Date
        startDateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(this, { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                startDateInput.setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            datePicker.show()
        }

        // Date Picker for End Date
        endDateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(this, { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                endDateInput.setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            datePicker.show()
        }

        builder.setView(dialogView)

        builder.setPositiveButton("Create") { dialog, _ ->
            val name = nameInput.text.toString()
            val description = descriptionInput.text.toString()
            val assignee = assigneeInput.text.toString()
            val startDate = startDateInput.text.toString()
            val endDate = endDateInput.text.toString()
            val priority = prioritySpinner.selectedItem.toString().toIntOrNull()
            val status = statusSpinner.selectedItem.toString()

            if (name.isNotEmpty()) {
                val assigneesList = if (assignee.isNotEmpty()) listOf(assignee) else null
                createTask(
                    name,
                    description,
                    assigneesList,
                    status,
                    startDate,
                    endDate,
                    priority
                )
            } else {
                Toast.makeText(this, "Please enter a task name", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun createTask(
        name: String,
        description: String?,
        assignees: List<String>?,
        status: String,
        startDate: String,
        endDate: String,
        priority: Int?
    ) {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.clickup.com/api/v2/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val clickUpApi = retrofit.create(ClickUpApi::class.java)
        val newTask = TaskRequest(
            name = name,
            description = description,
            assignees = assignees,
            status = status,
            start_date = convertDateToEpoch(startDate),
            due_date = convertDateToEpoch(endDate),
            priority = priority
        )

        Log.d("TasksActivity", "Attempting to create task with name: $name in listId: $listId")

        clickUpApi.createTask(clickUpAuthToken, listId, newTask).enqueue(object : Callback<TaskResponse> {
            override fun onResponse(call: Call<TaskResponse>, response: Response<TaskResponse>) {
                if (response.isSuccessful) {
                    val createdTask = response.body()
                    createdTask?.let {
                        Toast.makeText(this@TasksActivity, "Task created: ${it.name}", Toast.LENGTH_SHORT).show()
                        fetchTasks() // Refresh the list of tasks
                    }
                } else {
                    Log.e("TasksActivity", "Failed to create task: ${response.code()} - ${response.errorBody()?.string()}")
                    Toast.makeText(this@TasksActivity, "Failed to create task", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TaskResponse>, t: Throwable) {
                Log.e("TasksActivity", "Error creating task", t)
                Toast.makeText(this@TasksActivity, "Error creating task", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun deleteTask(task: Task) {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.clickup.com/api/v2/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val clickUpApi = retrofit.create(ClickUpApi::class.java)

        clickUpApi.deleteTask(clickUpAuthToken, task.id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    tasksList.remove(task)
                    tasksAdapter.notifyDataSetChanged()
                    Toast.makeText(this@TasksActivity, "Task deleted", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("TasksActivity", "Failed to delete task: ${response.code()} - ${response.errorBody()?.string()}")
                    Toast.makeText(this@TasksActivity, "Failed to delete task", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("TasksActivity", "Error deleting task", t)
                Toast.makeText(this@TasksActivity, "Error deleting task", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun convertDateToEpoch(date: String): Long? {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val parsedDate = sdf.parse(date)
            parsedDate?.time
        } catch (e: Exception) {
            Log.e("TasksActivity", "Failed to parse date: $date", e)
            null
        }
    }
}
