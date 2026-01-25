package com.example.mydogsapp.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mydogsapp.data.DataManager
import com.example.mydogsapp.data.DogImageDetails
import androidx.compose.runtime.saveable.rememberSaveable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDetailsScreen(
    imageUrl: String,
    breedName: String,

    galleryResult: List<String>?,
    onOpenGallery: () -> Unit,
    onSaveSuccess: () -> Unit
) {

    var name by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }

    var selectedGalleryImages by remember { mutableStateOf(listOf<String>()) }


    var nameError by remember { mutableStateOf<String?>(null) }
    var descError by remember { mutableStateOf<String?>(null) }
    var galleryError by remember { mutableStateOf<String?>(null) }


    LaunchedEffect(galleryResult) {
        if (galleryResult != null) {
            selectedGalleryImages = galleryResult
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Editează Detalii") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ---Name ---
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nume (4-20 caractere)") },
                modifier = Modifier.fillMaxWidth(),
                isError = nameError != null
            )
            if (nameError != null) {
                Text(nameError!!, color = Color.Red, style = MaterialTheme.typography.bodySmall)
            }

            // ---Description ---
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descriere (min 10 caractere)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                isError = descError != null
            )
            if (descError != null) {
                Text(descError!!, color = Color.Red, style = MaterialTheme.typography.bodySmall)
            }

            // ---Gallery ---
            Text("Galerie de imagini", style = MaterialTheme.typography.titleMedium)

            if (selectedGalleryImages.isEmpty()) {
                Button(onClick = onOpenGallery) {
                    Text("Modifică")
                }
            } else {

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(selectedGalleryImages) { img ->
                        AsyncImage(
                            model = img,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp).border(2.dp, Color.Green),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Button(onClick = onOpenGallery) {
                    Text("Modifică selecția")
                }
            }
            if (galleryError != null) {
                Text(galleryError!!, color = Color.Red, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- Save Button---
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {

                    var isValid = true


                    if (name.length < 4 || name.length > 20) {
                        nameError = "Numele trebuie să aibă între 4 și 20 caractere"
                        isValid = false
                    } else {
                        nameError = null
                    }


                    if (description.length < 10) {
                        descError = "Descrierea trebuie să aibă minimum 10 caractere"
                        isValid = false
                    } else {
                        descError = null
                    }


                    if (selectedGalleryImages.isEmpty()) {
                        galleryError = "Trebuie să selectați minim o imagine"
                        isValid = false
                    } else {
                        galleryError = null
                    }

                    if (isValid) {

                        DataManager.imageDetails[imageUrl] = DogImageDetails(name, description, selectedGalleryImages)
                        onSaveSuccess()
                    }
                }
            ) {
                Text("Actualizează detaliile")
            }
        }
    }
}