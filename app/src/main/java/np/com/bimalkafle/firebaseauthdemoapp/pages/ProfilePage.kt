package np.com.bimalkafle.firebaseauthdemoapp.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import np.com.bimalkafle.firebaseauthdemoapp.AuthViewModel

@Composable
fun ProfilePage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val currentUser = authViewModel.getCurrentUser()


    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "User Profile",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        currentUser?.let { user ->
            Text(text = "Email: ${user.email ?: "Not available"}")
            Text(text = "User ID: ${user.uid}")
            Text(text = "Name: ${user.displayName ?: "Not available"}")
        } ?: run {
            Text(text = "No user is currently logged in.")
        }

    }
}
