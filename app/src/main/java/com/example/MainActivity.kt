package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.navigation.PranayamaAppNav
import com.example.ui.theme.PranayamaTheme
import com.example.viewmodel.PranayamaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: PranayamaViewModel = viewModel()
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            PranayamaTheme(themePreference = settings.themePreference) {
                PranayamaAppNav(viewModel = viewModel)
            }
        }
    }
}
