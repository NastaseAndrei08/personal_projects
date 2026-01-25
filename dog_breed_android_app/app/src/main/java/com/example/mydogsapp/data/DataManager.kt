package com.example.mydogsapp.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf

object DataManager {

    val favoriteImages = mutableStateListOf<String>()


    val imageDetails = mutableStateMapOf<String, DogImageDetails>()

    fun toggleFavorite(imageUrl: String) {
        if (favoriteImages.contains(imageUrl)) {
            favoriteImages.remove(imageUrl)
        } else {
            favoriteImages.add(imageUrl)
        }
    }

    fun isFavorite(imageUrl: String): Boolean {
        return favoriteImages.contains(imageUrl)
    }

    fun hasDetails(imageUrl: String): Boolean {
        return imageDetails.containsKey(imageUrl)
    }
}


data class DogImageDetails(
    val name: String,
    val description: String,
    val selectedGalleryImages: List<String> = emptyList()
)