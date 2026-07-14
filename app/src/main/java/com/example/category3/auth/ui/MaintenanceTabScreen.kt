package com.example.category3.auth.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Camera
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Engineering
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material.icons.rounded.ListAlt
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.category3.components.RadialAppBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors

// --- 1. DATA MODELS ---
data class MaintenanceTicket(
    val id: String,
    val machineId: String,
    val issueDescription: String,
    val priority: String, // "CRITICAL", "HIGH", "MEDIUM"
    var status: String,   // "PREDICTIVE", "PENDING", "ACTIVE", "RESOLVED"
    val timestamp: String,
    val raisedBy: String,
    var assignedMechanic: String? = null,
    var assignedTime: String? = null,
    var resolvedTime: String? = null,
    var signature: List<List<Offset>>? = null, // In reality, you'd upload this as an image URL string
    var verificationPhoto: Bitmap? = null      // In reality, you'd upload this and store the URL string
)

// --- 2. API SPACE (Replace with Retrofit/Ktor) ---
object MaintenanceApiSpace {
    // 🔴 YOUR GET API CALL GOES HERE
    suspend fun fetchTicketsFromNetwork(): List<MaintenanceTicket> {
        delay(1500) // Simulating network loading time
        return listOf(
            MaintenanceTicket("ZY-9002", "Centrifuge A", "AI Predicts bearing failure within 48h based on vibration harmonics", "MEDIUM", "PREDICTIVE", "Predicted", "Zyren AI Engine"),
            MaintenanceTicket("TKT-8801", "Juice Pump 1", "Equipment Fault - Pump failed to start due to electrical trip", "CRITICAL", "PENDING", "10:03", "J. Mason (Mill Op)"),
            MaintenanceTicket("TKT-8799", "Evaporator 3", "Pressure valve stuck in open position, bypassing pressure lines", "HIGH", "ACTIVE", "08:15", "D. Smith", "M. Reynolds", "08:20")
        )
    }

    // 🔴 YOUR POST/PUT API CALL GOES HERE
    suspend fun assignTicketToMechanic(ticketId: String, mechanic: String): Boolean {
        delay(500) // Simulating API update
        return true // Return true if API update was successful
    }

    // 🔴 YOUR POST/MULTIPART API CALL GOES HERE (To upload photo/signature)
    suspend fun verifyAndResolveTicket(ticketId: String, signature: List<List<Offset>>, photo: Bitmap?): Boolean {
        delay(1000) // Simulating Image Upload and DB update
        return true
    }
}

// --- 3. VIEWMODEL & STATE MANAGEMENT ---
sealed interface TicketUiState {
    object Loading : TicketUiState
    data class Success(val tickets: List<MaintenanceTicket>) : TicketUiState
    data class Error(val message: String) : TicketUiState
}

class MaintenanceViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<TicketUiState>(TicketUiState.Loading)
    val uiState: StateFlow<TicketUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = TicketUiState.Loading
            try {
                // Call API
                val data = MaintenanceApiSpace.fetchTicketsFromNetwork()
                _uiState.value = TicketUiState.Success(data)
            } catch (e: Exception) {
                _uiState.value = TicketUiState.Error(e.localizedMessage ?: "Failed to load data from server")
            }
        }
    }

    fun assignTicket(ticketId: String, mechanic: String) {
        val currentState = _uiState.value
        if (currentState is TicketUiState.Success) {
            viewModelScope.launch {
                val success = MaintenanceApiSpace.assignTicketToMechanic(ticketId, mechanic)
                if (success) {
                    // Update local state so UI reacts instantly after API confirms
                    val updatedList = currentState.tickets.map {
                        if (it.id == ticketId) it.copy(status = "ACTIVE", assignedMechanic = mechanic, assignedTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")))
                        else it
                    }
                    _uiState.value = TicketUiState.Success(updatedList)
                }
            }
        }
    }

    fun resolveTicket(ticketId: String, signature: List<List<Offset>>, photo: Bitmap?) {
        val currentState = _uiState.value
        if (currentState is TicketUiState.Success) {
            viewModelScope.launch {
                val success = MaintenanceApiSpace.verifyAndResolveTicket(ticketId, signature, photo)
                if (success) {
                    // Update local state
                    val updatedList = currentState.tickets.map {
                        if (it.id == ticketId) it.copy(status = "RESOLVED", resolvedTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")), signature = signature, verificationPhoto = photo)
                        else it
                    }
                    _uiState.value = TicketUiState.Success(updatedList)
                }
            }
        }
    }
}

// --- Greyfrost Theme Palette ---
object GreyFrostColors {
    val TextPrimary = Color(0xFF1E293B)
    val TextSecondary = Color(0xFF64748B)
    val GlassPane = Color(0xFFF1F5F9).copy(alpha = 0.5f)
    val GlassCard = Color(0xFFFFFFFF).copy(alpha = 0.85f)
    val GlassBorder = Color(0xFFFFFFFF).copy(alpha = 0.9f)
    val Shadow = Color(0xFF0F172A).copy(alpha = 0.08f)
    val AI = Color(0xFF8B5CF6)
    val Critical = Color(0xFFEF4444)
    val High = Color(0xFFF59E0B)
    val Active = Color(0xFF3B82F6)
    val Resolved = Color(0xFF10B981)
}

fun Modifier.greyFrostCard(isColumn: Boolean = false) = this
    .shadow(if (isColumn) 0.dp else 8.dp, RoundedCornerShape(16.dp), spotColor = GreyFrostColors.Shadow)
    .background(if (isColumn) GreyFrostColors.GlassPane else GreyFrostColors.GlassCard, RoundedCornerShape(16.dp))
    .border(1.dp, GreyFrostColors.GlassBorder, RoundedCornerShape(16.dp))
    .clip(RoundedCornerShape(16.dp))

// --- 4. MAIN ENTRY SCREEN ---
@Composable
fun MaintenanceTabScreen(
    onNavigateToScreen: (String) -> Unit, // Navigation callback
    viewModel: MaintenanceViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFE2E8F0))) {
        // 1. Background layer
        GreyFrostBackground()

        // 2. Main Content layer
        when (val state = uiState) {
            is TicketUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = GreyFrostColors.Active)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Fetching Live Data from Server...", color = GreyFrostColors.TextSecondary, fontWeight = FontWeight.Bold)
                    }
                }
            }
            is TicketUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.Warning, null, tint = GreyFrostColors.Critical, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Connection Error", color = GreyFrostColors.TextPrimary, fontWeight = FontWeight.Black, fontSize = 20.sp)
                        Text(state.message, color = GreyFrostColors.TextSecondary, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadData() }, colors = ButtonDefaults.buttonColors(containerColor = GreyFrostColors.TextPrimary)) {
                            Text("RETRY")
                        }
                    }
                }
            }
            is TicketUiState.Success -> {
                // Pass the data and API actions down to the UI
                MaintenanceDashboardContent(
                    tickets = state.tickets,
                    onAssign = { id, mechanic -> viewModel.assignTicket(id, mechanic) },
                    onResolve = { id, sig, photo -> viewModel.resolveTicket(id, sig, photo) }
                )
            }
        }

        // 3. Radial App Bar floating gracefully on the left edge
        RadialAppBar(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 8.dp)
                .zIndex(30f),
            activeSection = "maintenance_tab", // Highlights the maintenance icon/section
            onActionSelected = onNavigateToScreen
        )
    }
}

