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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// DATABASE IMPORTS
import com.example.setsync.data.AppDatabase
import com.example.setsync.data.WorkoutDao
import com.example.setsync.model.WorkoutSet
import com.example.setsync.model.WorkoutSession

// Put this at the bottom of Exercise.kt or top of MainActivity.kt (outside the class)
data class ExerciseWithSets(
    val exercise: Exercise,
    val sets: List<WorkoutSet> = emptyList()
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
    var selectedTab by remember { mutableIntStateOf(0) }
    var startNewSessionTrigger by remember { mutableStateOf(false) }
    var sessionToViewTrigger by remember { mutableStateOf<WorkoutSession?>(null) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = CardBg) {
                val items = listOf(
                    "Home" to Icons.Filled.Home,
                    "Sessions" to Icons.Filled.Assignment,
                    "Exercices" to Icons.Filled.FitnessCenter,
                    "Calculator" to Icons.Filled.Calculate,
                    "1RM tracker" to Icons.Filled.FitnessCenter



                )
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                            if (index != 1) {
                                sessionToViewTrigger = null
                            }
                        },
                        label = { Text(item.first, fontSize = 10.sp) },
                        icon = { Icon(item.second, contentDescription = item.first) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Blue,
                            selectedTextColor = Blue,
                            unselectedIconColor = TextGray,
                            unselectedTextColor = TextGray,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        },
        containerColor = DarkBg
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            when (selectedTab) {
                0 -> HomeScreen(dao, onStartNewSession = {
                    startNewSessionTrigger = true
                    selectedTab = 1
                }, onSessionClick = { session ->
                    sessionToViewTrigger = session
                    selectedTab = 1
                })
                1 -> {
                    SessionsScreen(
                        dao = dao,
                        startNewSessionTrigger = startNewSessionTrigger,
                        viewSessionTrigger = sessionToViewTrigger,
                        onSessionHandled = {
                            startNewSessionTrigger = false
                            sessionToViewTrigger = null
                        }
                    )
                }
                2 -> ExercisesScreen(dao)
                3 -> OneRMScreen()
            }
        }
    }
}

