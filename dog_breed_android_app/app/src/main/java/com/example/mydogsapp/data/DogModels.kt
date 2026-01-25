package com.example.mydogsapp.data

import kotlinx.serialization.Serializable


data class AllBreedsResponse(
    val message: Map<String, List<String>>,
    val status: String
)


data class BreedImagesResponse(
    val message: List<String>,
    val status: String
)