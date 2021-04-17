package com.shivam.puppyadoption.ui.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.shivam.puppyadoption.R
import com.shivam.puppyadoption.databinding.ActivityMainBinding
import com.shivam.puppyadoption.utils.setupWithNavController

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var currentNavController: LiveData<NavController>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        setupNavigationBar()
    }

    private fun setupNavigationBar() {
        val bottomNavigationView = binding.bottomNavView

        val navGraphIds = listOf(
            R.navigation.nav_home,
            R.navigation.nav_request,
            R.navigation.nav_post,
            R.navigation.nav_message,
            R.navigation.nav_profile
        )

        val controller = bottomNavigationView.setupWithNavController(
            navGraphIds = navGraphIds,
            fragmentManager = supportFragmentManager,
            containerId = R.id.fragment_container,
            intent = intent
        )

        controller.observe(this, { navController ->
            setupActionBarWithNavController(navController)
        })

        currentNavController = controller
    }


//    override fun onSupportNavigateUp(): Boolean {
//        return currentNavController?.value?.navigateUp() ?: false
//    }
}