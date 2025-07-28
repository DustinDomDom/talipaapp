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
//HomePage.kt
@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val authState = authViewModel.authState.observeAsState()
    val currentUser = authViewModel.getCurrentUser()
    val displayName = currentUser?.displayName ?: "User"
    var showDialog by remember { mutableStateOf(false) }
    var storeName by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(authState.value) {
        if (authState.value is AuthState.Unauthenticated) {
            navController.navigate("login") {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
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
        var storeList by remember { mutableStateOf<List<AuthViewModel.Store>>(emptyList()) }
        var expanded by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            authViewModel.fetchStores { stores ->
                storeList = stores
            }
        }

        Box {
            Text(
                text = "Select Store",
                color = Color.Blue,
                fontSize = 16.sp,
                modifier = Modifier.clickable {
                    expanded = true
                }
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                storeList.forEach { store ->
                    DropdownMenuItem(
                        text = { Text(store.name) },
                        onClick = {
                            expanded = false
                            navController.navigate("store/${store.id}")
                        }
                    )
                }
            }
        }


        Spacer(modifier = Modifier.height(12.dp))

        // Create Store
        Text(
            text = "Create Store",
            color = Color.Blue,
            fontSize = 16.sp,
            modifier = Modifier.clickable {
                showDialog = true
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        message?.let {
            Text(
                text = it,
                color = if (it.contains("success", true)) Color.Green else Color.Red
            )
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



