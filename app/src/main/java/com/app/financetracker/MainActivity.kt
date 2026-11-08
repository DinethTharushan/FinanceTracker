package com.app.financetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.app.financetracker.data.db.FinanceDatabase
import com.app.financetracker.ui.PlaceholderScreen
import com.app.financetracker.ui.theme.FinanceTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Temporary: makes Dagger validate the :data bindings at build time. Room opens the
    // database lazily, so this does no work. Remove once the first repository exists.
    @Inject
    lateinit var database: FinanceDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinanceTrackerTheme {
                PlaceholderScreen()
            }
        }
    }
}
