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
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.database.*
import np.com.bimalkafle.firebaseauthdemoapp.AuthViewModel
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import com.google.firebase.database.FirebaseDatabase
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment

@Composable
fun ProfilePage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var showTransactionsDialog by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var storeName by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val currentUser = authViewModel.getCurrentUser()
    var message by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Profile", style = MaterialTheme.typography.headlineMedium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        currentUser?.let { user ->
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Email: ${user.email ?: "N/A"}")
                    Text("UID: ${user.uid}")
                    Text("Name: ${user.displayName ?: "N/A"}")
                }
            }
        } ?: Text("No user is logged in.")

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { showDialog = true }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.AddBusiness, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Store")
            }

            OutlinedButton(onClick = { showTransactionsDialog = true }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.History, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Transactions")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Store, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Your Stores", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(8.dp))

        ProfileStores(authViewModel = authViewModel, navController = navController)

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                authViewModel.signout()
                navController.navigate("login") {
                    popUpTo("profile") { inclusive = true }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.Default.Logout, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign Out", color = Color.White)
        }
    }

    if (showTransactionsDialog) {
        TransactionHistoryDialog(
            authViewModel = authViewModel,
            onDismiss = { showTransactionsDialog = false }
        )
    }

    if (showDialog) {
        StoreCreationDialog(
            storeName = storeName,
            imageUrl = imageUrl,
            description = description,
            onDismiss = {
                showDialog = false
                storeName = ""
                imageUrl = ""
                description = ""
            },
            onCreate = { name, url, desc ->
                authViewModel.createStore(name, url, desc) { success, msg ->
                    message = msg
                    showDialog = false
                    storeName = ""
                    imageUrl = ""
                    description = ""
                }
            },
            onUpdateFields = { name, url, desc ->
                storeName = name
                imageUrl = url
                description = desc
            }
        )
    }
}

@Composable
fun TransactionHistoryDialog(
    authViewModel: AuthViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val userId = authViewModel.getCurrentUser()?.uid ?: return

    var transactions by remember { mutableStateOf<List<DataSnapshot>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val ref = FirebaseDatabase.getInstance().getReference("transactions").child(userId)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                transactions = snapshot.children.toList()
                loading = false
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load transactions", Toast.LENGTH_SHORT).show()
                loading = false
            }
        })
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .fillMaxHeight(0.5f), // Reduce height to avoid layout overflow
        title = { Text("Transaction History") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (loading) {
                        Text("Loading...")
                    } else if (transactions.isEmpty()) {
                        Text("No transactions found.")
                    } else {
                        Column {
                            transactions.forEach { txn ->
                                val totalPrice = txn.child("totalPrice").value?.toString() ?: "0"
                                val timestamp = txn.child("timestamp").value?.toString()?.toLongOrNull()?.let {
                                    java.text.SimpleDateFormat("yyyy-MM-dd HH:mm")
                                        .format(java.util.Date(it))
                                } ?: "Unknown Date"
                                val owner = txn.child("owner").value?.toString() ?: "Unknown Store"

                                Text("• ₱$totalPrice — $timestamp", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("  From: $owner", fontSize = 14.sp)

                                txn.child("items").children.forEach { item ->
                                    val product = item.child("product").value?.toString() ?: "Unnamed"
                                    val quantity = item.child("quantity").value?.toString() ?: "0"
                                    Text("    - $product x$quantity", fontSize = 14.sp)
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}


@Composable
fun StoreCreationDialog(
    storeName: String,
    imageUrl: String,
    description: String,
    onDismiss: () -> Unit,
    onCreate: (String, String, String) -> Unit,
    onUpdateFields: (String, String, String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("🛍️ Create New Store") },
        text = {
            Column {
                OutlinedTextField(
                    value = storeName,
                    onValueChange = { onUpdateFields(it, imageUrl, description) },
                    label = { Text("Store Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { onUpdateFields(storeName, it, description) },
                    label = { Text("Image URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { onUpdateFields(storeName, imageUrl, it) },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (storeName.isNotBlank()) {
                        onCreate(storeName.trim(), imageUrl.trim(), description.trim())
                    }
                }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}



@Composable
fun ProfileStores(
    authViewModel: AuthViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val database = FirebaseDatabase.getInstance().reference

    var storeList by remember { mutableStateOf(listOf<AuthViewModel.Store>()) }
    var isLoading by remember { mutableStateOf(true) }

    val currentUser = authViewModel.getCurrentUser()
    val currentOwner = currentUser?.displayName ?: currentUser?.email ?: ""

    LaunchedEffect(Unit) {
        database.child("stores").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val stores = mutableListOf<AuthViewModel.Store>()
                for (storeSnapshot in snapshot.children) {
                    val store = storeSnapshot.getValue(AuthViewModel.Store::class.java)
                    if (store?.owner == currentOwner) {
                        stores.add(store)
                    }
                }
                storeList = stores
                isLoading = false
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load stores", Toast.LENGTH_SHORT).show()
                isLoading = false
            }
        })
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color(0xFFF0F0F0))
    ) {
        if (isLoading) {
            item { Text("Loading Stores...", color = Color.Gray) }
        } else if (storeList.isEmpty()) {
            item { Text("No stores found.", color = Color.Gray) }
        } else {
            items(storeList) { store ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("store/${store.id}") }
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        if (store.imageUrl.isNotBlank()) {
                            Box(

                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(16.dp))
                            ) {
                                AsyncImage(
                                    model = store.imageUrl,
                                    contentDescription = "${store.name} image",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                        Text(store.name, fontSize = 20.sp, color = Color.Black)
                        Spacer(Modifier.height(4.dp))
                        Text(store.description, fontSize = 14.sp, color = Color.Gray)
                        Spacer(Modifier.height(4.dp))
                        Text("Store ID: ${store.id}", fontSize = 8.sp, color = Color.DarkGray)
                    }
                }
            }
        }
    }
}