// --- 5. ACTUAL UI DASHBOARD ---
@Composable
fun MaintenanceDashboardContent(
    tickets: List<MaintenanceTicket>,
    onAssign: (String, String) -> Unit,
    onResolve: (String, List<List<Offset>>, Bitmap?) -> Unit
) {
    // 1. CHANGED: Now uses mutableStateListOf so we can add new technicians to it
    val availableMechanics = remember {
        mutableStateListOf("M. Reynolds (Mech)", "P. Taylor (Elec)", "J. Kowalski (Mech)", "S. Connor (Plumbing)")
    }

    // UI State for Modals & Filters
    var ticketToSign by remember { mutableStateOf<String?>(null) }
    var ticketToAssign by remember { mutableStateOf<MaintenanceTicket?>(null) }
    var selectedPriority by remember { mutableStateOf("ALL") }

    val displayTickets = if (selectedPriority == "ALL") {
        tickets
    } else {
        tickets.filter { it.priority == selectedPriority }
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        HeaderSection(tickets)
        Spacer(modifier = Modifier.height(20.dp))

        KpiDashboard(displayTickets)
        Spacer(modifier = Modifier.height(16.dp))

        PriorityFilterRow(
            selectedPriority = selectedPriority,
            onPrioritySelected = { selectedPriority = it }
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            FrostedColumn("ZYREN PREDICTIVE", GreyFrostColors.AI, displayTickets.filter { it.status == "PREDICTIVE" }, Modifier.weight(1f), onAssign = { ticketToAssign = it }, onSign = {})
            FrostedColumn("UNASSIGNED TRIAGE", GreyFrostColors.Critical, displayTickets.filter { it.status == "PENDING" }, Modifier.weight(1f), onAssign = { ticketToAssign = it }, onSign = {})
            FrostedColumn("DISPATCHED (ACTIVE)", GreyFrostColors.Active, displayTickets.filter { it.status == "ACTIVE" }, Modifier.weight(1f), onAssign = {}, onSign = { ticketToSign = it })
            FrostedColumn("RESOLVED (24H)", GreyFrostColors.Resolved, displayTickets.filter { it.status == "RESOLVED" }, Modifier.weight(1f), onAssign = {}, onSign = {})
        }
    }

    // 2. CHANGED: Fixed the error by properly passing all parameters, including onAddMechanic
    ticketToAssign?.let { ticket ->
        DispatchModal(
            mechanics = availableMechanics,
            onDismiss = { ticketToAssign = null },
            onAssign = { mechanic ->
                onAssign(ticket.id, mechanic) // Calls API via ViewModel
                ticketToAssign = null
            },
            onAddMechanic = { newMechanic ->
                availableMechanics.add(newMechanic) // Adds the new technician to the list
            }
        )
    }

    ticketToSign?.let { id ->
        VerificationModal(ticketId = id, onDismiss = { ticketToSign = null }) { signature, photo ->
            onResolve(id, signature, photo) // Calls API via ViewModel
            ticketToSign = null
        }
    }
}

// --- REMAINING UI COMPONENTS ---

@Composable
fun PriorityFilterRow(selectedPriority: String, onPrioritySelected: (String) -> Unit) {
    val priorities = listOf("ALL", "CRITICAL", "HIGH", "MEDIUM")

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.FilterList, contentDescription = "Filter", tint = GreyFrostColors.TextSecondary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Filter Importance:", fontSize = 13.sp, color = GreyFrostColors.TextSecondary, fontWeight = FontWeight.Bold)
        }

        priorities.forEach { priority ->
            val isSelected = selectedPriority == priority
            val baseColor = when(priority) { "CRITICAL" -> GreyFrostColors.Critical; "HIGH" -> GreyFrostColors.High; "MEDIUM" -> GreyFrostColors.AI; else -> GreyFrostColors.TextPrimary }
            val containerColor = if (isSelected) baseColor else Color.Transparent
            val contentColor = if (isSelected) Color.White else baseColor

            Surface(
                modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { onPrioritySelected(priority) }, shape = RoundedCornerShape(12.dp), color = containerColor, border = BorderStroke(1.dp, baseColor.copy(alpha = if (isSelected) 1f else 0.5f))
            ) {
                Text(text = priority, color = contentColor, fontSize = 11.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp))
            }
        }
    }
}

