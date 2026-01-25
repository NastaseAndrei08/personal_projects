package com.example.mydogsapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mydogsapp.network.RetrofitInstance
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DogsViewModel : ViewModel() {

    var breedList by mutableStateOf<List<String>>(emptyList())
        private set


    var isLoading by mutableStateOf(true)
        private set


    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        fetchBreeds()
    }

    fun fetchBreeds() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {

                val loadingTimer = launch { delay(2000) }


                val response = RetrofitInstance.api.getAllBreeds()


                loadingTimer.join()


                breedList = response.message.keys.toList()

            } catch (e: Exception) {
                errorMessage = "Error: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
}