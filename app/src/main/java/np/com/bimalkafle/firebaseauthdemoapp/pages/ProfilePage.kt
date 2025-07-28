package np.com.bimalkafle.firebaseauthdemoapp.pages

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import np.com.bimalkafle.firebaseauthdemoapp.AuthViewModel

@Composable
fun ProfilePage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var showDialog by remember { mutableStateOf(false) }
    var storeName by remember { mutableStateOf("") }
    val currentUser = authViewModel.getCurrentUser()
    var message by remember { mutableStateOf<String?>(null) }


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

        Spacer(modifier = Modifier.height(32.dp))
        // Create Store
        Text(
            text = "Create Store",
            color = Color.Blue,
            fontSize = 16.sp,
            modifier = Modifier.clickable {
                showDialog = true
            }
        )

        Button(
            onClick = {
                authViewModel.signout()
                navController.navigate("login") {
                    popUpTo("profile") { inclusive = true }
                }
            }
        ) {
            Text("Sign Out")
        }
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Create Store") },
            text = {
                Column {
                    Text("Enter store name:")
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = storeName,
                        onValueChange = { storeName = it },
                        placeholder = { Text("e.g. PureGold") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (storeName.isNotBlank()) {
                            authViewModel.createStore(storeName.trim()) { success, msg ->
                                Log.d("StoreCreation", "Success: $success, Message: $msg")
                                message = msg
                                showDialog = false
                                storeName = ""
                            }
                        } else {
                            message = "Store name cannot be empty"
                        }
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    storeName = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}
