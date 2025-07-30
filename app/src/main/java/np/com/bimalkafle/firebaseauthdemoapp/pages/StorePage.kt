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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.SwipeLeft
import androidx.compose.material.icons.filled.SwipeRight
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorePage(
    modifier: Modifier = Modifier,
    storeId: String,
    authViewModel: AuthViewModel,
    navController: NavController
) {
    val scrollState = rememberScrollState()
    var showProductDialog by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<Map<String, String>?>(null) }
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
    var storeImageUrl by remember { mutableStateOf("") }
    val shape = RoundedCornerShape(16.dp)


    var posts by remember { mutableStateOf(listOf<Map<String, String>>()) }

    LaunchedEffect(storeId) {
        val storeRef = database.getReference("stores").child(storeId)

        storeRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                storeName = snapshot.child("name").getValue(String::class.java) ?: ""
                storeImageUrl = snapshot.child("imageUrl").getValue(String::class.java) ?: ""
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
            if (storeImageUrl.isNotBlank()) {
                AsyncImage(
                    model = storeImageUrl,
                    contentDescription = "$storeName banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(
                text = storeName,
                style = MaterialTheme.typography.headlineMedium,
                color = Color(60, 140, 64)
            )
            Text(
                text = "by $ownerName",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (isOwner == true) {


                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            newTitle = ""
                            newDescription = ""
                            newPrice = ""
                            newImageUrl = ""
                            showAddDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(76, 176, 80)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f).height(42.dp)
                    ) {
                        Text("Add Product")
                    }



                    Button(
                        onClick = { showDeleteDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(246, 53, 62)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f).height(42.dp)

                    ) {
                        Text("Delete Store", color = Color.White)
                    }

                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            if (isOwner == true) {
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    color = Color(0xFFE0E0E0),
                    thickness = 1.dp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color(0xFFFAFAFA),


                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SwipeLeft, contentDescription = "Swipe left", tint = Color(0xFF757575))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Edit",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFF424242),
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Delete",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFF424242),
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.SwipeRight, contentDescription = "Swipe right", tint = Color(0xFF757575))
                        }
                    }
                }
            }

            Text("Products:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {

                items(posts) { post ->
                    val dismissState = rememberDismissState(
                        confirmValueChange = {
                            when (it) {
                                DismissValue.DismissedToStart -> {
                                    newTitle = post["title"] ?: ""
                                    newDescription = post["description"] ?: ""
                                    newPrice = post["price"] ?: ""
                                    newImageUrl = post["imageUrl"] ?: ""
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


                    val productCard = @androidx.compose.runtime.Composable {
                        Card(
                            modifier = Modifier
                                .clickable {
                                    selectedProduct = post
                                    showProductDialog = true
                                }
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp)) {
                                AsyncImage(
                                    model = post["imageUrl"] ?: "",
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = post["title"] ?: "No Title",
                                        fontSize = 18.sp,
                                        color = Color.Black,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = post["description"] ?: "No Description",
                                        fontSize = 14.sp,
                                        color = Color.DarkGray,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "₱${post["price"] ?: "0"}",
                                        fontSize = 16.sp,
                                        color = Color(0xFF4CAF50),
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }
                    }

                    if (isOwner == true) {
                        Box(modifier = Modifier.padding(vertical = 0.dp)) {
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
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(color),
                                        contentAlignment = alignment
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(
                                                start = if (direction == DismissDirection.StartToEnd) 20.dp else 0.dp,
                                                end = if (direction == DismissDirection.EndToStart) 20.dp else 0.dp
                                            )
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = Color.White
                                            )
                                        }
                                    }
                                },
                                dismissContent = { productCard() }
                            )
                        }
                    } else {
                        productCard()
                    }
                }


            }

        }
    }
    if (showProductDialog && selectedProduct != null) {
        ProductsPage(
            title = selectedProduct?.get("title") ?: "",
            description = selectedProduct?.get("description") ?: "",
            price = selectedProduct?.get("price") ?: "",
            imageUrl = selectedProduct?.get("imageUrl") ?: "",
            onCancel = { showProductDialog = false },
            onAddToCart = {
                selectedProduct?.let { product ->
                    authViewModel.addToCart(storeName, product) { success, message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        if (success) {
                            showProductDialog = false
                        }
                    }
                }
            }
        )
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
                        label = { Text("Description") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp, max = 200.dp)
                            .verticalScroll(scrollState),
                        maxLines = 10
                    )
                    OutlinedTextField(
                        value = newPrice,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) {
                                newPrice = input
                            }
                        },
                        label = {
                            Text(
                                text = "₱ Price",
                                fontSize = 16.sp,
                                color = Color(0xFF4CAF50), // Green color
                                style = MaterialTheme.typography.labelLarge
                            )
                        },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
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
                        "imageUrl" to newImageUrl
                    )
                    postRef.setValue(postData).addOnSuccessListener {
                        Toast.makeText(context, "Product added!", Toast.LENGTH_SHORT).show()
                        newTitle = ""
                        newDescription = ""
                        newPrice = ""
                        newImageUrl = ""
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
                    Text("Cancel" , color = MaterialTheme.colorScheme.primary)
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
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .heightIn(max = 100.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        OutlinedTextField(
                            value = newDescription,
                            onValueChange = { newDescription = it },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = false,
                            maxLines = 10
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newPrice,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) {
                                newPrice = input
                            }
                        },
                        label = { Text(
                            text = "₱rice",
                            fontSize = 16.sp,
                            color = Color(0xFF4CAF50),
                            style = MaterialTheme.typography.labelLarge
                        ) },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )


                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newImageUrl,
                        onValueChange = { newImageUrl = it },
                        label = { Text("Image URL (https://...)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { val postRef = database.getReference("stores")
                    .child(storeId)
                    .child("posts")
                    .child(editingPostId ?: return@TextButton)  // graceful null handling


                    val updatedData = mapOf(
                        "id" to editingPostId,
                        "title" to newTitle,
                        "description" to newDescription,
                        "price" to newPrice,
                        "imageUrl" to newImageUrl
                    )


                    postRef.setValue(updatedData).addOnSuccessListener {
                        Toast.makeText(context, "Product updated!", Toast.LENGTH_SHORT).show()
                        showEditDialog = false
                    }.addOnFailureListener {
                        Toast.makeText(context, "Failed to update product", Toast.LENGTH_SHORT).show()
                    }}) {
                    Text("Save")
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