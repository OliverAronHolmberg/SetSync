package com.example.setsync

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.setsync.model.Exercise
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType

// DATABASE IMPORTS
import com.example.setsync.data.AppDatabase
import com.example.setsync.data.WorkoutDao
import com.example.setsync.model.WorkoutSet

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Put this at the bottom of Exercise.kt or top of MainActivity.kt (outside the class)
data class ExerciseWithSets(
    val exercise: Exercise,
    val sets: MutableList<WorkoutSet> = mutableListOf()
)

val DarkBg = Color(0xFF1A1E2E)
val CardBg = Color(0xFF252A3D)
val Blue = Color(0xFF2196F3)
val TextGray = Color(0xFFAAAAAA)

class MainActivity : ComponentActivity() {
    // Initialize the Database
    private val db by lazy {
        androidx.room.Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "gym-db"
        ).build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dao = db.workoutDao()
        setContent {
            GymApp(dao)
        }
    }
}

@Composable
fun GymApp(dao: WorkoutDao) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = CardBg) {
                val items = listOf("Hem" to Icons.Filled.Home, "Pass" to Icons.Filled.DateRange, "Övningar" to Icons.Filled.Star, "1RM" to Icons.Filled.Settings)
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        label = { Text(item.first, color = if (selectedTab == index) Blue else TextGray, fontSize = 12.sp) },
                        icon = { Icon(item.second, contentDescription = item.first, tint = if (selectedTab == index) Blue else TextGray) },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                    )
                }
            }
        },
        containerColor = DarkBg
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (selectedTab) {
                0 -> HomeScreen()
                1 -> SessionsScreen()
                2 -> ExercisesScreen(dao) // Passes the DAO to your exercise list
                3 -> OneRMScreen()
            }
        }
    }
}

@Composable
fun ExercisesScreen(dao: WorkoutDao) {
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }

    // Fix: collectAsState uses parameter 'initial', not 'initialValue'
    val exercises by dao.getAllExercisesFlow().collectAsState(initial = emptyList())

    var showDialog by remember { mutableStateOf(false) }
    var editingExercise by remember { mutableStateOf<Exercise?>(null) }
    var currentName by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    Column(modifier = Modifier.fillMaxSize().background(DarkBg).padding(20.dp)) {
        Text("Mina Övningar", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Sök övning...", color = TextGray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextGray) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                unfocusedBorderColor = CardBg, focusedContainerColor = CardBg, unfocusedContainerColor = CardBg
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        val filteredList = exercises.filter { it.name.contains(searchQuery, ignoreCase = true) }.reversed()

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            filteredList.forEach { exercise ->
                ExerciseCardItem(
                    exercise = exercise,
                    onEdit = {
                        // SET UP EDIT MODE
                        editingExercise = exercise
                        currentName = exercise.name
                        selectedImageUri = exercise.imageUri?.let { Uri.parse(it) }
                        showDialog = true
                    },
                    onDelete = {
                        scope.launch(Dispatchers.IO) { dao.deleteExercise(exercise) }
                    }
                )
            }
        }

        Button(
            onClick = {
                editingExercise = null
                currentName = ""
                selectedImageUri = null
                showDialog = true
            },
            modifier = Modifier.fillMaxWidth().height(56.dp).padding(top = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Blue),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Skapa ny övning", fontWeight = FontWeight.Bold)
        }
    }

    if (showDialog) {
        // This forces the dialog to sync with the editingExercise every time it opens
        LaunchedEffect(showDialog) {
            if (editingExercise != null) {
                currentName = editingExercise!!.name
                selectedImageUri = editingExercise!!.imageUri?.let { Uri.parse(it) }
            } else {
                currentName = ""
                selectedImageUri = null
            }
        }

        AlertDialog(
            onDismissRequest = {
                showDialog = false
                editingExercise = null
            },
            containerColor = CardBg,
            title = { Text(if (editingExercise == null) "Ny övning" else "Redigera övning", color = Color.White) },
            text = {
                Column {
                    OutlinedTextField(
                        value = currentName,
                        onValueChange = { currentName = it },
                        label = { Text("Namn (t.ex. Bänkpress)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = Blue,
                            unfocusedLabelColor = TextGray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Show a preview of the selected image in the dialog
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = null,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .align(Alignment.CenterHorizontally),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Button(
                        onClick = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkBg),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (selectedImageUri == null) "Välj bild från telefon" else "Ändra bild ✅")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (currentName.isNotBlank()) {
                        val currentEditing = editingExercise // Capture the exercise before resetting state
                        scope.launch(Dispatchers.IO) {
                            val exerciseToSave = if (currentEditing != null) {
                                currentEditing.copy(
                                    name = currentName,
                                    imageUri = selectedImageUri?.toString()
                                )
                            } else {
                                Exercise(name = currentName, imageUri = selectedImageUri?.toString())
                            }

                            dao.insertExercise(exerciseToSave)
                        }
                        showDialog = false
                        editingExercise = null
                    }
                }) { Text("Spara", color = Blue, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    editingExercise = null
                }) { Text("Avbryt", color = TextGray) }
            }
        )
    }
}

@Composable
fun ExerciseCardItem(exercise: Exercise, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (exercise.imageUri != null) {
                AsyncImage(
                    model = exercise.imageUri,
                    contentDescription = null,
                    modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(modifier = Modifier.size(50.dp).background(DarkBg, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Build, contentDescription = null, tint = TextGray)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(exercise.name, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))

            // EDIT BUTTON
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Redigera", tint = Blue)
            }
            // DELETE BUTTON
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Radera", tint = Color.Red.copy(alpha = 0.7f))
            }
        }
    }
}

// Add your existing HomeScreen, SessionsScreen, and OneRMScreen logic below
@Composable fun HomeScreen() { /* ... */ }
@Composable fun SessionsScreen() { /* ... */ }
@Composable fun OneRMScreen() { /* ... */ }
@Composable fun ExerciseSetCard(exerciseData: ExerciseWithSets, onDelete: () -> Unit) { /* ... */ }
