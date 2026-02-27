package com.djoudini.player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.djoudini.player.ui.navigation.AppNavigation
import com.djoudini.player.ui.theme.DjoudiniTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DjoudiniTheme {
                AppNavigation()
            }
        }
    }
}
