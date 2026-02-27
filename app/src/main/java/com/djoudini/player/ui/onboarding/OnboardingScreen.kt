package com.djoudini.player.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.djoudini.player.data.remote.XtreamCategory

@Composable
fun OnboardingScreen(
    onLoginSuccess: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val selectedLive = remember { mutableStateListOf<String>() }
    val selectedVod = remember { mutableStateListOf<String>() }
    val selectedSeries = remember { mutableStateListOf<String>() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(24.dp).widthIn(max = 600.dp).fillMaxHeight(0.8f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            when (uiState.currentStep) {
                OnboardingStep.LOGIN -> LoginStep(viewModel)
                
                OnboardingStep.LIVE_CATEGORIES -> CategorySelectionStep(
                    title = "Live TV Categories",
                    categories = uiState.liveCategories,
                    selectedIds = selectedLive,
                    onNextClicked = { viewModel.nextStep() }
                )
                
                OnboardingStep.VOD_CATEGORIES -> CategorySelectionStep(
                    title = "VOD (Movies) Categories",
                    categories = uiState.vodCategories,
                    selectedIds = selectedVod,
                    onNextClicked = { viewModel.nextStep() }
                )

                OnboardingStep.SERIES_CATEGORIES -> CategorySelectionStep(
                    title = "Series Categories",
                    categories = uiState.seriesCategories,
                    selectedIds = selectedSeries,
                    isFinalStep = true,
                    onNextClicked = {
                        viewModel.completeOnboarding(
                            selectedLive.toList(),
                            selectedVod.toList(),
                            selectedSeries.toList()
                        )
                        onLoginSuccess()
                    }
                )
            }
        }
    }
}

@Composable
fun LoginStep(viewModel: OnboardingViewModel) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var serverUrl by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Welcome to Djoudini Player", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        Text("Enter your Xtream Codes API details", style = MaterialTheme.typography.bodyMedium, color = Color.LightGray)
        
        if (uiState.error != null) {
            Text(uiState.error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium)
        }

        OutlinedTextField(value = serverUrl, onValueChange = { serverUrl = it }, label = { Text("Server URL (http://domain:port)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { viewModel.authenticate(serverUrl, username, password) },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            if (uiState.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp)) else Text("Login")
        }
    }
}

@Composable
fun CategorySelectionStep(
    title: String,
    categories: List<XtreamCategory>,
    selectedIds: MutableList<String>,
    isFinalStep: Boolean = false,
    onNextClicked: () -> Unit
) {
    LaunchedEffect(categories) {
        selectedIds.clear()
        selectedIds.addAll(categories.map { it.category_id })
    }

    Column(
        modifier = Modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        
        Row {
            Button(onClick = {
                selectedIds.clear()
                selectedIds.addAll(categories.map { it.category_id })
            }) { Text("Select All") }
            Spacer(Modifier.width(8.dp))
            Button(onClick = { selectedIds.clear() }) { Text("Deselect All") }
        }

        LazyColumn(modifier = Modifier.weight(1f).padding(vertical = 16.dp)) {
            items(categories) { category ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = selectedIds.contains(category.category_id),
                        onCheckedChange = { isChecked ->
                            if (isChecked) selectedIds.add(category.category_id)
                            else selectedIds.remove(category.category_id)
                        }
                    )
                    Text(category.category_name)
                }
            }
        }
        
        Button(
            onClick = onNextClicked,
            enabled = selectedIds.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text(if (isFinalStep) "Start App" else "Next")
        }
    }
}
