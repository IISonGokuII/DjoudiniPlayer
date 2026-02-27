package com.djoudini.player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.djoudini.player.data.local.PreferencesManager
import com.djoudini.player.ui.navigation.AppNavigation
import com.djoudini.player.ui.theme.DjoudiniTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isLoggedIn by preferencesManager.isLoggedIn.collectAsState(initial = false)

            DjoudiniTheme {
                AppNavigation(isLoggedIn = isLoggedIn)
            }
        }
    }
}
