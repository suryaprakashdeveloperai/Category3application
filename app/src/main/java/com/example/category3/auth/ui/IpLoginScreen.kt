package com.example.category3.auth.ui
import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun IpLoginScreen(onNavigateToHmi: (String) -> Unit) {
    var ipText by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val theme = getAdaptiveTheme(false) // Assuming this exists from your Dashboard code
    val ipPattern = Patterns.WEB_URL

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmWhite), // Match background
        contentAlignment = Alignment.Center
    ) {
        CleanPanel(
            theme = theme,
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth()
                .wrapContentSize(),
            cornerRadius = 24.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(AccentPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Memory, "HMI", tint = AccentPrimary, modifier = Modifier.size(32.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("V-Box HMI Portal", color = BrandDeepNavy, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text("Enter machine IP to establish connection", color = theme.textMuted, fontSize = 13.sp, modifier = Modifier.padding(bottom = 24.dp))

                OutlinedTextField(
                    value = ipText,
                    onValueChange = {
                        ipText = it; isError = false
                    },
                    label = { Text("IP Address (e.g. 192.168.1.50)") },
                    isError = isError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    textStyle = TextStyle(color = theme.textMain, fontWeight = FontWeight.SemiBold)
                )

                if (isError) {
                    Text("Please enter a valid IP or URL", color = AccentCritical, fontSize = 11.sp, modifier = Modifier.padding(bottom = 16.dp).align(Alignment.Start))
                } else {
                    Spacer(modifier = Modifier.height(24.dp))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.linearGradient(listOf(AccentPrimary, AccentPrimary.copy(alpha = 0.8f))))
                        .clickable {
                            var finalIp = ipText.trim()

                            if (finalIp.isEmpty()) {
                                isError = true
                                Toast.makeText(context, "Please enter an IP", Toast.LENGTH_SHORT).show()
                                return@clickable
                            }

                            // If the user didn't type a protocol (like http://, https://, or ws://),
                            // default to http://. Otherwise, keep exactly what they typed.
                            if (!finalIp.contains("://")) {
                                finalIp = "http://$finalIp"
                            }

                            // URL Encode to safely pass through Jetpack Navigation
                            val encodedUrl = java.net.URLEncoder.encode(
                                finalIp,
                                java.nio.charset.StandardCharsets.UTF_8.toString()
                            )

                            onNavigateToHmi(encodedUrl)
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("CONNECT", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }
        }
    }
}

