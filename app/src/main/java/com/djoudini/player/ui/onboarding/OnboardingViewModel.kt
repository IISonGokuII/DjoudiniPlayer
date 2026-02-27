package com.djoudini.player.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.djoudini.player.data.local.PreferencesManager
import com.djoudini.player.data.remote.XtreamApi
import com.djoudini.player.data.remote.XtreamCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class OnboardingState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val error: String? = null,
    val liveCategories: List<XtreamCategory> = emptyList(),
    val vodCategories: List<XtreamCategory> = emptyList(),
    val seriesCategories: List<XtreamCategory> = emptyList()
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val xtreamApi: XtreamApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingState())
    val uiState = _uiState.asStateFlow()

    fun authenticate(server: String, user: String, pass: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // Ensure server URL is correctly formatted
                val baseUrl = if (server.endsWith("/")) server.dropLast(1) else server
                val authUrl = "$baseUrl/player_api.php?username=$user&password=$pass"
                
                val response = xtreamApi.authenticate(authUrl)
                if (response.isSuccessful && response.body()?.user_info?.username != null) {
                    val userInfo = response.body()!!.user_info!!
                    
                    // Parse expiration date (usually unix timestamp string)
                    val expDateTimestamp = userInfo.exp_date?.toLongOrNull()
                    val expDateString = if (expDateTimestamp != null && expDateTimestamp > 0) {
                        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(expDateTimestamp * 1000))
                    } else {
                        "Unlimited"
                    }
                    
                    preferencesManager.saveAccountInfo(baseUrl, user, pass, expDateString)
                    
                    // Proceed to fetch categories
                    fetchCategories(baseUrl, user, pass)
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Login failed. Check credentials.")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Network error: ${e.message}")
            }
        }
    }

    private suspend fun fetchCategories(baseUrl: String, user: String, pass: String) {
        try {
            val liveUrl = "$baseUrl/player_api.php?username=$user&password=$pass&action=get_live_categories"
            val vodUrl = "$baseUrl/player_api.php?username=$user&password=$pass&action=get_vod_categories"
            val seriesUrl = "$baseUrl/player_api.php?username=$user&password=$pass&action=get_series_categories"

            val liveRes = xtreamApi.getLiveCategories(liveUrl)
            val vodRes = xtreamApi.getVodCategories(vodUrl)
            val seriesRes = xtreamApi.getSeriesCategories(seriesUrl)

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isAuthenticated = true,
                liveCategories = liveRes.body() ?: emptyList(),
                vodCategories = vodRes.body() ?: emptyList(),
                seriesCategories = seriesRes.body() ?: emptyList()
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to fetch categories: ${e.message}")
        }
    }

    fun completeOnboarding(selectedLive: List<String>, selectedVod: List<String>, selectedSeries: List<String>) {
        viewModelScope.launch {
            // In a full implementation, you'd save these selections to a Room DB
            // and trigger the PlaylistSyncRepository worker.
            
            // Mark onboarding as complete to go to dashboard
            preferencesManager.setLoggedIn(true)
        }
    }
}
