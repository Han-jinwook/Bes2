package com.bes2.app.ui.review

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.Text
import androidx.navigation.compose.rememberNavController
// [FINAL FIX] Correct the import path to include the 'theme' package
import com.bes2.core_ui.theme.Bes2Theme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReviewActivity : ComponentActivity() {

    private val viewModel: ReviewViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Bes2Theme {
                // ReviewScreen requires a NavController. Since this activity only shows
                // this screen, we can provide a new, empty NavController.
                val navController = rememberNavController()
                ReviewScreen(viewModel = viewModel, navController = navController)
            }
        }
    }
}
