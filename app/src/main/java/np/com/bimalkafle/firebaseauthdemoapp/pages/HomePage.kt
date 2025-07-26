package np.com.bimalkafle.firebaseauthdemoapp.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import np.com.bimalkafle.firebaseauthdemoapp.AuthState
import np.com.bimalkafle.firebaseauthdemoapp.AuthViewModel

@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var taskText by remember { mutableStateOf("") }
    var editingTaskIndex by remember { mutableStateOf(-1) }
    val tasks = remember { mutableStateListOf<String>() }
    val authState = authViewModel.authState.observeAsState()

    LaunchedEffect(authState.value) {
        when(authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("login") {
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
            }
            else -> Unit
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Home Page", fontSize = 32.sp)

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TextButton(onClick = {
                    authViewModel.signout()
                }) {
                    Text(text = "Sign out")
                }

                TextButton(onClick = {
                    editingTaskIndex = -1
                    showAddTaskDialog = true
                }) {
                    Text(text = "Add Task")
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            items(tasks) { task ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = task,
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f)
                        )

                        Row {
                            IconButton(
                                onClick = {
                                    editingTaskIndex = tasks.indexOf(task)
                                    taskText = task
                                    showAddTaskDialog = true
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit task"
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = { tasks.remove(task) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete task"
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add/Edit Task Dialog
    if (showAddTaskDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddTaskDialog = false
                editingTaskIndex = -1
                taskText = ""
            },
            title = { Text(if (editingTaskIndex >= 0) "Edit Task" else "Add New Task") },
            text = {
                OutlinedTextField(
                    value = taskText,
                    onValueChange = { taskText = it },
                    label = { Text("Task description") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (taskText.isNotBlank()) {
                            if (editingTaskIndex >= 0) {
                                // Update existing task
                                tasks[editingTaskIndex] = taskText
                            } else {
                                // Add new task
                                tasks.add(taskText)
                            }
                            taskText = ""
                            showAddTaskDialog = false
                            editingTaskIndex = -1
                        }
                    }
                ) {
                    Text(if (editingTaskIndex >= 0) "Update" else "Add")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddTaskDialog = false
                        editingTaskIndex = -1
                        taskText = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}