package com.djoudini.player.ui.onboarding

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.djoudini.player.data.local.PreferencesManager
import com.djoudini.player.data.remote.XtreamApi
import com.djoudini.player.data.remote.XtreamCategory
import com.djoudini.player.data.worker.XtreamDataSyncWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

enum class OnboardingStep {
    LOGIN,
    LIVE_CATEGORIES,
    VOD_CATEGORIES,
    SERIES_CATEGORIES
}

data class OnboardingState(
    val currentStep: OnboardingStep = OnboardingStep.LOGIN,
    val isLoading: Boolean = false,
    val error: String? = null,
    val liveCategories: List<XtreamCategory> = emptyList(),
    val vodCategories: List<XtreamCategory> = emptyList(),
    val seriesCategories: List<XtreamCategory> = emptyList()
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val xtreamApi: XtreamApi,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingState())
    val uiState = _uiState.asStateFlow()

    fun authenticate(server: String, user: String, pass: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val baseUrl = if (server.endsWith("/")) server.dropLast(1) else server
                val authUrl = "$baseUrl/player_api.php?username=$user&password=$pass"
                
                val response = xtreamApi.authenticate(authUrl)

                if (response.isSuccessful && response.body() != null) {
                    val userInfo = response.body()?.userInfo
                    if (userInfo?.username != null) {
                        val expDate = userInfo.expDate?.toLongOrNull()?.let {
                            if (it > 0) SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(it * 1000)) else "Unlimited"
                        } ?: "Unlimited"
                        
                        preferencesManager.saveAccountInfo(baseUrl, user, pass, expDate)
                        fetchCategories(baseUrl, user, pass)
                    } else {
                        // Successful response, but empty or invalid user_info block
                        _uiState.update { it.copy(isLoading = false, error = "Authentication succeeded but server returned invalid user data.") }
                    }
                } else {
                     // Unsuccessful response (401, 404, etc.)
                     val errorBody = response.errorBody()?.string() ?: "Invalid credentials or server error."
                     _uiState.update { it.copy(isLoading = false, error = "Login failed: $errorBody") }
                }
            } catch (e: Exception) {
                 // Network-level error (no connection, DNS issue)
                 _uiState.update { it.copy(isLoading = false, error = "Network error: Could not connect to server.") }
            }
        }
    }

    private suspend fun fetchCategories(baseUrl: String, user: String, pass: String) {
        try {
            val liveRes = xtreamApi.getLiveCategories("$baseUrl/player_api.php?username=$user&password=$pass&action=get_live_categories")
            val vodRes = xtreamApi.getVodCategories("$baseUrl/player_api.php?username=$user&password=$pass&action=get_vod_categories")
            val seriesRes = xtreamApi.getSeriesCategories("$baseUrl/player_api.php?username=$user&password=$pass&action=get_series_categories")

            _uiState.update {
                it.copy(
                    isLoading = false,
                    liveCategories = liveRes.body() ?: emptyList(),
                    vodCategories = vodRes.body() ?: emptyList(),
                    seriesCategories = seriesRes.body() ?: emptyList(),
                    currentStep = OnboardingStep.LIVE_CATEGORIES // Move to next step
                )
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false, error = "Failed to fetch categories.") }
        }
    }

    fun nextStep() {
        val next = when (_uiState.value.currentStep) {
            OnboardingStep.LIVE_CATEGORIES -> OnboardingStep.VOD_CATEGORIES
            OnboardingStep.VOD_CATEGORIES -> OnboardingStep.SERIES_CATEGORIES
            else -> OnboardingStep.SERIES_CATEGORIES // Should not happen
        }
        _uiState.update { it.copy(currentStep = next) }
    }

    fun completeOnboarding(selectedLive: List<String>, selectedVod: List<String>, selectedSeries: List<String>) {
        viewModelScope.launch {
            preferencesManager.saveCategorySelections(selectedLive, selectedVod, selectedSeries)
            
            val workRequest = OneTimeWorkRequestBuilder<XtreamDataSyncWorker>().build()
            WorkManager.getInstance(context).enqueue(workRequest)
            
            preferencesManager.setLoggedIn(true)
        }
    }
}
