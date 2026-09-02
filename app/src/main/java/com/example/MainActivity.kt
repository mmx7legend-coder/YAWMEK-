package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ui.YawmekApp

class MainActivity : ComponentActivity() {

    companion object {
        const val EXTRA_NAV_DESTINATION = "EXTRA_NAV_DESTINATION"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YawmekApp()
        }
    }
}

