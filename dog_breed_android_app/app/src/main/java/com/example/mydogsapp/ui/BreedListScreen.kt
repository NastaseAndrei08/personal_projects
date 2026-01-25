package com.example.mydogsapp.ui

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreedListScreen(
    breedList: List<String>,
    isLoading: Boolean,
    onBreedClick: (String) -> Unit
) {

    val activity = LocalContext.current as? Activity


    var showExitDialog by remember { mutableStateOf(false) }


    BackHandler {
        showExitDialog = true
    }


    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Confirmare") },
            text = { Text("Doriți să părăsiți aplicația?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        activity?.finish()
                    }
                ) {
                    Text("Da")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Nu")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Dogs App") })
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Loading data. Please wait...")
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(breedList) { breed ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .height(60.dp)
                                .clickable { onBreedClick(breed) },
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = breed.replaceFirstChar { it.uppercase() },
                                    modifier = Modifier.padding(start = 16.dp),

                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}