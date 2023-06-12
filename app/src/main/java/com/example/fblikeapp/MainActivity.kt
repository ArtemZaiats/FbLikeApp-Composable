package com.example.fblikeapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DismissibleNavigationDrawer
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.example.fblikeapp.data.Shortcut
import com.example.fblikeapp.data.getRandomItems
import com.example.fblikeapp.ui.theme.FbLikeAppTheme
import kotlinx.coroutines.launch

sealed class Destination(val route: String) {
    object Home : Destination("home")
    object Notifications : Destination("notifications")
    object Detail : Destination("detail/{itemId}") {
        fun createRoute(itemId: Int) = "detail/$itemId"
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FbLikeAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    FbScaffold(navController = navController)
                }
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FbScaffold(navController: NavHostController) {
    val scope = rememberCoroutineScope()
    val state = rememberDrawerState(initialValue = DrawerValue.Closed)
    val onDrawerIconClick: () -> Unit = {
        scope.launch {
            state.open()
        }
    }

    val context = LocalContext.current
    val randomItems = remember { mutableStateOf(getRandomItems(10)) }
    val shortcuts = remember { mutableStateOf(Shortcut.getShortcuts()) }

    ModalNavigationDrawer(
        drawerContent = { NavigationDrawer(randomItems.value, shortcuts.value) },
        drawerState = state,
        content = {
            Scaffold(
                bottomBar = { FbBottomNav(navController = navController, onDrawerIconClick) },

                ) { padding ->
                val stdModifier =
                    Modifier
                        .padding(bottom = padding.calculateBottomPadding())
                        .background(Color(0xffcccccc))
                NavHost(navController = navController, startDestination = Destination.Home.route) {
                    composable(Destination.Home.route) {
                        HomeScreen(
                            navController = navController,
                            modifier = stdModifier
                        )
                    }
                    composable(Destination.Notifications.route) {
                        NotificationsScreen(
                            navController = navController,
                            modifier = stdModifier
                        )
                    }
                    composable(Destination.Detail.route,
                        deepLinks = listOf(
                            navDeepLink { uriPattern = "https://www.fblikeapp.com/{itemId}" },
                            navDeepLink { uriPattern = "https://fblikeapp.com/{itemId}" }
                        )
                    ) {
                        val itemId = it.arguments?.getString("itemId")
                        if (itemId == null)
                            Toast.makeText(context, "Id is required", Toast.LENGTH_SHORT).show()
                        else
                            ItemDetailsScreen(itemId = itemId.toInt(), modifier = stdModifier)
                    }
                }
            }
        }
    )

}