@Composable
fun HomeScreen(dao: WorkoutDao, onStartNewSession: () -> Unit, onSessionClick: (WorkoutSession) -> Unit) {
    val sessions by dao.getAllSessionsFlow().collectAsState(initial = emptyList())

    val monthlyStats = remember(sessions) {
        val stats = IntArray(12) { 0 }
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val cal = Calendar.getInstance()
        val currentYear = cal.get(Calendar.YEAR)

        sessions.forEach { session ->
            try {
                val date = sdf.parse(session.date)
                if (date != null) {
                    cal.time = date
                    if (cal.get(Calendar.YEAR) == currentYear) {
                        stats[cal.get(Calendar.MONTH)]++
                    }
                }
            } catch (e: Exception) {
            }
        }
        stats
    }

    Column(

        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        
        Text(
            text = "Hem", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Chart Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 16.dp, start = 8.dp, end = 8.dp)
                    .height(150.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val months = listOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC")
                    val calendar = Calendar.getInstance()
                    val currentYear = calendar.get(Calendar.YEAR)

                    months.forEachIndexed { index, month ->
                        val count = monthlyStats[index]
                        calendar.set(Calendar.YEAR, currentYear)
                        calendar.set(Calendar.MONTH, index)
                        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (count > 0) {
                                Text(
                                    text = count.toString(),
                                    color = TextGray,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .width(20.dp)
                                        .fillMaxHeight((count.toFloat() / daysInMonth).coerceAtMost(1f) * 0.75f)
                                        .background(Blue)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .padding(bottom = 2.dp)
                                        .width(20.dp)
                                        .height(4.dp)
                                        .background(TextGray.copy(alpha = 0.3f))
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = month,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onStartNewSession,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Blue),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Skapa nytt pass", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("Senaste pass", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(16.dp))

        if (sessions.isEmpty()) {
            Text("Inga pass loggade än", color = TextGray, fontSize = 14.sp)
        } else {
            sessions.take(10).forEach { session ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clickable { onSessionClick(session) },
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(DarkBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.DirectionsRun, contentDescription = null, tint = Blue)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Tidigare pass", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(session.date, color = TextGray, fontSize = 14.sp)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextGray)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
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

    Column(modifier = Modifier
        .fillMaxSize()
        .background(DarkBg)
        .padding(20.dp)) {
        Text("Mina Övningar", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
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
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
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
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(top = 8.dp),
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
fun ExerciseCardItem(exercise: Exercise, onEdit: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkBg),
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

        Text("1RM Räknare", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
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
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = Blue,
                        unfocusedBorderColor = CardBg,
                        focusedContainerColor = CardBg,
                        unfocusedContainerColor = CardBg
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
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
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
fun SessionsScreen(
    dao: WorkoutDao,
    startNewSessionTrigger: Boolean = false,
    viewSessionTrigger: WorkoutSession? = null,
    onSessionHandled: () -> Unit = {}
) {
    var showEditor by remember { mutableStateOf(false) }
    var editingSession by remember { mutableStateOf<WorkoutSession?>(null) }
    var expandedSessionId by remember { mutableStateOf<Int?>(null) }
    val sessions by dao.getAllSessionsFlow().collectAsState(initial = emptyList())
    val exercises by dao.getAllExercisesFlow().collectAsState(initial = emptyList())

    LaunchedEffect(startNewSessionTrigger, viewSessionTrigger) {
        if (startNewSessionTrigger) {
            editingSession = null
            showEditor = true
            onSessionHandled()
        } else if (viewSessionTrigger != null) {
            expandedSessionId = viewSessionTrigger.id
            onSessionHandled()
        }
    }

    if (showEditor) {
        SessionEditor(
            dao = dao,
            existingSession = editingSession,
            onBack = {
                showEditor = false
                editingSession = null
            }
        )
    } else {
        Column(modifier = Modifier.fillMaxSize().background(DarkBg).padding(20.dp)) {
            Text("Pass", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (sessions.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                        Text("Inga pass loggade än!", color = TextGray, fontSize = 16.sp)
                    }
                } else {
                    sessions.forEach { session ->
                        SessionCardItem(
                            session = session,
                            dao = dao,
                            exercises = exercises,
                            isExpanded = expandedSessionId == session.id,
                            onExpandToggle = {
                                expandedSessionId = if (expandedSessionId == session.id) null else session.id
                            },
                            onEdit = {
                                editingSession = session
                                showEditor = true
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    editingSession = null
                    showEditor = true
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Starta nytt pass", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SessionCardItem(
    session: WorkoutSession,
    dao: WorkoutDao,
    exercises: List<Exercise>,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onEdit: () -> Unit
) {
    val sets by dao.getSetsForSessionFlow(session.id).collectAsState(initial = emptyList())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExpandToggle() },
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(session.location.ifBlank { "Okänd plats" }, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(session.date, color = TextGray, fontSize = 13.sp)
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Redigera", tint = Blue)
                }
                IconButton(onClick = onExpandToggle) {
                    Icon(
                        if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = TextGray
                    )
                }
            }

            if (isExpanded) {
                HorizontalDivider(color = DarkBg)
                Column(modifier = Modifier.padding(16.dp)) {
                    if (sets.isEmpty()) {
                        Text("Inga sets loggade", color = TextGray, fontSize = 14.sp)
                    } else {
                        val grouped = sets.groupBy { it.exerciseId }
                        grouped.forEach { (exerciseId, exSets) ->
                            val exerciseName = exercises.find { it.id == exerciseId }?.name ?: "Okänd övning"
                            Text(exerciseName, color = Blue, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.padding(bottom = 6.dp))
                            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                                Text("Set", color = TextGray, fontSize = 12.sp, modifier = Modifier.weight(0.5f))
                                Text("Vikt", color = TextGray, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                Text("Reps", color = TextGray, fontSize = 12.sp, modifier = Modifier.weight(1f))
                            }
                            exSets.forEachIndexed { index, set ->
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                                    Text("${index + 1}", color = TextGray, fontSize = 14.sp, modifier = Modifier.weight(0.5f))
                                    Text("${set.weight} kg", color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
                                    Text("${set.reps}", color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SessionEditor(dao: WorkoutDao, existingSession: WorkoutSession? = null, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val exercises by dao.getAllExercisesFlow().collectAsState(initial = emptyList())
    var location by remember { mutableStateOf(existingSession?.location ?: "") }
    var selectedExercises by remember { mutableStateOf(listOf<ExerciseWithSets>()) }
    var showPicker by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // Load existing sets if editing
    LaunchedEffect(existingSession, exercises) {
        if (existingSession != null && exercises.isNotEmpty() && selectedExercises.isEmpty()) {
            val existingSets = withContext(Dispatchers.IO) {
                dao.getSetsForSession(existingSession.id)
            }
            val grouped = existingSets.groupBy { it.exerciseId }
            val loaded = grouped.map { (exerciseId, sets) ->
                val exercise = exercises.find { it.id == exerciseId }
                if (exercise != null) {
                    ExerciseWithSets(exercise = exercise, sets = sets)
                } else null
            }.filterNotNull()
            selectedExercises = loaded
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = CardBg,
            title = { Text("Ta bort pass?", color = Color.White) },
            text = { Text("Är du säker på att du vill ta bort detta pass? Det går inte att ångra.", color = TextGray) },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch(Dispatchers.IO) {
                        if (existingSession != null) {
                            dao.deleteSetsForSession(existingSession.id)
                            dao.deleteSession(existingSession)
                        }
                        withContext(Dispatchers.Main) {
                            onBack()
                        }
                    }
                }) { Text("Ta bort", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Avbryt", color = TextGray)
                }
            }
        )
    }

    if (showPicker) {
        AlertDialog(
            onDismissRequest = { showPicker = false },
            containerColor = CardBg,
            title = { Text("Välj övning", color = Color.White) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    exercises.forEach { exercise ->
                        TextButton(onClick = {
                            selectedExercises = selectedExercises + ExerciseWithSets(
                                exercise = exercise,
                                sets = listOf(WorkoutSet(sessionId = existingSession?.id ?: 0, exerciseId = exercise.id, weight = 0.0, reps = 0))
                            )
                            showPicker = false
                        }) {
                            Text(exercise.name, color = Color.White, fontSize = 16.sp)
                        }
                        HorizontalDivider(color = DarkBg)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("Stäng", color = TextGray)
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(DarkBg).padding(20.dp)) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
            }
            Text(if (existingSession != null) "Redigera Pass" else "Logga Pass", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            if (existingSession != null) {
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Ta bort pass", tint = Color.Red.copy(alpha = 0.7f))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            placeholder = { Text("Plats (t.ex. Orminge)", color = TextGray) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                unfocusedBorderColor = CardBg, focusedContainerColor = CardBg, unfocusedContainerColor = CardBg
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            selectedExercises.forEachIndexed { exIndex, exWithSets ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(exWithSets.exercise.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            IconButton(onClick = {
                                selectedExercises = selectedExercises.toMutableList().also { it.removeAt(exIndex) }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red.copy(alpha = 0.7f))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("Set", color = TextGray, fontSize = 13.sp, modifier = Modifier.weight(0.5f))
                            Text("Vikt (kg)", color = TextGray, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Text("Reps", color = TextGray, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Spacer(modifier = Modifier.weight(0.4f))
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        exWithSets.sets.forEachIndexed { setIndex, set ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("${setIndex + 1}", color = TextGray, modifier = Modifier.weight(0.5f))
                                OutlinedTextField(
                                    value = if (set.weight == 0.0) "" else set.weight.toString(),
                                    onValueChange = { v ->
                                        val updated = selectedExercises.toMutableList()
                                        val newSets = updated[exIndex].sets.toMutableList()
                                        newSets[setIndex] = set.copy(weight = v.toDoubleOrNull() ?: 0.0)
                                        updated[exIndex] = updated[exIndex].copy(sets = newSets.toList())
                                        selectedExercises = updated
                                    },
                                    placeholder = { Text("0", color = TextGray) },
                                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                        focusedContainerColor = DarkBg, unfocusedContainerColor = DarkBg,
                                        unfocusedBorderColor = CardBg
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = if (set.reps == 0) "" else set.reps.toString(),
                                    onValueChange = { v ->
                                        val updated = selectedExercises.toMutableList()
                                        val newSets = updated[exIndex].sets.toMutableList()
                                        newSets[setIndex] = set.copy(reps = v.toIntOrNull() ?: 0)
                                        updated[exIndex] = updated[exIndex].copy(sets = newSets.toList())
                                        selectedExercises = updated
                                    },
                                    placeholder = { Text("0", color = TextGray) },
                                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                        focusedContainerColor = DarkBg, unfocusedContainerColor = DarkBg,
                                        unfocusedBorderColor = CardBg
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                                IconButton(onClick = {
                                    val updated = selectedExercises.toMutableList()
                                    val newSets = updated[exIndex].sets.toMutableList()
                                    newSets.removeAt(setIndex)
                                    updated[exIndex] = updated[exIndex].copy(sets = newSets.toList())
                                    selectedExercises = updated
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = null, tint = TextGray)
                                }
                            }
                        }

                        TextButton(onClick = {
                            val updated = selectedExercises.toMutableList()
                            val newSets = updated[exIndex].sets.toMutableList()
                            newSets.add(WorkoutSet(sessionId = existingSession?.id ?: 0, exerciseId = exWithSets.exercise.id, weight = 0.0, reps = 0))
                            updated[exIndex] = updated[exIndex].copy(sets = newSets.toList())
                            selectedExercises = updated
                        }) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Blue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Lägg till set", color = Blue, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { showPicker = true },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CardBg),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Blue)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Lägg till övning", color = Blue, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (saved) {
            Text("Pass sparat! ✅", color = Blue, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                scope.launch(Dispatchers.IO) {
                    val today = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
                    if (existingSession != null) {
                        // Update existing session
                        dao.deleteSetsForSession(existingSession.id)
                        val updatedSession = existingSession.copy(location = location)
                        dao.updateSession(updatedSession)
                        selectedExercises.forEach { exWithSets ->
                            exWithSets.sets.forEach { set ->
                                dao.insertSet(set.copy(id = 0, sessionId = existingSession.id))
                            }
                        }
                    } else {
                        // New session
                        val sessionId = dao.insertSession(WorkoutSession(date = today, location = location)).toInt()
                        selectedExercises.forEach { exWithSets ->
                            exWithSets.sets.forEach { set ->
                                dao.insertSet(set.copy(id = 0, sessionId = sessionId))
                            }
                        }
                    }
                    withContext(Dispatchers.Main) {
                        saved = true
                        delay(600)
                        onBack()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Blue),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(if (existingSession != null) "Uppdatera pass" else "Spara pass", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