@Composable
fun KpiDashboard(tickets: List<MaintenanceTicket>) {
    val totalWorks = tickets.size
    val pendingWorks = tickets.count { it.status == "PENDING" }
    val activeWorks = tickets.count { it.status == "ACTIVE" }
    val resolvedWorks = tickets.count { it.status == "RESOLVED" }
    val predictiveWorks = tickets.count { it.status == "PREDICTIVE" }
    val completionRate = if (totalWorks > 0) (resolvedWorks.toFloat() / totalWorks * 100).toInt() else 0

    val scrollState = rememberScrollState()

    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(scrollState), horizontalArrangement = Arrangement.spacedBy(12.dp)) {

        KpiCard(
            title = "Total Works",
            value = totalWorks.toString(),
            trendText = "Live Server Data",
            isTrendUp = true,
            icon = Icons.Rounded.ListAlt,
            modifier = Modifier.width(150.dp)
        )

        KpiCard(
            title = "Pending",
            value = pendingWorks.toString(),
            trendText = "Needs Attention",
            isTrendUp = false,
            isPositiveChange = pendingWorks == 0,
            icon = Icons.Rounded.HourglassEmpty,
            modifier = Modifier.width(150.dp)
        )

        KpiCard(
            title = "Completed",
            value = "$completionRate%",
            trendText = "Current Shift",
            isTrendUp = true,
            icon = Icons.Rounded.CheckCircle,
            modifier = Modifier.width(150.dp)
        )

        KpiCard(
            title = "Active Techs",
            value = activeWorks.toString(),
            trendText = "In Field",
            isTrendUp = true,
            icon = Icons.Rounded.Engineering,
            modifier = Modifier.width(150.dp)
        )

        KpiCard(
            title = "AI Predictive",
            value = predictiveWorks.toString(),
            trendText = "Pre-Failure",
            isTrendUp = true,
            isPositiveChange = false,
            icon = Icons.Rounded.NotificationsActive,
            modifier = Modifier.width(150.dp)
        )
    }
}

