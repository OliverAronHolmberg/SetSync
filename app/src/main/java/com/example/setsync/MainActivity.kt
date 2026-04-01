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

import com.example.setsync.model.WorkoutSet

val DarkBg = Color(0xFF1A1E2E)
val CardBg = Color(0xFF252A3D)
val Blue = Color(0xFF2196F3)
val TextGray = Color(0xFFAAAAAA)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GymApp()
        }
    }
}

@Composable
fun GymApp() {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = CardBg) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    label = { Text("Hem", color = if (selectedTab == 0) Blue else TextGray, fontSize = 12.sp) },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Hem", tint = if (selectedTab == 0) Blue else TextGray) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    label = { Text("Pass", color = if (selectedTab == 1) Blue else TextGray, fontSize = 12.sp) },
                    icon = { Icon(Icons.Filled.DateRange, contentDescription = "Pass", tint = if (selectedTab == 1) Blue else TextGray) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    label = { Text("Övningar", color = if (selectedTab == 2) Blue else TextGray, fontSize = 12.sp) },
                    icon = { Icon(Icons.Filled.Star, contentDescription = "Övningar", tint = if (selectedTab == 2) Blue else TextGray) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    label = { Text("1RM", color = if (selectedTab == 3) Blue else TextGray, fontSize = 12.sp) },
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "1RM", tint = if (selectedTab == 3) Blue else TextGray) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                )
            }
        },
        containerColor = DarkBg
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                0 -> HomeScreen()
                1 -> SessionsScreen()
                2 -> ExercisesScreen()
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
        Text("1RM Räknare", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
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
            Text("Ditt uppskattade 1RM är:", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Text("%.1f".format(rm), color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)

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
fun ExercisesScreen() {
    var searchQuery by remember { mutableStateOf("") }
    val exercises = remember { mutableStateListOf<Exercise>() }
    var showAddDialog by remember { mutableStateOf(false) }
    var newExName by remember { mutableStateOf("") }
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
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                unfocusedBorderColor = CardBg,
                focusedContainerColor = CardBg,
                unfocusedContainerColor = CardBg
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Filters list and puts latest on top
        val filteredList = exercises.filter { it.name.contains(searchQuery, ignoreCase = true) }.reversed()

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            filteredList.forEach { exercise ->
                ExerciseCardItem(
                    exercise = exercise,
                    onDelete = { exercises.remove(exercise) }
                )
            }
        }

        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier.fillMaxWidth().height(56.dp).padding(top = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Blue),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Skapa ny övning", fontWeight = FontWeight.Bold)
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = CardBg,
            title = { Text("Ny övning", color = Color.White) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newExName,
                        onValueChange = { newExName = it },
                        label = { Text("Namn (t.ex. Bänkpress)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkBg),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (selectedImageUri == null) "Välj bild från telefon" else "Bild vald ✅")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newExName.isNotBlank()) {
                        exercises.add(Exercise(name = newExName, imageUri = selectedImageUri?.toString()))
                        newExName = ""
                        selectedImageUri = null
                        showAddDialog = false
                    }
                }) { Text("Spara", color = Blue) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddDialog = false
                    newExName = ""
                    selectedImageUri = null
                }) { Text("Avbryt", color = TextGray) }
            }
        )
    }
}

@Composable
fun ExerciseCardItem(exercise: Exercise, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun ExerciseCard(exercise: Exercise, onDelete: () -> Unit, onEdit: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Display the image from Uri
            if (exercise.imageUri != null) {
                AsyncImage(
                    model = exercise.imageUri,
                    contentDescription = null,
                    modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(modifier = Modifier.size(50.dp).background(DarkBg, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Place, contentDescription = null, tint = TextGray)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))
            Text(exercise.name, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))

            // EDIT & DELETE BUTTONS
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = null, tint = TextGray, modifier = Modifier.size(20.dp)) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(20.dp)) }
        }
    }
}
@Composable
fun SessionsScreen() {
    var sessionLocation by remember { mutableStateOf("Orminge") }
    // A complex mutable state holding our list of exercises and their sets
    var currentWorkoutExercises by remember { mutableStateOf(mutableListOf<ExerciseWithSets>()) }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp).background(DarkBg)) {
        // ... (Header and timer logic from mockup)
        Text("Logga Pass", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(20.dp))

        // The Downwards List of Exercises + Sets
        Column(modifier = Modifier.verticalScroll(rememberScrollState()).weight(1f)) {
            currentWorkoutExercises.forEach { workoutExercise ->
                ExerciseSetCard(workoutExercise) {
                    // Logic to handle deleting/editing that entire exercise block
                }
            }
        }

        // --- Input Section (Mockup Design) ---
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

// Data holder for a single exercise and its sets during the active workout
data class ExerciseWithSets(val exercise: Exercise, val sets: MutableList<WorkoutSet>)

@Composable
fun ExerciseSetCard(exerciseData: ExerciseWithSets, onDelete: () -> Unit) {
    // Implement the detailed "Set 1, 2, 3" card logic seen in your mockup here
}

