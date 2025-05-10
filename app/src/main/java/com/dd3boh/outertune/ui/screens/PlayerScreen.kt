package com.dd3boh.outertune.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.dd3boh.outertune.ui.component.BottomSheetState
import com.dd3boh.outertune.ui.player.BottomSheetPlayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    navController: NavController,
//    scrollBehavior: TopAppBarScrollBehavior,
    playerBottomSheetState: BottomSheetState,
    modifier: Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        BottomSheetPlayer(
            state = playerBottomSheetState,
            navController = navController
        )
    }
}