@Composable
fun KpiCard(title: String, value: String, trendText: String, isTrendUp: Boolean, isPositiveChange: Boolean = isTrendUp, icon: ImageVector, modifier: Modifier = Modifier) {
    val trendColor = if (isPositiveChange) GreyFrostColors.Resolved else GreyFrostColors.Critical
    val trendIcon = if (isTrendUp) Icons.Rounded.ArrowUpward else Icons.Rounded.ArrowDownward

    Column(modifier = modifier.greyFrostCard(isColumn = false).padding(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title, fontSize = 11.sp, color = GreyFrostColors.TextSecondary, fontWeight = FontWeight.Bold)
            Icon(Icons.Rounded.MoreHoriz, contentDescription = "More", tint = GreyFrostColors.TextSecondary, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Text(value, fontSize = 22.sp, color = GreyFrostColors.TextPrimary, fontWeight = FontWeight.Black, fontFamily = FontFamily.SansSerif)
            Icon(icon, contentDescription = null, tint = GreyFrostColors.TextSecondary.copy(alpha = 0.2f), modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(trendIcon, contentDescription = null, tint = trendColor, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(trendText, fontSize = 10.sp, color = GreyFrostColors.TextPrimary, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun GreyFrostBackground() {
    Canvas(modifier = Modifier.fillMaxSize().blur(140.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)) {
        drawCircle(color = Color(0xFFCBD5E1).copy(alpha = 0.8f), radius = size.width / 3, center = Offset(size.width * 0.2f, size.height * 0.2f))
        drawCircle(color = Color(0xFFF1F5F9).copy(alpha = 0.9f), radius = size.width / 2.5f, center = Offset(size.width * 0.8f, size.height * 0.8f))
        drawCircle(color = Color(0xFF94A3B8).copy(alpha = 0.4f), radius = size.width / 4, center = Offset(size.width * 0.5f, size.height * 0.5f))
    }
}

@Composable
fun HeaderSection(tickets: List<MaintenanceTicket>) {
    val criticalCount = tickets.count { it.priority == "CRITICAL" && it.status == "PENDING" }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text("Work Order Dispatch", fontSize = 28.sp, color = GreyFrostColors.TextPrimary, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Engineering, null, tint = GreyFrostColors.TextSecondary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("SUPERVISOR: Chief Eng. H. Ford", color = GreyFrostColors.TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
        if (criticalCount > 0) {
            Surface(color = GreyFrostColors.Critical.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, GreyFrostColors.Critical)) {
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Warning, null, tint = GreyFrostColors.Critical, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("$criticalCount CRITICAL PENDING", color = GreyFrostColors.Critical, fontWeight = FontWeight.Black, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun FrostedColumn(title: String, accentColor: Color, tickets: List<MaintenanceTicket>, modifier: Modifier = Modifier, onAssign: (MaintenanceTicket) -> Unit, onSign: (String) -> Unit) {
    Column(modifier = modifier.fillMaxHeight().greyFrostCard(isColumn = true).padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(12.dp).background(accentColor, CircleShape))
                Spacer(modifier = Modifier.width(10.dp))
                Text(title, fontSize = 13.sp, color = GreyFrostColors.TextPrimary, fontWeight = FontWeight.Black)
            }
            Surface(color = GreyFrostColors.GlassCard, shape = CircleShape) {
                Text(tickets.size.toString(), fontSize = 12.sp, color = GreyFrostColors.TextPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxSize()) {
            items(tickets, key = { it.id }) { ticket -> FrostedTicketCard(ticket, onAssign = { onAssign(ticket) }, onSign = { onSign(ticket.id) }) }
        }
    }
}

@Composable
fun FrostedTicketCard(ticket: MaintenanceTicket, onAssign: () -> Unit, onSign: () -> Unit) {
    val priorityColor = when (ticket.priority) { "CRITICAL" -> GreyFrostColors.Critical; "HIGH" -> GreyFrostColors.High; "MEDIUM" -> GreyFrostColors.AI; else -> GreyFrostColors.TextSecondary }

    Box(modifier = Modifier.fillMaxWidth().greyFrostCard(isColumn = false)) {
        Box(Modifier.fillMaxHeight().width(6.dp).background(if(ticket.status == "PREDICTIVE") GreyFrostColors.AI else priorityColor).align(Alignment.CenterStart))
        Column(Modifier.padding(start = 22.dp, top = 16.dp, bottom = 16.dp, end = 16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(ticket.id, fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = GreyFrostColors.TextSecondary, fontWeight = FontWeight.Bold)
                Surface(color = priorityColor.copy(0.15f), shape = RoundedCornerShape(6.dp)) {
                    Text(ticket.priority, color = priorityColor, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(ticket.machineId, fontSize = 17.sp, color = GreyFrostColors.TextPrimary, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(4.dp))
            Text(ticket.issueDescription, fontSize = 14.sp, color = GreyFrostColors.TextSecondary, maxLines = 3, overflow = TextOverflow.Ellipsis, lineHeight = 20.sp)
            Spacer(Modifier.height(16.dp))
            Column(Modifier.fillMaxWidth().background(GreyFrostColors.Shadow, RoundedCornerShape(8.dp)).padding(12.dp)) {
                DataText("Logged:", ticket.raisedBy, ticket.timestamp)
                if (ticket.assignedMechanic != null) { Spacer(Modifier.height(6.dp)); DataText("Assigned:", ticket.assignedMechanic!!, ticket.assignedTime ?: "") }
                if (ticket.status == "RESOLVED") { Spacer(Modifier.height(6.dp)); DataText("Verified:", "Signed & Photo Attached", ticket.resolvedTime ?: "", icon = Icons.Rounded.VerifiedUser) }
            }
            if (ticket.status == "PENDING" || ticket.status == "PREDICTIVE") {
                Spacer(Modifier.height(16.dp))
                Button(onClick = onAssign, modifier = Modifier.fillMaxWidth().height(44.dp), colors = ButtonDefaults.buttonColors(containerColor = GreyFrostColors.TextPrimary), shape = RoundedCornerShape(8.dp)) {
                    Icon(Icons.Rounded.Engineering, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(8.dp)); Text("DISPATCH TECHNICIAN", fontSize = 12.sp, fontWeight = FontWeight.Black)
                }
            } else if (ticket.status == "ACTIVE") {
                Spacer(Modifier.height(16.dp))
                Button(onClick = onSign, modifier = Modifier.fillMaxWidth().height(44.dp), colors = ButtonDefaults.buttonColors(containerColor = GreyFrostColors.Resolved), shape = RoundedCornerShape(8.dp)) {
                    Icon(Icons.Rounded.CameraAlt, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(8.dp)); Text("FINAL VERIFICATION", fontSize = 12.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun DataText(label: String, value: String, time: String, icon: ImageVector? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        icon?.let { Icon(it, null, tint = GreyFrostColors.Resolved, modifier = Modifier.size(14.dp).padding(end = 4.dp)) }
        Text(label, fontSize = 12.sp, color = GreyFrostColors.TextSecondary, modifier = Modifier.width(if (icon == null) 68.dp else 64.dp), fontWeight = FontWeight.Medium)
        Text(value, fontSize = 12.sp, color = GreyFrostColors.TextPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(time, fontSize = 12.sp, color = GreyFrostColors.TextSecondary, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun DispatchModal(
    mechanics: List<String>,
    onDismiss: () -> Unit,
    onAssign: (String) -> Unit,
    onAddMechanic: (String) -> Unit // 🔴 NEW: Callback for adding a tech
) {
    // State for the new technician input field
    var newMechanicName by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f) // Prevents it from going off-screen if the list is too long
                .greyFrostCard(isColumn = false)
                .background(Color.White.copy(0.95f))
                .padding(24.dp)
        ) {
            Text("Select Technician", fontSize = 20.sp, color = GreyFrostColors.TextPrimary, fontWeight = FontWeight.Black)
            Text("Assign personnel to this work order.", fontSize = 14.sp, color = GreyFrostColors.TextSecondary)

            Spacer(Modifier.height(24.dp))

            // 🔴 CHANGED: Added verticalScroll so we can scroll through the list
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                mechanics.forEach { mechanic ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, GreyFrostColors.TextSecondary.copy(0.2f), RoundedCornerShape(8.dp))
                            .clickable { onAssign(mechanic) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(shape = CircleShape, color = GreyFrostColors.AI.copy(0.1f)) {
                            Icon(Icons.Rounded.Engineering, null, tint = GreyFrostColors.AI, modifier = Modifier.padding(8.dp).size(20.dp))
                        }
                        Spacer(Modifier.width(16.dp))
                        Text(mechanic, fontSize = 15.sp, color = GreyFrostColors.TextPrimary, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // 🔴 NEW: Add Technician Section
                Text("Add New Technician", fontSize = 13.sp, color = GreyFrostColors.TextPrimary, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newMechanicName,
                        onValueChange = { newMechanicName = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("e.g. J. Doe (Mech)", fontSize = 12.sp, color = GreyFrostColors.TextSecondary) },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreyFrostColors.Active,
                            unfocusedBorderColor = GreyFrostColors.TextSecondary.copy(alpha = 0.3f),
                        )
                    )

                    Button(
                        onClick = {
                            if (newMechanicName.isNotBlank()) {
                                onAddMechanic(newMechanicName.trim())
                                newMechanicName = "" // clear input after adding
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GreyFrostColors.Active),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(50.dp)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = "Add Tech", tint = Color.White)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                Text("CANCEL", color = GreyFrostColors.TextSecondary, fontWeight = FontWeight.Black)
            }
        }
    }
}

enum class CameraState { IDLE, PREVIEW, CAPTURED }

@Composable
fun VerificationModal(ticketId: String, onDismiss: () -> Unit, onSubmit: (List<List<Offset>>, Bitmap?) -> Unit) {
    val context = LocalContext.current
    var lines by remember { mutableStateOf(emptyList<List<Offset>>()) }
    var currentLine by remember { mutableStateOf(emptyList<Offset>()) }
    var cameraState by remember { mutableStateOf(CameraState.IDLE) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted -> if (isGranted) cameraState = CameraState.PREVIEW }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(Modifier.fillMaxWidth(0.7f).greyFrostCard(isColumn = false).background(Color.White.copy(0.95f)).padding(32.dp)) {
            Text("Final Verification & Sign-Off", fontSize = 24.sp, color = GreyFrostColors.TextPrimary, fontWeight = FontWeight.Black)
            Text("Ticket: $ticketId", color = GreyFrostColors.TextSecondary, fontFamily = FontFamily.Monospace)
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Column(Modifier.weight(0.8f)) {
                    Text("1. Work Verification Photo", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GreyFrostColors.TextPrimary); Spacer(Modifier.height(8.dp))
                    val borderColor = if (cameraState == CameraState.CAPTURED) GreyFrostColors.Resolved else GreyFrostColors.TextSecondary.copy(0.2f)
                    Box(Modifier.fillMaxWidth().height(180.dp).background(GreyFrostColors.Shadow, RoundedCornerShape(12.dp)).border(2.dp, borderColor, RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                        when (cameraState) {
                            CameraState.IDLE -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize().clickable { if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) { cameraState = CameraState.PREVIEW } else { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) } }, verticalArrangement = Arrangement.Center) {
                                    Icon(Icons.Rounded.AddAPhoto, null, tint = GreyFrostColors.TextSecondary, modifier = Modifier.size(48.dp)); Spacer(Modifier.height(8.dp)); Text("Tap to open Camera", color = GreyFrostColors.TextSecondary, fontWeight = FontWeight.Medium)
                                }
                            }
                            CameraState.PREVIEW -> { InlineCameraPreview(onImageCaptured = { bmp -> capturedBitmap = bmp; cameraState = CameraState.CAPTURED }) }
                            CameraState.CAPTURED -> {
                                capturedBitmap?.let { bmp ->
                                    Image(bitmap = bmp.asImageBitmap(), contentDescription = "Captured", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.3f)).clickable { cameraState = CameraState.PREVIEW }, contentAlignment = Alignment.Center) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(Color.Black.copy(0.6f), RoundedCornerShape(16.dp)).padding(horizontal = 12.dp, vertical = 6.dp)) {
                                            Icon(Icons.Rounded.Refresh, null, tint = Color.White, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Retake", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Column(Modifier.weight(1.2f)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("2. Technician Signature", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GreyFrostColors.TextPrimary)
                        if (lines.isNotEmpty()) { Text("CLEAR", fontSize = 12.sp, color = GreyFrostColors.Critical, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { lines = emptyList() }) }
                    }
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.fillMaxWidth().height(180.dp).background(GreyFrostColors.Shadow, RoundedCornerShape(12.dp)).border(1.dp, GreyFrostColors.TextSecondary.copy(0.2f), RoundedCornerShape(12.dp)).pointerInput(Unit) { detectDragGestures(onDragStart = { currentLine = listOf(it) }, onDrag = { change, _ -> currentLine = currentLine + change.position }, onDragEnd = { lines = lines + listOf(currentLine); currentLine = emptyList() }) }) {
                        if (lines.isEmpty() && currentLine.isEmpty()) { Text("Sign here...", color = GreyFrostColors.TextSecondary.copy(0.5f), modifier = Modifier.align(Alignment.Center)) }
                        Canvas(Modifier.fillMaxSize()) { (lines + listOf(currentLine)).forEach { line -> if (line.size > 1) { drawPath(Path().apply { moveTo(line[0].x, line[0].y); line.drop(1).forEach { lineTo(it.x, it.y) } }, color = GreyFrostColors.TextPrimary, style = Stroke(6f, cap = StrokeCap.Round, join = StrokeJoin.Round)) } } }
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
            val isFormComplete = lines.isNotEmpty() && capturedBitmap != null
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onDismiss) { Text("CANCEL", color = GreyFrostColors.TextSecondary, fontWeight = FontWeight.Black) }
                Spacer(Modifier.width(16.dp))
                Button(onClick = { onSubmit(lines, capturedBitmap) }, enabled = isFormComplete, colors = ButtonDefaults.buttonColors(containerColor = GreyFrostColors.Resolved), shape = RoundedCornerShape(8.dp), modifier = Modifier.height(48.dp)) {
                    Icon(if (isFormComplete) Icons.Rounded.Verified else Icons.Rounded.Lock, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("VERIFY & CLOSE WORK ORDER", fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun InlineCameraPreview(onImageCaptured: (Bitmap) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val executor = remember { Executors.newSingleThreadExecutor() }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = { ctx ->
            val previewView = PreviewView(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                try { cameraProvider.unbindAll(); cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture) } catch (exc: Exception) { Log.e("CameraX", "Use case binding failed", exc) }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        }, modifier = Modifier.fillMaxSize())

        IconButton(onClick = {
            imageCapture.takePicture(executor, object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) { val bitmap = image.toBitmap(); image.close(); ContextCompat.getMainExecutor(context).execute { onImageCaptured(bitmap) } }
                override fun onError(exception: ImageCaptureException) { Log.e("CameraX", "Photo capture failed: ${exception.message}", exception) }
            })
        }, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp).size(48.dp).background(Color.White.copy(0.5f), CircleShape)) {
            Icon(Icons.Rounded.Camera, contentDescription = "Capture", tint = Color.Black, modifier = Modifier.size(24.dp))
        }
    }
}