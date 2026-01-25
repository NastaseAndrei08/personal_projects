package com.example.mydogsapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.mydogsapp.ui.BreedDetailScreen
import com.example.mydogsapp.ui.BreedImagesRoute
import com.example.mydogsapp.ui.BreedListRoute
import com.example.mydogsapp.ui.BreedListScreen
import com.example.mydogsapp.ui.EditDetailsRoute
import com.example.mydogsapp.ui.EditDetailsScreen
import com.example.mydogsapp.ui.GallerySelectorRoute
import com.example.mydogsapp.ui.GallerySelectorScreen
import com.example.mydogsapp.viewmodel.BreedDetailViewModel
import com.example.mydogsapp.viewmodel.DogsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = BreedListRoute
                ) {
                    // --- Screen 1: List ---
                    composable<BreedListRoute> {
                        val listViewModel: DogsViewModel = viewModel()

                        BreedListScreen(
                            breedList = listViewModel.breedList,
                            isLoading = listViewModel.isLoading,
                            onBreedClick = { selectedBreed ->
                                navController.navigate(BreedImagesRoute(breedName = selectedBreed))
                            }
                        )
                    }

                    // --- Screen 2: Images ---
                    composable<BreedImagesRoute> { backStackEntry ->
                        val args = backStackEntry.toRoute<BreedImagesRoute>()
                        val detailViewModel: BreedDetailViewModel = viewModel()

                        BreedDetailScreen(
                            breedName = args.breedName,
                            viewModel = detailViewModel,
                            onBackClick = { navController.popBackStack() },

                            onEditClick = { imageUrl ->
                                navController.navigate(
                                    EditDetailsRoute(
                                        imageUrl = imageUrl,
                                        breedName = args.breedName
                                    )
                                )
                            }
                        )
                    }

                    // --- Screen 3: Edit Details Form ---
                    composable<EditDetailsRoute> { backStackEntry ->
                        val args = backStackEntry.toRoute<EditDetailsRoute>()


                        val galleryResult = backStackEntry.savedStateHandle.get<List<String>>("selected_images")

                        EditDetailsScreen(
                            imageUrl = args.imageUrl,
                            breedName = args.breedName,
                            galleryResult = galleryResult,
                            onOpenGallery = {
                                navController.navigate(GallerySelectorRoute(breedName = args.breedName))
                            },
                            onSaveSuccess = {
                                navController.popBackStack()
                            }
                        )
                    }

                    // --- Screen 4: Photo Selector ---
                    composable<GallerySelectorRoute> { backStackEntry ->
                        val args = backStackEntry.toRoute<GallerySelectorRoute>()

                        val viewModel: BreedDetailViewModel = viewModel()

                        GallerySelectorScreen(
                            breedName = args.breedName,
                            viewModel = viewModel,
                            onSaveSelection = { selectedList ->

                                navController.previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.set("selected_images", selectedList)

                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}