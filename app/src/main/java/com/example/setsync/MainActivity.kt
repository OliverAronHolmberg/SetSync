package com.example.setsync

import android.content.Context
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
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// DATABASE IMPORTS
import com.example.setsync.data.AppDatabase
import com.example.setsync.data.WorkoutDao
import com.example.setsync.model.WorkoutSet

// Put this at the bottom of Exercise.kt or top of MainActivity.kt (outside the class)
data class ExerciseWithSets(
    val exercise: Exercise,
    val sets: MutableList<WorkoutSet> = mutableListOf()
)

val DarkBg = Color(0xFF1A1E2E)
val CardBg = Color(0xFF252A3D)
val Blue = Color(0xFF2196F3)
val TextGray = Color(0xFFAAAAAA)

// Helper function to save image to internal storage
fun saveImageToInternalStorage(context: Context, uri: Uri): Uri? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val fileName = "exercise_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, fileName)
        val outputStream = FileOutputStream(file)
        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        Uri.fromFile(file)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

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
fun HomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text("Hej, Magnus!", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("\"Håll dig stark, varje rep räknas.\"", color = TextGray, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp, bottom = 24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Veckans Träning", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(">", color = TextGray)
                }
                Text("Veckans totala träning", color = TextGray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(16.dp))
                val days = listOf("To", "Me", "Do", "Fr", "Sa", "Sö")
                val heights = listOf(0.4f, 0.7f, 0.3f, 0.9f, 0.5f, 0.2f)
                Row(modifier = Modifier.fillMaxWidth().height(80.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
                    days.forEachIndexed { index, day ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                            Box(modifier = Modifier.width(20.dp).height((heights[index] * 60).dp).clip(RoundedCornerShape(4.dp)).background(Blue))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(day, color = TextGray, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {},
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Blue),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Starta nytt pass", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Senaste pass", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Senaste pass", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("22 sets", color = TextGray, fontSize = 12.sp)
                }
                Text(">", color = TextGray)
            }
        }
    }
}

@Composable
fun ExercisesScreen(dao: WorkoutDao) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }

    val exercises by dao.getAllExercisesFlow().collectAsState(initial = emptyList())

    var showDialog by remember { mutableStateOf(false) }
    var editingExercise by remember { mutableStateOf<Exercise?>(null) }
    var currentName by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> 
            if (uri != null) {
                scope.launch(Dispatchers.IO) {
                    val localUri = saveImageToInternalStorage(context, uri)
                    if (localUri != null) {
                        withContext(Dispatchers.Main) {
                            selectedImageUri = localUri
                        }
                    }
                }
            }
        }
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
                        editingExercise = exercise
                        currentName = exercise.name
                        selectedImageUri = exercise.imageUri?.let { Uri.parse(it) }
                        showDialog = true
                    },
                    onDelete = {
                        scope.launch(Dispatchers.IO) { 
                            // Cleanup local file if it exists
                            exercise.imageUri?.let { uriStr ->
                                try {
                                    val file = File(Uri.parse(uriStr).path!!)
                                    if (file.exists()) file.delete()
                                } catch (e: Exception) {}
                            }
                            dao.deleteExercise(exercise) 
                        }
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
                        val currentEditing = editingExercise
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
            Box(
                modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)).background(DarkBg),
                contentAlignment = Alignment.Center
            ) {
                if (!exercise.imageUri.isNullOrBlank()) {
                    AsyncImage(
                        model = exercise.imageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = rememberVectorPainter(Icons.Default.Face)
                    )
                } else {
                    Icon(Icons.Default.Build, contentDescription = null, tint = TextGray)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(exercise.name, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))

            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Redigera", tint = Blue)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Radera", tint = Color.Red.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun OneRMScreen() {
    var weight by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<Float?>(null) }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text("1RM Räknare", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Vikt", color = Color.White, fontSize = 14.sp, modifier = Modifier.padding(bottom = 6.dp))
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    placeholder = { Text("10", color = TextGray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Blue,
                        unfocusedBorderColor = CardBg,
                        focusedContainerColor = CardBg,
                        unfocusedContainerColor = CardBg,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Right) }),
                    singleLine = true
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Reps", color = Color.White, fontSize = 14.sp, modifier = Modifier.padding(bottom = 6.dp))
                OutlinedTextField(
                    value = reps,
                    onValueChange = { reps = it },
                    placeholder = { Text("0", color = TextGray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Blue,
                        unfocusedBorderColor = CardBg,
                        focusedContainerColor = CardBg,
                        unfocusedContainerColor = CardBg,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        val w = weight.toFloatOrNull() ?: 0f
                        val r = reps.toFloatOrNull() ?: 0f
                        result = w * (1 + r / 30.0f)
                    }),
                    singleLine = true
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                focusManager.clearFocus()
                val w = weight.toFloatOrNull() ?: 0f
                val r = reps.toFloatOrNull() ?: 0f
                result = w * (1 + r / 30.0f)
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Blue),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Räkna ut", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        result?.let { rm ->
            Spacer(modifier = Modifier.height(24.dp))
            Text("Ditt uppskattade 1RM är:", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Text("%.1f".format(rm), color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Estimated Rep Maxes", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 12.dp))
                    val percentages = listOf(1 to 1.0f, 2 to 0.94f, 3 to 0.91f, 4 to 0.88f, 5 to 0.86f, 6 to 0.83f, 7 to 0.81f, 8 to 0.79f, 9 to 0.77f, 10 to 0.75f)
                    val pctLabels = listOf("100%", "94%", "91%", "88%", "86%", "83%", "81%", "79%", "77%", "75%")
                    percentages.forEachIndexed { index, (rep, pct) ->
                        Text("${rep}RM:", color = TextGray, fontSize = 14.sp)
                        Text("%.1f".format(rm * pct), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                        Text("${pctLabels[index]} of 1RM", color = TextGray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SessionsScreen() {
    var sessionLocation by remember { mutableStateOf("Orminge") }
    var currentWorkoutExercises by remember { mutableStateOf(mutableListOf<ExerciseWithSets>()) }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp).background(DarkBg)) {
        Text("Logga Pass", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(20.dp))

        Column(modifier = Modifier.verticalScroll(rememberScrollState()).weight(1f)) {
            currentWorkoutExercises.forEach { workoutExercise ->
                ExerciseSetCard(workoutExercise) {
                    // Logic to handle deleting/editing that entire exercise block
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), colors = CardDefaults.cardColors(containerColor = CardBg)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Button(onClick = { /* Select Exercise from Library */ }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Blue)) {
                    Text("Byt övning", color = Color.White)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { /* Create and Insert the Session + Sets to SQLite */ }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Blue)) {
                    Text("Spara övning", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable fun ExerciseSetCard(exerciseData: ExerciseWithSets, onDelete: () -> Unit) { /* ... */ }
