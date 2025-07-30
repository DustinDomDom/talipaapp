// ShoppingCartPage.kt
package np.com.bimalkafle.firebaseauthdemoapp.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import np.com.bimalkafle.firebaseauthdemoapp.AuthViewModel
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.rememberDismissState
import com.google.firebase.database.*
import androidx.compose.foundation.background



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingCartPage(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    var cartItems by remember { mutableStateOf<List<AuthViewModel.CartItem>>(emptyList()) }
    var totalPrice by remember { mutableStateOf(0) } // <-- NEW

    // Helper function to calculate total
    fun calculateTotal(items: List<AuthViewModel.CartItem>): Int {
        return items.sumOf {
            val price = it.price.toIntOrNull() ?: 0
            val quantity = it.quantity
            price * quantity
        }
    }

    LaunchedEffect(Unit) {
        authViewModel.fetchCartItemsForCurrentUser {
            cartItems = it
            totalPrice = calculateTotal(it) // <-- update total
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shopping Cart") },
                actions = {
                    var showPlaceOrderDialog by remember { mutableStateOf(false) }

                    Button(onClick = {
                        if (cartItems.isNotEmpty()) {
                            showPlaceOrderDialog = true
                        }
                    }) {
                        Text("Place Order")
                    }

                    if (showPlaceOrderDialog) {
                        AlertDialog(
                            onDismissRequest = { showPlaceOrderDialog = false },
                            title = { Text("Place Order?") },
                            text = { Text("Total: ₱${String.format("%,d", totalPrice)}") },
                            confirmButton = {
                                TextButton(onClick = {
                                    authViewModel.placeOrder(cartItems, totalPrice) {
                                        // After placing the order, clear cart items
                                        authViewModel.fetchCartItemsForCurrentUser {
                                            cartItems = it
                                            totalPrice = calculateTotal(it)
                                        }
                                    }
                                    showPlaceOrderDialog = false
                                }) {
                                    Text("Proceed")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showPlaceOrderDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }

                }
            )
        }
    ) { padding ->
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Your cart is currently empty.")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                cartItems.forEach { item ->
                    var showDeleteDialog by remember { mutableStateOf(false) }
                    val dismissState = rememberDismissState(
                        confirmValueChange = { dismissValue ->
                            if (dismissValue == DismissValue.DismissedToStart) {
                                showDeleteDialog = true
                                false
                            } else false
                        }
                    )

                    if (showDeleteDialog) {
                        AlertDialog(
                            onDismissRequest = { showDeleteDialog = false },
                            title = { Text("Delete Item") },
                            text = { Text("Are you sure you want to remove this item from your cart?") },
                            confirmButton = {
                                TextButton(onClick = {
                                    authViewModel.deleteCartItem(item.id) { success ->
                                        if (success) {
                                            authViewModel.fetchCartItemsForCurrentUser { updatedItems ->
                                                cartItems = updatedItems
                                                totalPrice = calculateTotal(updatedItems) // <-- update total
                                            }
                                        }
                                    }
                                    showDeleteDialog = false
                                }) {
                                    Text("Delete")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }

                    SwipeToDismiss(
                        state = dismissState,
                        background = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.Red),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.padding(end = 20.dp)
                                )
                            }
                        },
                        directions = setOf(DismissDirection.EndToStart),
                        dismissContent = {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                            ) {
                                Row(modifier = Modifier.padding(12.dp)) {
                                    AsyncImage(
                                        model = item.imageUrl,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.product,
                                            fontSize = 18.sp,
                                            color = Color.Black,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = item.description,
                                            fontSize = 14.sp,
                                            color = Color.DarkGray,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "₱${String.format("%,d", item.price.toIntOrNull() ?: 0)}",
                                                fontSize = 16.sp,
                                                color = Color(0xFF4CAF50),
                                                style = MaterialTheme.typography.labelLarge
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            TextButton(onClick = {
                                                if (item.quantity > 1) {
                                                    authViewModel.updateCartItemQuantity(item.id, item.quantity - 1) {
                                                        authViewModel.fetchCartItemsForCurrentUser {
                                                            cartItems = it
                                                            totalPrice = calculateTotal(it) // <-- update total
                                                        }
                                                    }
                                                }
                                            }) {
                                                Text("-")
                                            }
                                            Text(text = item.quantity.toString())
                                            TextButton(onClick = {
                                                authViewModel.updateCartItemQuantity(item.id, item.quantity + 1) {
                                                    authViewModel.fetchCartItemsForCurrentUser {
                                                        cartItems = it
                                                        totalPrice = calculateTotal(it) // <-- update total
                                                    }
                                                }
                                            }) {
                                                Text("+")
                                            }
                                        }

                                    }
                                }
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Show total below cart
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Total: ₱${String.format("%,d", totalPrice)}",
                        fontSize = 20.sp,
                        color = Color.Black,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    }
}
