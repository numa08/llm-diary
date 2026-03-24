package net.numa08.llmdiary.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import net.numa08.llmdiary.ui.navigation.NavGraph
import net.numa08.llmdiary.ui.theme.LlmDiaryTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LlmDiaryTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}
