package com.jetdesk

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ListsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var createListButton: Button
    private lateinit var listsAdapter: ListsAdapter
    private val listsList = mutableListOf<ClickUpList>()

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private val clickUpAuthToken = "81853189_87110273f3e75c210ac57a2d8d1d0336eea54e1508fe5f3bf246af0b68b51c8a" // Replace this with the actual token
    private val folderId: String by lazy {
        intent.getStringExtra("FOLDER_ID") ?: ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lists)

        // Set up Toolbar
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Initialize DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView: NavigationView = findViewById(R.id.navigation_view)

        // Set up Drawer Toggle
        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Handle navigation item clicks
        navigationView.setNavigationItemSelectedListener { menuItem ->
            handleNavigation(menuItem)
            true
        }

        // Log to check the received folderId
        Log.d("ListsActivity", "Received folderId: $folderId")

        if (folderId.isEmpty()) {
            Log.e("ListsActivity", "Folder ID is empty. Cannot proceed.")
            Toast.makeText(this, "Invalid folder. Please try again.", Toast.LENGTH_SHORT).show()
            finish() // Close the activity if folderId is invalid
            return
        }

        // Initialize UI components
        recyclerView = findViewById(R.id.listsRecyclerView)
        createListButton = findViewById(R.id.createListButton)

        recyclerView.layoutManager = LinearLayoutManager(this)
        listsAdapter = ListsAdapter(listsList) { list ->
            val intent = Intent(this, TasksActivity::class.java)
            intent.putExtra("LIST_ID", list.id)
            startActivity(intent)
        }
        recyclerView.adapter = listsAdapter

        // Set up button to create a new list
        createListButton.setOnClickListener {
            showCreateListDialog()
        }

        // Fetch lists for the folder
        fetchLists()
    }

    private fun handleNavigation(menuItem: MenuItem) {
        when (menuItem.itemId) {
            R.id.nav_home -> {
                val intent = Intent(this, DashboardActivity::class.java)
                startActivity(intent)
                finish()
            }
            R.id.nav_spaces -> {
                val intent = Intent(this, SpacesActivity::class.java)
                startActivity(intent)
                finish()
            }
            R.id.nav_logout -> {
                showLogoutConfirmationDialog()
            }
            R.id.exit -> {
                showExitConfirmationDialog()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
    }

    private fun showExitConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Exit")
        builder.setMessage("Are you sure you want to exit the app?")
        builder.setPositiveButton("Yes") { _, _ ->
            finishAffinity() // Close all activities and exit the app
        }
        builder.setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Logout")
        builder.setMessage("Are you sure you want to log out?")
        builder.setPositiveButton("Yes") { _, _ ->
            performLogout()
        }
        builder.setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }

    private fun performLogout() {
        Toast.makeText(this, "Logged out successfully!", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun fetchLists() {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder().addInterceptor(logging).build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.clickup.com/api/v2/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val clickUpApi = retrofit.create(ClickUpApi::class.java)

        clickUpApi.getLists(clickUpAuthToken, folderId).enqueue(object : Callback<ListsResponse> {
            override fun onResponse(call: Call<ListsResponse>, response: Response<ListsResponse>) {
                if (response.isSuccessful) {
                    val lists = response.body()?.lists ?: emptyList()
                    listsList.clear()
                    listsList.addAll(lists)
                    listsAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this@ListsActivity, "Failed to fetch lists", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ListsResponse>, t: Throwable) {
                Toast.makeText(this@ListsActivity, "Error fetching lists", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showCreateListDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Create New List")

        val input = EditText(this)
        input.hint = "Enter list name"
        builder.setView(input)

        builder.setPositiveButton("Create") { dialog, _ ->
            val listName = input.text.toString()
            if (listName.isNotEmpty()) {
                createList(listName)
            } else {
                Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun createList(listName: String) {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder().addInterceptor(logging).build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.clickup.com/api/v2/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val clickUpApi = retrofit.create(ClickUpApi::class.java)
        val newList = ListRequest(name = listName)

        clickUpApi.createList(clickUpAuthToken, folderId, newList).enqueue(object : Callback<ListResponse> {
            override fun onResponse(call: Call<ListResponse>, response: Response<ListResponse>) {
                if (response.isSuccessful) {
                    val createdList = response.body()
                    createdList?.let {
                        fetchLists()
                    }
                } else {
                    Toast.makeText(this@ListsActivity, "Failed to create list", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ListResponse>, t: Throwable) {
                Toast.makeText(this@ListsActivity, "Error creating list", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
