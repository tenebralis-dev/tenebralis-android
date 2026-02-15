package com.tenebralis.dreamos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tenebralis.dreamos.domain.model.SessionState
import com.tenebralis.dreamos.presentation.components.DreamOsSplashScreen
import com.tenebralis.dreamos.presentation.navigation.DreamOsNavGraph
import com.tenebralis.dreamos.presentation.theme.DreamOsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DreamOsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val sessionState by mainViewModel.sessionState
                        .collectAsStateWithLifecycle()

                    when (sessionState) {
                        is SessionState.Loading -> {
                            // Session 加载中 → 显示闪屏
                            DreamOsSplashScreen()
                        }
                        else -> {
                            // 已确定认证状态 → 进入导航
                            DreamOsNavGraph(sessionState = sessionState)
                        }
                    }
                }
            }
        }
    }
}
