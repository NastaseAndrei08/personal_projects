package com.example.mydogsapp.ui

import kotlinx.serialization.Serializable


@Serializable
object BreedListRoute


@Serializable
data class BreedImagesRoute(
    val breedName: String
)


@Serializable
data class EditDetailsRoute(
    val imageUrl: String,
    val breedName: String
)


@Serializable
data class GallerySelectorRoute(
    val breedName: String,
    val currentSelection: List<String> = emptyList()
)