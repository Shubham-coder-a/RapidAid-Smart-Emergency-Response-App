package com.example.rapidaid

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.rapidaid.ui.theme.RapidAidTheme
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.database.FirebaseDatabase


fun saveUserToFirebase(name: String, mobile: String, emergency: String) {
    val userData = mapOf(
        "name" to name,
        "mobile" to mobile,
        "emergency" to emergency,
        "createdAt" to System.currentTimeMillis()
    )

    FirebaseDatabase.getInstance()
        .getReference("users")
        .child(mobile)
        .setValue(userData)
}

fun sendSosWithLocation(context: Context, mobile: String) {

    val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        return
    }

    fusedClient.getCurrentLocation(
        Priority.PRIORITY_HIGH_ACCURACY,
        null
    ).addOnSuccessListener { location ->

        if (location != null) {

            val lat = location.latitude
            val lon = location.longitude
            val mapsLink = "https://maps.google.com/?q=$lat,$lon"

            val sosData = mapOf(
                "userMobile" to mobile,
                "message" to "SOS button pressed",
                "status" to "ACTIVE",
                "timestamp" to System.currentTimeMillis(),
                "location" to mapOf(
                    "latitude" to lat,
                    "longitude" to lon,
                    "mapsLink" to mapsLink
                )
            )

            FirebaseDatabase.getInstance()
                .getReference("sosAlerts")
                .push()
                .setValue(sosData)
        }
    }
}


class MainActivity : ComponentActivity() {

    private val locationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] != true) {
                Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

        setContent {
            RapidAidTheme {
                AppContent()
            }
        }
    }
}


@Composable
fun AppContent() {

    val context = LocalContext.current

    var screen by remember { mutableStateOf("login") }
    var selectedTab by remember { mutableStateOf("home") }
    var userMobile by remember { mutableStateOf("") }

    when (screen) {

        "login" -> LoginScreen {
            screen = "create"
        }

        "create" -> CreateAccountScreen { mobile ->
            userMobile = mobile
            selectedTab = "home"
            screen = "home"
        }

        "home" -> HomeScreen(
            selectedTab = selectedTab,
            onTabSelect = {
                selectedTab = it
                screen = it
            },
            onSosClick = {
                sendSosWithLocation(context, userMobile)
                screen = "alert"
            }
        )

        "features" -> FeaturesScreen {
            selectedTab = "home"
            screen = "home"
        }

        "profile" -> ProfileScreen(userMobile) {
            selectedTab = "home"
            screen = "home"
        }

        "alert" -> AlertSentScreen {
            screen = "home"
        }
    }
}

@Composable
fun FeaturesScreen(content: @Composable () -> Unit) {
    TODO("Not yet implemented")
}

@Composable
fun LoginScreen(onNext: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Login / Sign Up", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = onNext) {
            Text("Continue", color = Color.White)
        }
    }
}

@Composable
fun CreateAccountScreen(onContinue: (String) -> Unit) {

    var name by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var emergency by remember { mutableStateOf("") }

    val isFormValid =
        name.isNotBlank() &&
                mobile.length == 10 &&
                emergency.length == 10

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Create Account",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 🔹 Full Name
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name",color=Color.Black) },

            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
//                focusedBorderColor = Color(0xFF7B1FA2),
//                unfocusedBorderColor = Color.LightGray,
                focusedLabelColor = Color(0xFF7B1FA2),
                cursorColor = Color(0xFF7B1FA2)
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 🔹 Mobile Number
        OutlinedTextField(
            value = mobile,
            onValueChange = {
                // 🔒 Sirf digits allow + max 10
                if (it.all { ch -> ch.isDigit() } && it.length <= 10) {
                    mobile = it
                }
            },
            label = { Text(text = "Mobile Number", color=Color.Black) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number // 🔥 number keyboard
            ),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedLabelColor = Color(0xFF7B1FA2),
                cursorColor = Color(0xFF7B1FA2)
            )
        )


        Spacer(modifier = Modifier.height(14.dp))

        // 🔹 Emergency Contact
        OutlinedTextField(
            value = emergency,
            onValueChange = {
                // 🔒 Sirf digits allow + max 10
                if (it.all { ch -> ch.isDigit() } && it.length <= 10) {
                    emergency = it
                }
            },
            label = { Text(text = "Emergency Contact Number", color=Color.Black) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number // 🔥 number keyboard
            ),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedLabelColor = Color(0xFF7B1FA2),
                cursorColor = Color(0xFF7B1FA2)
            )
        )


        Spacer(modifier = Modifier.height(30.dp))

        // 🔥 Gradient Button (Aliya style)
        Button(
            onClick = {
                saveUserToFirebase(name, mobile, emergency)
                onContinue(mobile)
            },
            enabled = isFormValid,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF7B1FA2),
                disabledContainerColor = Color(0xFFCE93D8)
            )
        ) {
            Text(
                text = "Register / Continue",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold

            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Your safety is our priority",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}


@Composable
fun HomeScreen(
    selectedTab: String,
    onTabSelect: (String) -> Unit,
    onSosClick: () -> Unit
)
 {


    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp)
    ) {

        Text("RapidAid", fontSize = 40.sp, fontWeight = FontWeight.Bold)
        Text("Press button to call emergency", fontSize = 20.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(80.dp))

        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            StaticSOSCircle { onSosClick() }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            EmergencyCard("Call", Icons.Default.Call) {
                context.startActivity(Intent(Intent.ACTION_DIAL))
            }

            EmergencyCard("SMS", Icons.Default.Email) {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("sms:")))
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        BottomFooter("home") {}
    }
}

@Composable
fun AlertSentScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🚨 Alert Sent Successfully", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Red)
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = onBack) {
            Text("Back to Home")
        }
    }
}

@Composable
fun StaticSOSCircle(onClick: () -> Unit) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(240.dp)) {
        Box(Modifier.size(240.dp).background(Color(0xFFFFE5E5), CircleShape))
        Box(
            Modifier.size(160.dp)
                .background(Color.Red, CircleShape)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Text("SOS", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun EmergencyCard(title: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier.size(120.dp).clickable { onClick() },
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = title, tint = Color.Red)
            Spacer(modifier = Modifier.height(8.dp))
            Text(title)
        }
    }
}

@Composable
fun BottomFooter(
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {

        FooterItem(
            title = "Home",
            icon = Icons.Default.Home,
            selected = selected == "home"
        ) {
            onSelect("home")
        }

        FooterItem(
            title = "Features",
            icon = Icons.Default.Star,
            selected = selected == "features"
        ) {
            onSelect("features")
        }

        FooterItem(
            title = "Profile",
            icon = Icons.Default.Person,
            selected = selected == "profile"
        ) {
            onSelect("profile")
        }
    }
}

@Composable
fun FooterItem(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (selected) Color.Red else Color.Gray
        )
        Text(
            text = title,
            fontSize = 12.sp,
            color = if (selected) Color.Red else Color.Gray
        )
    }
}

@Composable
fun ProfileScreen(mobile: String, onBack: () -> Unit) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Profile", fontSize = 26.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(20.dp))

        Text("Registered Mobile:")
        Text(
            text = mobile,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(onClick = onBack) {
            Text("Back to Home")
        }
    }
}


