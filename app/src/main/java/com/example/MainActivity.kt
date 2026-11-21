package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ui.AppContent
import com.example.ui.theme.BharatShikshaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize Firebase App Check before any ViewModel/Compose accesses Firebase AI Logic
        configureAppCheck(this, intent)
        enableEdgeToEdge()
        setContent {
            BharatShikshaTheme {
                AppContent()
            }
        }
    }
}
