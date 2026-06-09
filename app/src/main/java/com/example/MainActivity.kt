package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.StoreRepository
import com.example.ui.StoreMainScreen
import com.example.ui.StoreViewModel
import com.example.ui.StoreViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Initialize DB & Repository
        val database = AppDatabase.getDatabase(this)
        val repository = StoreRepository(database.storeDao())
        
        // 2. Instantiate Main Store ViewModel
        val factory = StoreViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, factory)[StoreViewModel::class.java]
        
        enableEdgeToEdge()
        
        setContent {
            // Apply customizable dynamic Dark / Light theme selection matching the user request
            MyApplicationTheme(darkTheme = viewModel.isDarkTheme) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Main responsive store system container
                    StoreMainScreen(viewModel)
                }
            }
        }
    }
}
