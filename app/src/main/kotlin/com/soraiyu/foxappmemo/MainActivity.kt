package com.soraiyu.foxappmemo

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.soraiyu.foxappmemo.ui.navigation.FoxAppMemoNavGraph
import com.soraiyu.foxappmemo.ui.theme.FoxAppMemoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Extract shared text from intent (e.g., Play Store share)
        val sharedText: String? = when {
            intent?.action == Intent.ACTION_SEND &&
                intent.type == "text/plain" -> intent.getStringExtra(Intent.EXTRA_TEXT)
            else -> null
        }

        setContent {
            FoxAppMemoTheme {
                FoxAppMemoNavGraph(sharedText = sharedText)
            }
        }
    }
}
