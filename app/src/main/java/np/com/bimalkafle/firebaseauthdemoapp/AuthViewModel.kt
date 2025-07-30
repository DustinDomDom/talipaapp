
//filename: AuthViewModel.kt
package np.com.bimalkafle.firebaseauthdemoapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import java.util.*
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError


class AuthViewModel : ViewModel() {

    private val auth : FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    init {
        checkAuthStatus()
    }

    data class Store(
        val id: String = "",
        val name: String = "",
        val owner: String = "",
        val posts: Map<String, Post> = emptyMap(),
        val imageUrl: String = "",
        val description: String = ""
    )



    data class Post(
        val id: String = "",
        val title: String = "",
        val description: String = ""
    )


    data class CartItem(
        val id: String = "",
        val owner: String = "",
        val store: String = "",
        val product: String = "",
        val price: String = "",
        val imageUrl: String = "",
        val description: String = "",
        val quantity: Int = 1
    )



    fun updateCartItemQuantity(cartItemId: String, newQuantity: Int, onResult: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid ?: return onResult(false)
        val cartRef = FirebaseDatabase.getInstance().getReference("carts").child(userId).child(cartItemId)

        cartRef.child("quantity").setValue(newQuantity)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun fetchCartItemsForCurrentUser(onResult: (List<CartItem>) -> Unit) {
        val user = getCurrentUser() ?: return onResult(emptyList())
        val uid = user.uid

        val dbRef = FirebaseDatabase.getInstance().getReference("carts").child(uid)
        dbRef.get().addOnSuccessListener { snapshot ->
            val cartItems = mutableListOf<CartItem>()
            snapshot.children.forEach { child ->
                val item = child.getValue(CartItem::class.java)
                if (item != null) {
                    cartItems.add(item)
                }
            }
            onResult(cartItems)
        }.addOnFailureListener {
            onResult(emptyList())
        }
    }

    fun deleteCartItem(cartItemId: String, onResult: (Boolean) -> Unit = {}) {
        val userId = auth.currentUser?.uid ?: return onResult(false)

        val database = FirebaseDatabase.getInstance()
        val cartItemRef = database.getReference("carts").child(userId).child(cartItemId)

        cartItemRef.removeValue()
            .addOnSuccessListener {
                onResult(true)
            }
            .addOnFailureListener {
                onResult(false)
            }
    }

    fun placeOrder(
        cartItems: List<CartItem>,
        totalPrice: Int,
        onComplete: () -> Unit
    ) {
        val user = auth.currentUser ?: return
        val userId = user.uid
        val userName = user.displayName ?: user.email ?: "Unknown User"

        val transactionsRef = FirebaseDatabase.getInstance().getReference("transactions").child(userId)
        val transactionId = transactionsRef.push().key ?: return

        val items = cartItems.map {
            mapOf(
                "store" to it.store,
                "product" to it.product,
                "quantity" to it.quantity,
                "price" to it.price
            )
        }

        val transactionData = mapOf(
            "id" to transactionId,
            "owner" to userName,
            "items" to items,
            "totalPrice" to totalPrice.toString(),
            "timestamp" to System.currentTimeMillis().toString()
        )

        transactionsRef.child(transactionId).setValue(transactionData).addOnSuccessListener {
            // Clear cart after placing order
            FirebaseDatabase.getInstance().getReference("carts").child(userId).removeValue().addOnCompleteListener {
                onComplete()
            }
        }
    }

    fun addToCart(
        storeName: String,
        product: Map<String, String>,
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val user = auth.currentUser ?: return onResult(false, "Not logged in")
        val userId = user.uid
        val userName = user.displayName ?: user.email ?: "Unknown User"
        val cartRef = FirebaseDatabase.getInstance().getReference("carts").child(userId)

        cartRef.get().addOnSuccessListener { snapshot ->
            var matchingItemSnapshot: DataSnapshot? = null

            for (child in snapshot.children) {
                val existingProduct = child.child("product").getValue(String::class.java)
                if (existingProduct == product["title"]) {
                    matchingItemSnapshot = child
                    break
                }
            }

            if (matchingItemSnapshot != null) {
                // If product exists: only increment quantity
                val currentQuantity = matchingItemSnapshot.child("quantity").getValue(Int::class.java) ?: 1
                val newQuantity = currentQuantity + 1

                val updates = mapOf(
                    "quantity" to newQuantity
                )

                cartRef.child(matchingItemSnapshot.key!!).updateChildren(updates).addOnSuccessListener {
                    onResult(true, "Cart updated: +1 ${product["title"]}")
                }.addOnFailureListener {
                    onResult(false, "Failed to update cart")
                }

            } else {
                // New product: add with quantity = 1
                val cartItemId = cartRef.push().key ?: return@addOnSuccessListener onResult(false, "Failed to generate ID")

                val cartData = mapOf(
                    "id" to cartItemId,
                    "owner" to userName,
                    "store" to storeName,
                    "product" to product["title"],
                    "price" to product["price"], // this is the unit price
                    "originalPrice" to product["price"], // just for reference
                    "quantity" to 1,
                    "imageUrl" to product["imageUrl"],
                    "description" to product["description"]
                )

                cartRef.child(cartItemId).setValue(cartData).addOnSuccessListener {
                    onResult(true, "Added to cart!")
                }.addOnFailureListener {
                    onResult(false, "Failed to add to cart.")
                }
            }
        }.addOnFailureListener {
            onResult(false, "Failed to check cart: ${it.message}")
        }
    }



    fun checkAuthStatus(){
        if(auth.currentUser==null){
            _authState.value = AuthState.Unauthenticated
        }else{
            _authState.value = AuthState.Authenticated
        }
    }
    fun createStore(name: String, imageUrl: String, description: String, onResult: (Boolean, String) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            onResult(false, "User not authenticated")
            return
        }

        val storeId = UUID.randomUUID().toString()
        val store = Store(
            id = storeId,
            name = name,
            owner = user.displayName ?: user.email ?: "Unknown Owner",
            imageUrl = imageUrl,
            description = description,
            posts = emptyMap()
        )

        val dbRef = FirebaseDatabase.getInstance().getReference("stores").child(storeId)
        dbRef.setValue(store)
            .addOnSuccessListener {
                onResult(true, "Store created successfully")
            }
            .addOnFailureListener {
                onResult(false, it.message ?: "Failed to create store")
            }
    }



