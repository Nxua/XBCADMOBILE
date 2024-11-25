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

class FoldersActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var createFolderButton: Button
    private lateinit var foldersAdapter: FoldersAdapter
    private val foldersList = mutableListOf<Folder>()

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private val clickUpAuthToken = "81853189_87110273f3e75c210ac57a2d8d1d0336eea54e1508fe5f3bf246af0b68b51c8a" // Replace this with the actual token
    private val spaceId: String by lazy {
        intent.getStringExtra("SPACE_ID") ?: ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_folders)

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

        // Log to check the received spaceId
        Log.d("FoldersActivity", "Received spaceId: $spaceId")

        if (spaceId.isEmpty()) {
            Log.e("FoldersActivity", "Space ID is empty. Cannot proceed.")
            Toast.makeText(this, "Invalid space. Please try again.", Toast.LENGTH_SHORT).show()
            finish() // Close the activity if spaceId is invalid
            return
        }

        // Initialize UI components
        recyclerView = findViewById(R.id.foldersRecyclerView)
        createFolderButton = findViewById(R.id.createFolderButton)

        recyclerView.layoutManager = LinearLayoutManager(this)
        foldersAdapter = FoldersAdapter(foldersList) { folder ->
            val intent = Intent(this, ListsActivity::class.java)
            intent.putExtra("FOLDER_ID", folder.id)
            startActivity(intent)
        }
        recyclerView.adapter = foldersAdapter

        // Set up button to create a new folder
        createFolderButton.setOnClickListener {
            showCreateFolderDialog()
        }

        // Fetch folders for the space
        fetchFolders()
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

    private fun fetchFolders() {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder().addInterceptor(logging).build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.clickup.com/api/v2/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val clickUpApi = retrofit.create(ClickUpApi::class.java)

        clickUpApi.getFolders(clickUpAuthToken, spaceId).enqueue(object : Callback<FoldersResponse> {
            override fun onResponse(call: Call<FoldersResponse>, response: Response<FoldersResponse>) {
                if (response.isSuccessful) {
                    val folders = response.body()?.folders ?: emptyList()
                    foldersList.clear()
                    foldersList.addAll(folders)
                    foldersAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this@FoldersActivity, "Failed to fetch folders", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<FoldersResponse>, t: Throwable) {
                Toast.makeText(this@FoldersActivity, "Error fetching folders", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showCreateFolderDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Create New Folder")

        val input = EditText(this)
        input.hint = "Enter folder name"
        builder.setView(input)

        builder.setPositiveButton("Create") { dialog, _ ->
            val folderName = input.text.toString()
            if (folderName.isNotEmpty()) {
                createFolder(folderName)
            } else {
                Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun createFolder(folderName: String) {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder().addInterceptor(logging).build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.clickup.com/api/v2/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val clickUpApi = retrofit.create(ClickUpApi::class.java)
        val newFolder = FolderRequest(name = folderName)

        clickUpApi.createFolder(clickUpAuthToken, spaceId, newFolder).enqueue(object : Callback<FolderResponse> {
            override fun onResponse(call: Call<FolderResponse>, response: Response<FolderResponse>) {
                if (response.isSuccessful) {
                    val createdFolder = response.body()
                    createdFolder?.let {
                        fetchFolders()
                    }
                } else {
                    Toast.makeText(this@FoldersActivity, "Failed to create folder", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<FolderResponse>, t: Throwable) {
                Toast.makeText(this@FoldersActivity, "Error creating folder", Toast.LENGTH_SHORT).show()
            }
        })
    }
}