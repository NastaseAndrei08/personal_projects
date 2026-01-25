package com.example.mydogsapp.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mydogsapp.data.DataManager
import com.example.mydogsapp.viewmodel.BreedDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreedDetailScreen(
    breedName: String,
    viewModel: BreedDetailViewModel,
    onBackClick: () -> Unit,
    onEditClick: (String) -> Unit
) {

    LaunchedEffect(breedName) {
        viewModel.loadImagesForBreed(breedName)
    }


    var showBackDialog by remember { mutableStateOf(false) }


    BackHandler {
        showBackDialog = true
    }


    if (showBackDialog) {
        AlertDialog(
            onDismissRequest = { showBackDialog = false },
            title = { Text("Confirmare") },
            text = { Text("Doriți să reveniți la lista de rase?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showBackDialog = false
                        onBackClick()
                    }
                ) {
                    Text("Da")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackDialog = false }) {
                    Text("Nu")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(breedName.uppercase()) },

                navigationIcon = {
                    IconButton(onClick = { showBackDialog = true }) {
                        Text("<", style = MaterialTheme.typography.titleLarge)
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (viewModel.isLoading) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Încărcare imagini...")
                }
            } else if (viewModel.errorMessage != null) {
                Text(text = viewModel.errorMessage!!)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    items(viewModel.displayedImages) { imageUrl ->
                        val isFavorite = DataManager.isFavorite(imageUrl)
                        val hasDetails = DataManager.hasDetails(imageUrl)
                        var showMenu by remember { mutableStateOf(false) }

                        val cardBorder = if (hasDetails)
                            BorderStroke(4.dp, Color.Blue)
                        else null

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            elevation = CardDefaults.cardElevation(4.dp),
                            border = cardBorder
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = "Dog image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                // Menu Button
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                ) {
                                    IconButton(
                                        onClick = { showMenu = true },
                                        colors = IconButtonDefaults.iconButtonColors(
                                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                                        )
                                    ) {
                                        Text("⋮", style = MaterialTheme.typography.titleLarge)
                                    }

                                    DropdownMenu(
                                        expanded = showMenu,
                                        onDismissRequest = { showMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Editează detalii") },
                                            onClick = {
                                                showMenu = false
                                                onEditClick(imageUrl)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = {
                                                Text(if (isFavorite) "Elimină din favorite" else "Adaugă la favorite")
                                            },
                                            onClick = {
                                                DataManager.toggleFavorite(imageUrl)
                                                showMenu = false
                                            }
                                        )
                                    }
                                }

                                // Favorite Star
                                if (isFavorite) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Favorite",
                                        tint = Color.Yellow,
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(16.dp)
                                            .size(48.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Load More Button
                    if (viewModel.canLoadMore) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Button(onClick = { viewModel.loadMoreImages() }) {
                                    Text("Încarcă mai multe")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}