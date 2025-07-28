package np.com.bimalkafle.firebaseauthdemoapp

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import np.com.bimalkafle.firebaseauthdemoapp.pages.*
//Filaname: MyAppNavigation.kt
@Composable
fun MyAppNavigation(modifier: Modifier = Modifier, authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route
            if (currentDestination == "home" || currentDestination == "shoppingcart" || currentDestination == "profile") {
                BottomNavigationBar(navController, authViewModel)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = modifier.then(Modifier.padding(innerPadding))
        ) {
            composable("login") {
                LoginPage(modifier, navController, authViewModel)
            }
            composable("signup") {
                SignupPage(modifier, navController, authViewModel)
            }
            composable("home") {
                HomePage(modifier, navController, authViewModel)
            }
            composable("shoppingcart") {
                ShoppingCartPage(modifier, navController, authViewModel)
            }
            composable("profile") {
                ProfilePage(modifier, navController, authViewModel)
            }
        }
    }
}
