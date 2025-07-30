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
import androidx.compose.ui.layout.ContentScale

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

        Text("Profile", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(12.dp))

        currentUser?.let { user ->
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(6.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = user.displayName ?: "No Name",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Divider(color = Color.LightGray, thickness = 1.dp)

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Email: ${user.email ?: "Not available"}",
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "UID: ${user.uid}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        } ?: Text(
            text = "No user is logged in.",
            color = Color.Gray,
            style = MaterialTheme.typography.bodyMedium
        )

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
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(Color(0xFFF8F9FA))
    ) {
        when {
            isLoading -> {
                item {
                    Text(
                        "Loading stores...",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            storeList.isEmpty() -> {
                item {
                    Text(
                        "No stores found.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            else -> {
                items(storeList) { store ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                            .clickable { navController.navigate("store/${store.id}") },
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Store Image
                            if (store.imageUrl.isNotBlank()) {

                                AsyncImage(
                                    model = store.imageUrl,
                                    contentDescription = "${store.name} image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(16f / 9f)
                                        .clip(RoundedCornerShape(16.dp))

                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // Store Name
                            Text(
                                store.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            // Store Description
                            if (store.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    store.description,
                                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.DarkGray)
                                )
                            }

                            // Store ID
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "ID: ${store.id}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}