package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.data.db.AppDatabase
import com.example.data.repository.CampusRepository
import com.example.ui.CampusViewModel
import com.example.ui.CampusViewModelFactory
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.google.android.gms.ads.MobileAds

sealed class Screen {
    object Dashboard : Screen()
    object Notifications : Screen()
    data class Detail(val opportunityId: Int) : Screen()
}

class MainActivity : ComponentActivity() {

    private lateinit var database: AppDatabase
    private lateinit var viewModel: CampusViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Google Mobile Ads SDK
        MobileAds.initialize(this) {}

        // Initialize Room Database
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "campushub_database"
        )
        .fallbackToDestructiveMigration()
        .build()

        // Create Repository & ViewModel
        val repository = CampusRepository(
            opportunityDao = database.opportunityDao(),
            registrationDao = database.registrationDao(),
            notificationDao = database.notificationDao(),
            userDao = database.userDao()
        )

        val factory = CampusViewModelFactory(application, repository)
        viewModel = ViewModelProvider(this, factory)[CampusViewModel::class.java]

        setContent {
            MyApplicationTheme {
                val userProfile by viewModel.userProfile.collectAsState()
                val isOrganizerMode by viewModel.isOrganizerMode.collectAsState()

                // Custom type-safe Navigation Stack
                var navigationStack = remember { mutableStateListOf<Screen>(Screen.Dashboard) }
                val currentScreen = navigationStack.lastOrNull() ?: Screen.Dashboard

                fun navigateTo(screen: Screen) {
                    navigationStack.add(screen)
                }

                fun navigateBack() {
                    if (navigationStack.size > 1) {
                        navigationStack.removeAt(navigationStack.size - 1)
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (userProfile == null) {
                            // Onboarding
                            ProfileOnboardingScreen(
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            // Main Navigation Selector
                            AnimatedContent(
                                targetState = currentScreen,
                                transitionSpec = {
                                    fadeIn() togetherWith fadeOut()
                                },
                                label = "ScreenTransition"
                            ) { screen ->
                                when (screen) {
                                    is Screen.Dashboard -> {
                                        if (isOrganizerMode) {
                                            OrganizerDashboardScreen(
                                                viewModel = viewModel,
                                                onOpportunityClick = { id -> navigateTo(Screen.Detail(id)) },
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            DashboardScreen(
                                                viewModel = viewModel,
                                                onOpportunityClick = { id -> navigateTo(Screen.Detail(id)) },
                                                onNotificationsClick = { navigateTo(Screen.Notifications) },
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                    is Screen.Notifications -> {
                                        NotificationsScreen(
                                            viewModel = viewModel,
                                            onBackClick = { navigateBack() },
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    is Screen.Detail -> {
                                        OpportunityDetailScreen(
                                            opportunityId = screen.opportunityId,
                                            viewModel = viewModel,
                                            onBackClick = { navigateBack() },
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
