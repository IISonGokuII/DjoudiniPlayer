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
    
    // Login Data
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var serverUrl by remember { mutableStateOf("") }

    // Category Selection Data
    val selectedLive = remember { mutableStateListOf<String>() }
    val selectedVod = remember { mutableStateListOf<String>() }
    val selectedSeries = remember { mutableStateListOf<String>() }
    
    // Auto-select all categories initially when they load
    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            selectedLive.addAll(uiState.liveCategories.map { it.category_id })
            selectedVod.addAll(uiState.vodCategories.map { it.category_id })
            selectedSeries.addAll(uiState.seriesCategories.map { it.category_id })
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(24.dp).widthIn(max = 600.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!uiState.isAuthenticated) {
                    Text(
                        text = "Welcome to Djoudini Player",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Enter your Xtream Codes API details",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray
                    )

                    if (uiState.error != null) {
                        Text(
                            text = uiState.error!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    OutlinedTextField(
                        value = serverUrl,
                        onValueChange = { serverUrl = it },
                        label = { Text("Server URL (http://domain:port)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (serverUrl.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty()) {
                                viewModel.authenticate(serverUrl, username, password)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = "Login",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Choose Categories",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Select what you want to load to save memory and speed up the app.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Categories List
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f) // Takes up available space
                    ) {
                        if (uiState.liveCategories.isNotEmpty()) {
                            item { CategoryHeader("Live TV") }
                            items(uiState.liveCategories) { category ->
                                CategoryCheckbox(
                                    category = category,
                                    isChecked = selectedLive.contains(category.category_id),
                                    onCheckedChange = { checked ->
                                        if (checked) selectedLive.add(category.category_id)
                                        else selectedLive.remove(category.category_id)
                                    }
                                )
                            }
                        }

                        if (uiState.vodCategories.isNotEmpty()) {
                            item { CategoryHeader("VOD (Movies)") }
                            items(uiState.vodCategories) { category ->
                                CategoryCheckbox(
                                    category = category,
                                    isChecked = selectedVod.contains(category.category_id),
                                    onCheckedChange = { checked ->
                                        if (checked) selectedVod.add(category.category_id)
                                        else selectedVod.remove(category.category_id)
                                    }
                                )
                            }
                        }

                        if (uiState.seriesCategories.isNotEmpty()) {
                            item { CategoryHeader("Series") }
                            items(uiState.seriesCategories) { category ->
                                CategoryCheckbox(
                                    category = category,
                                    isChecked = selectedSeries.contains(category.category_id),
                                    onCheckedChange = { checked ->
                                        if (checked) selectedSeries.add(category.category_id)
                                        else selectedSeries.remove(category.category_id)
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.completeOnboarding(
                                selectedLive.toList(),
                                selectedVod.toList(),
                                selectedSeries.toList()
                            )
                            onLoginSuccess()
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        enabled = selectedLive.isNotEmpty() || selectedVod.isNotEmpty() || selectedSeries.isNotEmpty()
                    ) {
                        Text(
                            text = "Start App",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp)
    )
}

@Composable
fun CategoryCheckbox(
    category: XtreamCategory,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
        )
        Text(text = category.category_name, color = Color.White)
    }
}
