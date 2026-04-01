package com.example.setsync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
                    icon = { Icon(Icons.Filled.FitnessCenter, contentDescription = "Övningar", tint = if (selectedTab == 2) Blue else TextGray) },
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
                1 -> PlaceholderScreen("Pass")
                2 -> PlaceholderScreen("Övningar")
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
fun PlaceholderScreen(name: String) {
    Box(modifier = Modifier.fillMaxSize().background(DarkBg), contentAlignment = Alignment.Center) {
        Text(name, color = Color.White, fontSize = 24.sp)
    }
}