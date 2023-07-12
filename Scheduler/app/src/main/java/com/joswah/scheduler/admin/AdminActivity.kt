package com.joswah.scheduler.admin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.joswah.scheduler.R
import com.joswah.scheduler.databinding.ActivityAdminBinding
import com.google.android.material.navigation.NavigationView
import com.joswah.scheduler.MainActivity
import com.joswah.scheduler.data.SharedPreferences

class AdminActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityAdminBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_schedule, R.id.nav_room, R.id.nav_addSchedule
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_logout -> {
                    logout()
                    true
                }
                else -> {
                    menuItem.onNavDestinationSelected(navController)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        val fragment = navController.currentDestination?.id
        if (fragment!! == R.id.nav_home) {
            logout()
        } else if (fragment == R.id.nav_schedule)
            navController.navigate(R.id.nav_home)
        else {
            super.onBackPressed()
        }
    }

    private fun logout() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Logout")
            .setMessage("Apakah anda ingin logout?")
            .setPositiveButton("Logout") { _, _ ->
                finish()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        val sf = SharedPreferences(this)
        sf.clearData()
    }
}
