package com.example.mydogsapp.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mydogsapp.viewmodel.BreedDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GallerySelectorScreen(
    breedName: String,
    viewModel: BreedDetailViewModel,
    onSaveSelection: (List<String>) -> Unit
) {

    LaunchedEffect(breedName) {
        viewModel.loadImagesForBreed(breedName)
    }


    val selectedImages = remember { mutableStateListOf<String>() }

    Scaffold(
        topBar = {
            TopAppBar(title = {

                if (selectedImages.size == 4) {
                    Text("Ați selectat deja 4 poze", color = Color.Red)
                } else {
                    Text("Poze selectate: ${selectedImages.size}/4")
                }
            })
        },
        bottomBar = {

            Button(
                onClick = {

                    onSaveSelection(ArrayList(selectedImages))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Salvează")
            }
        }
    ) { padding ->
        if (viewModel.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = padding,
                modifier = Modifier.fillMaxSize()
            ) {

                items(viewModel.displayedImages.size) { index ->
                    val imageUrl = viewModel.displayedImages[index]
                    val isSelected = selectedImages.contains(imageUrl)

                    if (index == viewModel.displayedImages.lastIndex && viewModel.canLoadMore) {
                        LaunchedEffect(Unit) {
                            viewModel.loadMoreImages()
                        }
                    }

                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .aspectRatio(1f)
                            .border(
                                width = if (isSelected) 4.dp else 0.dp,
                                color = if (isSelected) Color.Green else Color.Transparent
                            )
                            .clickable {
                                if (isSelected) {
                                    selectedImages.remove(imageUrl)
                                } else {
                                    if (selectedImages.size < 4) {
                                        selectedImages.add(imageUrl)
                                    }
                                }
                            }
                    ) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}