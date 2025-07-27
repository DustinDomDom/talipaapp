package np.com.bimalkafle.firebaseauthdemoapp

import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import np.com.bimalkafle.firebaseauthdemoapp.AuthViewModel


@Composable
fun BottomNavigationBar(navController: NavController, authViewModel: AuthViewModel) {
    val items = listOf(
        BottomNavItem("home", "Home", Icons.Default.Home),
        BottomNavItem("shoppingcart", "Cart", Icons.Default.ShoppingCart),
        BottomNavItem("profile", "Profile", Icons.Default.Person),
        BottomNavItem("logout", "Logout", Icons.Default.ExitToApp)
    )

    NavigationBar {
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    if (item.route == "logout") {
                        authViewModel.signout()
                        navController.navigate("login") {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                    else if (item.route == "shoppingcart") {
                        navController.navigate("shoppingcart") {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                    else if (item.route == "home") {
                        navController.navigate("home") {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                    else if (item.route == "profile") {
                        navController.navigate("profile") {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
