package com.jetdesk

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.jetdesk.databinding.ActivitySpacesBinding
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class SpacesActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySpacesBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private val clickUpAuthToken = "81853189_87110273f3e75c210ac57a2d8d1d0336eea54e1508fe5f3bf246af0b68b51c8a"
    private val teamId = "9012517272"
    private val spacesList = mutableListOf<Space>()
    private lateinit var spacesAdapter: SpacesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySpacesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up Toolbar
        setSupportActionBar(binding.toolbar)

        // Set up Drawer Layout
        setupDrawerNavigation()

        // Set up RecyclerView
        binding.spacesRecyclerView.layoutManager = LinearLayoutManager(this)
        spacesAdapter = SpacesAdapter(spacesList) { space ->
            val intent = Intent(this, FoldersActivity::class.java)
            intent.putExtra("SPACE_ID", space.id)
            startActivity(intent)
        }
        binding.spacesRecyclerView.adapter = spacesAdapter

        // Set up Create Space Button
        binding.createSpaceButton.setOnClickListener {
            showCreateSpaceDialog()
        }

        // Fetch Spaces
        fetchSpaces()
    }

    private fun setupDrawerNavigation() {
        toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            handleNavigation(menuItem)
            true
        }
    }

    private fun handleNavigation(menuItem: MenuItem) {
        when (menuItem.itemId) {
            R.id.nav_home -> {
                val intent = Intent(this, DashboardActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_spaces -> {
                Toast.makeText(this, "Already on Spaces", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_logout -> {
                showLogoutConfirmationDialog()
            }
            R.id.exit -> {
                showExitConfirmationDialog()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START) // Close the drawer after selection
    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Logout")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { _, _ ->
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showExitConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Exit")
            .setMessage("Are you sure you want to exit?")
            .setPositiveButton("Yes") { _, _ ->
                finishAffinity()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun fetchSpaces() {
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

        clickUpApi.getSpaces(clickUpAuthToken, teamId).enqueue(object : Callback<ClickUpResponse> {
            override fun onResponse(call: Call<ClickUpResponse>, response: Response<ClickUpResponse>) {
                if (response.isSuccessful) {
                    val spaces = response.body()?.spaces ?: emptyList()
                    spacesList.clear()
                    spacesList.addAll(spaces)
                    spacesAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this@SpacesActivity, "Failed to fetch spaces", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ClickUpResponse>, t: Throwable) {
                Log.e("SpacesActivity", "Error fetching spaces", t)
                Toast.makeText(this@SpacesActivity, "Error fetching spaces", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showCreateSpaceDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Create New Space")

        val input = EditText(this)
        input.hint = "Enter space name"
        builder.setView(input)

        builder.setPositiveButton("Create") { dialog, _ ->
            val spaceName = input.text.toString()
            if (spaceName.isNotEmpty()) {
                createSpace(spaceName)
            } else {
                Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }


    private fun createSpace(spaceName: String) {
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
        val newSpace = SpaceRequest(name = spaceName)

        Log.d("SpacesActivity", "Creating space with name: $spaceName for teamId: $teamId using token: $clickUpAuthToken")

        clickUpApi.createSpace(clickUpAuthToken, teamId, newSpace).enqueue(object : Callback<SpaceResponse> {
            override fun onResponse(call: Call<SpaceResponse>, response: Response<SpaceResponse>) {
                if (response.isSuccessful) {
                    val createdSpace = response.body()
                    createdSpace?.let {
                        Toast.makeText(this@SpacesActivity, "Space created: ${it.name}", Toast.LENGTH_SHORT).show()
                        fetchSpaces() // Refresh the list of spaces
                    }
                } else {
                    Log.e("SpacesActivity", "Error: ${response.code()} - ${response.errorBody()?.string()}")
                    Toast.makeText(this@SpacesActivity, "Failed to create space: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SpaceResponse>, t: Throwable) {
                Log.e("SpacesActivity", "Error creating space", t)
                Toast.makeText(this@SpacesActivity, "Error creating space", Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (toggle.onOptionsItemSelected(item)) {
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}