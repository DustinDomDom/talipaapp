package np.com.bimalkafle.firebaseauthdemoapp.pages

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import np.com.bimalkafle.firebaseauthdemoapp.AuthState
import np.com.bimalkafle.firebaseauthdemoapp.AuthViewModel

@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val authState = authViewModel.authState.observeAsState()
    val currentUser = authViewModel.getCurrentUser()
    val displayName = currentUser?.displayName ?: "User"
    var message by remember { mutableStateOf<String?>(null) }
    var storeList by remember { mutableStateOf<List<AuthViewModel.Store>>(emptyList()) }

    LaunchedEffect(authState.value) {
        if (authState.value is AuthState.Unauthenticated) {
            navController.navigate("login") {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
        }
    }

    LaunchedEffect(Unit) {
        authViewModel.fetchStores { stores ->
            storeList = stores
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Welcome $displayName", fontSize = 32.sp)
        Spacer(modifier = Modifier.height(24.dp))

        if (storeList.isEmpty()) {
            Text(text = "No stores found.", color = Color.Gray)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                storeList.forEach { store ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("store/${store.id}") },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                        ) {
                            Text(text = store.name, fontSize = 20.sp, color = Color.Black)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Store ID: ${store.id}", fontSize = 14.sp, color = Color.DarkGray)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        message?.let {
            Text(
                text = it,
                color = if (it.contains("success", true)) Color.Green else Color.Red
            )
        }
    }
}
