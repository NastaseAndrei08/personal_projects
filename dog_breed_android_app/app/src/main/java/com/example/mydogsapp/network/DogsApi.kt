package com.example.mydogsapp.network

import com.example.mydogsapp.data.AllBreedsResponse
import com.example.mydogsapp.data.BreedImagesResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path


interface DogsApi {

    @GET("breeds/list/all")
    suspend fun getAllBreeds(): AllBreedsResponse


    @GET("breed/{breed}/images")
    suspend fun getBreedImages(@Path("breed") breed: String): BreedImagesResponse
}


object RetrofitInstance {
    private const val BASE_URL = "https://dog.ceo/api/"

    val api: DogsApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DogsApi::class.java)
    }
}