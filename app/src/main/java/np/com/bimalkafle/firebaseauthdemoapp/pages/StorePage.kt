package np.com.bimalkafle.firebaseauthdemoapp.pages
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.database.FirebaseDatabase
import np.com.bimalkafle.firebaseauthdemoapp.AuthViewModel
import androidx.compose.material.icons.filled.ArrowBack
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.database.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import coil.compose.AsyncImage


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorePage(
    modifier: Modifier = Modifier,
    storeId: String,
    authViewModel: AuthViewModel,
    navController: NavController
) {
    var showDeletePostDialog by remember { mutableStateOf(false) }
    var deletingPostId by remember { mutableStateOf("") }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingPostId by remember { mutableStateOf<String?>(null) }
    val database = FirebaseDatabase.getInstance()
    val context = LocalContext.current
    var storeName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var isOwner by remember { mutableStateOf<Boolean?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newDescription by remember { mutableStateOf("") }
    var newPrice by remember { mutableStateOf("") }
    var newImageUrl by remember { mutableStateOf("") }

    var posts by remember { mutableStateOf(listOf<Map<String, String>>()) }

    LaunchedEffect(storeId) {
        val storeRef = database.getReference("stores").child(storeId)

        storeRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                storeName = snapshot.child("name").getValue(String::class.java) ?: ""
                ownerName = snapshot.child("owner").getValue(String::class.java) ?: ""
                val currentUser = authViewModel.getCurrentUser()
                isOwner = ownerName == (currentUser?.displayName ?: currentUser?.email ?: "")

            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to load store details: ${error.message}")
            }
        })

        val postsRef = storeRef.child("posts")
        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val loadedPosts = mutableListOf<Map<String, String>>()
                for (postSnapshot in snapshot.children) {
                    val post = postSnapshot.value as? Map<String, String>
                    post?.let { loadedPosts.add(it) }
                }
                posts = loadedPosts
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to load posts: ${error.message}")
            }
        })
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Store Page") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Text(text = "Welcome to $storeName!", style = MaterialTheme.typography.headlineSmall)
            Text(text = "by $ownerName", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

            Spacer(modifier = Modifier.height(24.dp))

            if (isOwner == true) {


                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { showAddDialog = true

                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Add Product")
                    }

                }
                Button(
                    onClick = { showDeleteDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Text("Delete Store", color = Color.White)
                }

            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "⬅️ Swipe left to edit | ➡️ Swipe right to delete",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Text("Products:", style = MaterialTheme.typography.titleMedium)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(posts) { post ->
                    val dismissState = rememberDismissState(
                        confirmValueChange = {
                            when (it) {
                                DismissValue.DismissedToStart -> {
                                    // Swiped Left ⟶ Edit
                                    newTitle = post["title"] ?: ""
                                    newDescription = post["description"] ?: ""
                                    newPrice = post["price"] ?: ""
                                    editingPostId = post["id"] ?: ""
                                    showEditDialog = true
                                    false
                                }
                                DismissValue.DismissedToEnd -> {
                                    deletingPostId = post["id"] ?: ""
                                    showDeletePostDialog = true
                                    false
                                }
                                else -> false
                            }
                        }
                    )
                    Box(modifier = Modifier.padding(vertical = 4.dp)) {
                        SwipeToDismiss(
                            state = dismissState,
                            background = {
                                val direction = dismissState.dismissDirection ?: return@SwipeToDismiss
                                val color = when (direction) {
                                    DismissDirection.StartToEnd -> Color.Red
                                    DismissDirection.EndToStart -> Color.LightGray
                                }
                                val icon = when (direction) {
                                    DismissDirection.StartToEnd -> Icons.Default.Delete
                                    DismissDirection.EndToStart -> Icons.Default.Edit
                                }
                                val alignment = when (direction) {
                                    DismissDirection.StartToEnd -> Alignment.CenterStart
                                    DismissDirection.EndToStart -> Alignment.CenterEnd
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(16.dp)) // 👈 match the card corner radius
                                        .background(color)
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = alignment
                                ) {
                                    Icon(imageVector = icon, contentDescription = null, tint = Color.White)
                                }

                            },
                            dismissContent = {
                                // Don't put vertical padding here
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    elevation = CardDefaults.cardElevation(4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        AsyncImage(
                                            model = post["imageUrl"],
                                            contentDescription = "Product Image",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(180.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Title: ${post["title"]}")
                                        Text("Description: ${post["description"]}")
                                        Text("Price: ${post["price"]}")
                                    }
                                }

                            }
                        )
                    }

                }

            }

        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Product") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Title") }
                    )
                    OutlinedTextField(
                        value = newDescription,
                        onValueChange = { newDescription = it },
                        label = { Text("Description") }
                    )
                    OutlinedTextField(
                        value = newPrice,
                        onValueChange = { newPrice = it },
                        label = { Text("Price (e.g. 120php)") }
                    )
                    OutlinedTextField(
                        value = newImageUrl,
                        onValueChange = { newImageUrl = it },
                        label = { Text("Image URL (https://...)") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val postId = database.reference.push().key ?: return@TextButton
                    val postRef = database.getReference("stores")
                        .child(storeId)
                        .child("posts")
                        .child(postId)

                    val postData = mapOf(
                        "id" to postId,
                        "title" to newTitle,
                        "description" to newDescription,
                        "price" to newPrice,
                        "imageUrl" to newImageUrl // 👈 This line adds the image
                    )

                    postRef.setValue(postData).addOnSuccessListener {
                        Toast.makeText(context, "Product added!", Toast.LENGTH_SHORT).show()
                        newTitle = ""
                        newDescription = ""
                        newPrice = ""
                        newImageUrl = "" // 👈 Reset the image URL
                        showAddDialog = false
                    }.addOnFailureListener {
                        Toast.makeText(context, "Failed to add product", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Create")
                }
            }
            ,
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Product") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Title") }
                    )
                    OutlinedTextField(
                        value = newDescription,
                        onValueChange = { newDescription = it },
                        label = { Text("Description") }
                    )
                    OutlinedTextField(
                        value = newPrice,
                        onValueChange = { newPrice = it },
                        label = { Text("Price (e.g. 120php)") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val postRef = database.getReference("stores")
                        .child(storeId)
                        .child("posts")
                        .child(editingPostId ?: return@TextButton)  // graceful null handling


                    val updatedData = mapOf(
                        "id" to editingPostId,
                        "title" to newTitle,
                        "description" to newDescription,
                        "price" to newPrice
                    )

                    postRef.setValue(updatedData).addOnSuccessListener {
                        Toast.makeText(context, "Product updated!", Toast.LENGTH_SHORT).show()
                        showEditDialog = false
                    }.addOnFailureListener {
                        Toast.makeText(context, "Failed to update product", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    if (showDeletePostDialog) {
        AlertDialog(
            onDismissRequest = { showDeletePostDialog = false },
            title = { Text("Delete Product") },
            text = { Text("Are you sure you want to delete this product?") },
            confirmButton = {
                TextButton(onClick = {
                    val postRef = database.getReference("stores")
                        .child(storeId)
                        .child("posts")
                        .child(deletingPostId)

                    postRef.removeValue().addOnSuccessListener {
                        Toast.makeText(context, "Product deleted!", Toast.LENGTH_SHORT).show()
                        showDeletePostDialog = false
                    }.addOnFailureListener {
                        Toast.makeText(context, "Failed to delete product", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeletePostDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }



    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Store") },
            text = { Text("Are you sure you want to delete this store? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    FirebaseDatabase.getInstance().reference
                        .child("stores")
                        .child(storeId)
                        .removeValue()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Store deleted.", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            } else {
                                Toast.makeText(context, "Failed to delete store.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    showDeleteDialog = false
                }) {
                    Text("Yes", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
