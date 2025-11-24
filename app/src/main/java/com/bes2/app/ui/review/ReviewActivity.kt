package com.bes2.app.ui.review

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.bes2.core_ui.theme.Bes2Theme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReviewActivity : ComponentActivity() {

    private val viewModel: ReviewViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Bes2Theme {
                ReviewScreen(
                    viewModel = viewModel,
                    onNavigateBack = { finish() }
                )
            }
        }
    }
}
