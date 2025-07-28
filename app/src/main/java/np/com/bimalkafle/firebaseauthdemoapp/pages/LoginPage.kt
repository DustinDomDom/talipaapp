package np.com.bimalkafle.firebaseauthdemoapp.pages

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import np.com.bimalkafle.firebaseauthdemoapp.AuthState
import np.com.bimalkafle.firebaseauthdemoapp.AuthViewModel
import np.com.bimalkafle.firebaseauthdemoapp.R
import np.com.bimalkafle.firebaseauthdemoapp.ui.theme.Green40
import androidx.compose.ui.layout.ContentScale
import androidx.compose.runtime.livedata.observeAsState

@Composable
fun LoginPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Authenticated -> navController.navigate("home")
            is AuthState.Error -> Toast.makeText(
                context,
                (authState.value as AuthState.Error).message,
                Toast.LENGTH_SHORT
            ).show()
            else -> Unit
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // 🔹 Background Image
        Image(
            painter = painterResource(id = R.drawable.wetmarket),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 🔹 Foreground Login Form
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ✅ Girl Illustration stays visible
                Image(
                    painter = painterResource(id = R.drawable.colored),
                    contentDescription = "Login Illustration",
                    modifier = Modifier
                        .height(200.dp)
                        .padding(top = 32.dp),
                    contentScale = ContentScale.Fit
                )

                // ✅ Styled "TalipaApp" text only
                Text(
                    text = "TalipaApp",
                    fontSize = 32.sp,
                    color = Color.White,
                    modifier = Modifier
                        .background(Green40, shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // ✅ Credentials section in white box
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .background(Color.White, shape = RoundedCornerShape(16.dp))
                        .padding(24.dp)
                        .fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(text = "Email") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(text = "Password") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = { authViewModel.login(email, password) },
                        enabled = authState.value != AuthState.Loading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Green40
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Login")
                    }

                    TextButton(onClick = { navController.navigate("signup") }) {
                        Text(
                            text = "Don't have an account? Signup",
                            color = Green40
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}