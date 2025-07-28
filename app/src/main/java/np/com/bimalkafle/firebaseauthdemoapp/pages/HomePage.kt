package np.com.bimalkafle.firebaseauthdemoapp.pages

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import np.com.bimalkafle.firebaseauthdemoapp.AuthViewModel

@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            scope.launch {
                val manufacturers = authViewModel.fetchManufacturers()
                if (manufacturers == null) {
                    Toast.makeText(context, "Failed to fetch manufacturers", Toast.LENGTH_LONG).show()
                } else if (manufacturers.isEmpty()) {
                    Toast.makeText(context, "Manufacturer list is empty", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context,
                        "Found ${manufacturers} manufacturers",
                        Toast.LENGTH_LONG).show()
                }
            }
        }) {
            Text(text = "Fetch Manufacturers")
        }
    }
}