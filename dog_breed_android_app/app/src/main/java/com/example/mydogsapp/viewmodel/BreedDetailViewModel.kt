package com.example.mydogsapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mydogsapp.network.RetrofitInstance
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BreedDetailViewModel : ViewModel() {

    private var allImages = listOf<String>()


    var displayedImages by mutableStateOf<List<String>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set


    val canLoadMore: Boolean
        get() = displayedImages.size < allImages.size


    fun loadImagesForBreed(breed: String) {

        if (allImages.isNotEmpty()) return

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {

                val loadingTimer = launch { delay(2000) }


                val response = RetrofitInstance.api.getBreedImages(breed)


                loadingTimer.join()

                allImages = response.message


                displayedImages = allImages.take(6)

            } catch (e: Exception) {
                errorMessage = "Error: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }


    fun loadMoreImages() {

        val currentCount = displayedImages.size
        val nextBatch = allImages.drop(currentCount).take(6)


        displayedImages = displayedImages + nextBatch
    }
}