    fun deletePost(postId: String) {
        val uid = auth.currentUser?.uid ?: return
        val db = FirebaseDatabase.getInstance().reference
        db.child("stores")
            .child(uid)
            .child("products")
            .child(postId)
            .removeValue()
    }



    fun fetchStores(onResult: (List<Store>) -> Unit) {
        val dbRef = FirebaseDatabase.getInstance().getReference("stores")

        dbRef.get()
            .addOnSuccessListener { dataSnapshot ->
                val storeList = mutableListOf<Store>()
                dataSnapshot.children.forEach { child ->
                    val store = child.getValue(Store::class.java)
                    store?.let { storeList.add(it) }
                }
                onResult(storeList)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    fun login(email : String,password : String){

        if(email.isEmpty() || password.isEmpty()){
            _authState.value = AuthState.Error("Email or password can't be empty")
            return
        }
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener{task->
                if (task.isSuccessful){
                    _authState.value = AuthState.Authenticated
                }else{
                    _authState.value = AuthState.Error(task.exception?.message?:"Something went wrong")
                }
            }
    }

    fun signout(){
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }

    fun getCurrentUser(): FirebaseUser? {
        return FirebaseAuth.getInstance().currentUser
    }

    fun signup(email: String, password: String, username: String) {
        _authState.value = AuthState.Loading

        val normalizedUsername = username.trim().lowercase()
        val usersRef = FirebaseDatabase.getInstance().getReference("users")

        usersRef.orderByChild("name").equalTo(normalizedUsername)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        _authState.value = AuthState.Error("Username '$normalizedUsername' is already taken.")
                    } else {
                        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val uid = task.result?.user?.uid ?: return@addOnCompleteListener
                                    val userRef = usersRef.child(uid)

                                    val userData = mapOf(
                                        "name" to normalizedUsername,
                                        "email" to email
                                    )

                                    userRef.setValue(userData)
                                        .addOnSuccessListener {
                                            _authState.value = AuthState.Authenticated
                                        }
                                        .addOnFailureListener {
                                            _authState.value = AuthState.Error("Failed to save user data.")
                                        }
                                } else {
                                    _authState.value = AuthState.Error(task.exception?.message ?: "Signup failed.")
                                }
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    _authState.value = AuthState.Error("Database error: ${error.message}")
                }
            })
    }

}


sealed class AuthState{
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message : String) : AuthState()
}