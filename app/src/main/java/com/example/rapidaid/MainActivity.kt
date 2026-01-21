package com.example.rapidaid

import android.Manifest
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.rapidaid.ui.theme.RapidAidTheme
import com.google.firebase.database.FirebaseDatabase
//import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority


// 🔥 Firebase helper functions
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

fun sendSosToFirebase(mobile: String) {
    val sosData = mapOf(
        "userMobile" to mobile,
        "message" to "SOS button pressed",
        "timestamp" to System.currentTimeMillis(),
        "status" to "ACTIVE"
    )

    FirebaseDatabase.getInstance()
        .getReference("sosAlerts")
        .push()
        .setValue(sosData)
}

class MainActivity : ComponentActivity() {

    private val locationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val granted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true

            if (!granted) {
                Toast.makeText(
                    this,
                    "Location permission required",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔐 Ask location permission
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

        // 🎨 UI
        setContent {
            RapidAidTheme {
                AppContent()
            }
        }
    }
}




@Composable
fun AppContent() {

    var screen by remember { mutableStateOf("login") }
    var userMobile by remember { mutableStateOf("") }

    when (screen) {

        "login" -> LoginScreen {
            screen = "create"
        }

        "create" -> CreateAccountScreen { mobile ->
            userMobile = mobile
            screen = "home"
        }

        "home" -> HomeScreen(
            onSosClick = {
                sendSosToFirebase(userMobile)
                screen = "alert"
            }
        )

        "alert" -> AlertSentScreen {
            screen = "home"
        }
    }
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
            Text("Continue",
            color=Color.White

            )
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
            .padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text("Create Account", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            textStyle = TextStyle(
                color = Color.Black
            ),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )


        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = mobile,
            onValueChange = { mobile = it },
            label = { Text("Mobile Number") },
            textStyle = TextStyle(
                color = Color.Black
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = emergency,
            onValueChange = { emergency = it },
            label = { Text("Emergency Contact Number") },
            textStyle = TextStyle(
                color = Color.Black
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (name.isBlank()) {
            Text("Please enter full name", color = Color.Red, fontSize = 12.sp)
        }

        if (mobile.isNotEmpty() && mobile.length != 10) {
            Text("Mobile number must be 10 digits", color = Color.Red, fontSize = 12.sp)
        }

        if (emergency.isNotEmpty() && emergency.length != 10) {
            Text("Emergency contact must be 10 digits", color = Color.Red, fontSize = 12.sp)
        }


        Button(
            onClick = {
                saveUserToFirebase(name, mobile, emergency)
                onContinue(mobile)

            },
            enabled = isFormValid,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        ) {
            Text("Register / Continue")
        }


        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Your safety is our priority",
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun HomeScreen(onSosClick: () -> Unit) {

    val context = LocalContext.current


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {

        // 🔹 HEADER
        Text(
            text = "RapidAid",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Press button to call emergency",
            fontSize = 22.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(100.dp))

        // 🔴 SOS BUTTON
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            StaticSOSCircle {
                onSosClick()
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // 📞 CALL + 💬 SMS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            Card(
                modifier = Modifier
                    .size(120.dp)
                    .clickable {
                        context.startActivity(Intent(Intent.ACTION_DIAL))
                    },
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.Call, contentDescription = "Call", tint = Color.Red)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Call", textAlign = TextAlign.Center)
                }
            }

            Card(
                modifier = Modifier
                    .size(120.dp)
                    .clickable {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("sms:"))
                        )
                    },
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.Email, contentDescription = "SMS", tint = Color.Red)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("SMS", textAlign = TextAlign.Center)
                }
            }
        }

        // 🚀 PUSH FOOTER TO BOTTOM
        Spacer(modifier = Modifier.weight(1f))

        // 🔻 FOOTER (ALWAYS LAST)
        BottomFooter(
            selected = "home",
            onSelect = {
                // future navigation
            }
        )
    }
}

@Composable
fun AlertSentScreen(onBack: () -> Unit) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(40.dp))

        // 🔴 MAIN ALERT CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "AI Emergency Alert",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD32F2F)
                )

                Spacer(modifier = Modifier.height(23.dp))

                // 🖼 ALERT IMAGE
                Image(
                    painter = painterResource(id = R.drawable.ai_alert),
                    contentDescription = "Emergency Alert",
                    modifier = Modifier
                        .height(220.dp)
                        .fillMaxWidth(),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(14.dp))

                Divider(color = Color.LightGray)

                Spacer(modifier = Modifier.height(10.dp))

//                Text(
//                    text = "Powered by Google AI • Nano Banana",
//                    fontSize = 12.sp,
//                    color = Color.Gray
//                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // 🚨 SUCCESS TEXT
        Text(
            text = "🚨 Alert Sent Successfully",
            fontSize = 23.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Red
        )

        Spacer(modifier = Modifier.height(44.dp))

        // 🔙 BACK BUTTON
        Button(
            onClick = onBack,
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .height(54.dp)
                .width(220.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6A4FB3)
            )
        ) {
            Text(
                text = "Back to Home",
                fontSize = 16.sp,
                color = Color.White
            )
        }
    }
}

@Composable
fun EmergencyCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEFE7FF) // 💜 light purple card
        ),

        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFF6A1B9A),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6A1B9A)
            )
        }
    }
}



@Composable
fun StaticSOSCircle(onClick: () -> Unit) {

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(260.dp)
    ) {

        Box(
            modifier = Modifier
                .size(260.dp)
                .background(Color(0xFFFFE5E5), CircleShape)
        )

        Box(
            modifier = Modifier
                .size(220.dp)
                .background(Color(0xFFFFCFCF), CircleShape)
        )

        Box(
            modifier = Modifier
                .size(160.dp)
                .background(Color(0xFFD32F2F), CircleShape)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "SOS",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

fun sendSosWithLocation(context: Context, mobile: String) {

    val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    if (
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
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

@Composable
fun BottomFooter(
    selected: String,
    onSelect: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {

            FooterItem("Home", Icons.Default.Home, selected == "home") {
                onSelect("home")
            }

            FooterItem("Features", Icons.Default.Star, selected == "features") {
                onSelect("features")
            }

            FooterItem("Your Info", Icons.Default.Person, selected == "profile") {
                onSelect("profile")
            }
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
            tint = if (selected) Color(0xFF6A1B9A) else Color.Gray
        )
        Text(
            text = title,
            fontSize = 12.sp,
            color = if (selected) Color(0xFF6A1B9A) else Color.Gray
        )
    }
}
