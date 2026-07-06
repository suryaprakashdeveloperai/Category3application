package com.auralis.industrialmesfrontendprototype.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AssignmentTurnedIn
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Create
import androidx.compose.material.icons.rounded.Engineering
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.PrecisionManufacturing
import androidx.compose.material.icons.rounded.SettingsSuggest
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// --- Premium Color Palette ---
object PremiumColors {
    val Background = Color(0xFFF1F5F9)
    val CardSurface = Color(0xFFFFFFFF)
    val PrimaryText = Color(0xFF0F172A)
    val SecondaryText = Color(0xFF64748B)
    val Border = Color(0xFFE2E8F0)
    val StatusGood = Color(0xFF10B981)
    val StatusAI = Color(0xFF8B5CF6) // Purple for Zyren AI
}

// --- Data Model ---
data class MaintenanceTicket(
    val id: String,
    val machineId: String,
    val issueDescription: String,
    val priority: String,
    var status: String, // PREDICTIVE, PENDING, ACTIVE, RESOLVED
    val timestamp: String,
    val raisedBy: String,
    var assignedMechanic: String? = null,
    var assignedTime: String? = null,
    var resolvedTime: String? = null,
    var signature: List<List<Offset>>? = null
)

@Composable
fun MaintenanceTabScreen() {
    val currentHeadName = "Chief Eng. H. Ford"
    val availableMechanics = remember { mutableStateListOf("M. Reynolds", "P. Taylor", "J. Kowalski", "S. Connor") }
    var showAddMechanicDialog by remember { mutableStateOf(false) }
    var newMechanicName by remember { mutableStateOf("") }
    var ticketToSign by remember { mutableStateOf<String?>(null) }

    val tickets = remember {
        mutableStateListOf(
            MaintenanceTicket(
                id = "ZY-9002", machineId = "Centrifuge A", issueDescription = "AI Predicts bearing failure within 48h based on vibration harmonics",
                priority = "MEDIUM", status = "PREDICTIVE", timestamp = "Predicted", raisedBy = "Zyren AI Engine"
            ),
            MaintenanceTicket(
                id = "TKT-8801", machineId = "Juice Pump 1", issueDescription = "Equipment Fault - Pump failed to start due to electrical trip",
                priority = "CRITICAL", status = "PENDING", timestamp = "10:03", raisedBy = "J. Mason (Mill Op)"
            ),
            MaintenanceTicket(
                id = "TKT-8799", machineId = "Evaporator 3", issueDescription = "Pressure valve stuck in open position, bypassing pressure lines",
                priority = "HIGH", status = "ACTIVE", timestamp = "08:15", raisedBy = "D. Smith (Boiling House)",
                assignedMechanic = "M. Reynolds", assignedTime = "08:20"
            )
        )
    }

    val predictiveTickets = tickets.filter { it.status == "PREDICTIVE" }
    val pendingTickets = tickets.filter { it.status == "PENDING" }
    val activeTickets = tickets.filter { it.status == "ACTIVE" }
    val resolvedTickets = tickets.filter { it.status == "RESOLVED" }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(PremiumColors.Background).padding(24.dp).padding(start = 70.dp)) {

            // --- Header (Matches Image) ---
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Maintenance Dispatch", fontSize = 32.sp, color = PremiumColors.PrimaryText, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.SettingsSuggest, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("SUPERVISOR VIEW: $currentHeadName", color = Color(0xFFF59E0B), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                Surface(color = Color(0xFFDC2626).copy(alpha = 0.1f), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Color(0xFFDC2626).copy(alpha = 0.3f))) {
                    Row(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).background(Color(0xFFDC2626), CircleShape))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("${pendingTickets.count { it.priority == "CRITICAL" }} CRITICAL PENDING", color = Color(0xFFDC2626), fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- Kanban Board Layout (4 Columns) ---
            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {

                // NEW: Zyren AI Column
                KanbanColumn(
                    title = "ZYREN AI PREDICTIVE", icon = Icons.Rounded.AutoAwesome, tickets = predictiveTickets, accentColor = PremiumColors.StatusAI, mechanicsList = availableMechanics, modifier = Modifier.weight(1f),
                    onAssignMechanic = { id, name -> moveTicket(tickets, id, name) },
                    onAddNewMechanic = { showAddMechanicDialog = true }, onActionClick = {}
                )

                KanbanColumn(
                    title = "UNASSIGNED TRIAGE", icon = Icons.Rounded.Inbox, tickets = pendingTickets, accentColor = Color(0xFFDC2626), mechanicsList = availableMechanics, modifier = Modifier.weight(1f),
                    onAssignMechanic = { id, name -> moveTicket(tickets, id, name) },
                    onAddNewMechanic = { showAddMechanicDialog = true }, onActionClick = {}
                )

                KanbanColumn(
                    title = "DISPATCHED (IN PROGRESS)", icon = Icons.Rounded.Engineering, tickets = activeTickets, accentColor = Color(0xFF3B82F6), mechanicsList = emptyList(), modifier = Modifier.weight(1f),
                    onAssignMechanic = { _, _ -> }, onAddNewMechanic = {},
                    onActionClick = { ticketId -> ticketToSign = ticketId }
                )

                KanbanColumn(
                    title = "RESOLVED (24H)", icon = Icons.Rounded.CheckCircle, tickets = resolvedTickets, accentColor = PremiumColors.StatusGood, mechanicsList = emptyList(), modifier = Modifier.weight(1f),
                    onAssignMechanic = { _, _ -> }, onAddNewMechanic = {}, onActionClick = {}
                )
            }
        }

        // --- Dialogs ---
        if (showAddMechanicDialog) { /* ... Add Mechanic Dialog Code ... */ }

        if (ticketToSign != null) {
            SignatureDialog(
                ticketId = ticketToSign!!,
                onDismiss = { ticketToSign = null },
                onSubmit = { signaturePath ->
                    val index = tickets.indexOfFirst { it.id == ticketToSign }
                    if (index != -1) {
                        tickets[index] = tickets[index].copy(
                            status = "RESOLVED",
                            resolvedTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),
                            signature = signaturePath
                        )
                    }
                    ticketToSign = null
                }
            )
        }
    }
}

private fun moveTicket(list: MutableList<MaintenanceTicket>, id: String, mechanic: String) {
    val index = list.indexOfFirst { it.id == id }
    if (index != -1) {
        list[index] = list[index].copy(
            status = "ACTIVE",
            assignedMechanic = mechanic,
            assignedTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        )
    }
}

@Composable
private fun KanbanColumn(
    title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, tickets: List<MaintenanceTicket>, accentColor: Color, mechanicsList: List<String>, modifier: Modifier = Modifier,
    onAssignMechanic: (String, String) -> Unit, onAddNewMechanic: () -> Unit, onActionClick: (String) -> Unit
) {
    Column(modifier = modifier.fillMaxHeight().background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp)).border(1.dp, PremiumColors.Border, RoundedCornerShape(12.dp)).padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = accentColor, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, fontSize = 14.sp, color = PremiumColors.PrimaryText, fontWeight = FontWeight.ExtraBold)
            }
            Surface(color = PremiumColors.Border, shape = CircleShape) {
                Text(tickets.size.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
            }
        }
        HorizontalDivider(color = PremiumColors.Border)
        Spacer(Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(tickets, key = { it.id }) { ticket ->
                TicketCard(ticket, mechanicsList, { onAssignMechanic(ticket.id, it) }, onAddNewMechanic, { onActionClick(ticket.id) })
            }
        }
    }
}

@Composable
private fun TicketCard(ticket: MaintenanceTicket, mechanicsList: List<String>, onAssignMechanic: (String) -> Unit, onAddNewMechanic: () -> Unit, onActionClick: () -> Unit) {
    val priorityColor = when (ticket.priority) {
        "CRITICAL" -> Color(0xFFDC2626)
        "HIGH" -> Color(0xFFF59E0B)
        else -> Color(0xFF94A3B8)
    }

    Card(
        modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = PremiumColors.CardSurface)
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            Box(Modifier.fillMaxHeight().width(6.dp).background(if(ticket.status == "PREDICTIVE") PremiumColors.StatusAI else priorityColor))
            Column(Modifier.padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(ticket.id, fontSize = 12.sp, color = PremiumColors.SecondaryText, fontWeight = FontWeight.Bold)
                    Surface(color = priorityColor.copy(0.1f), shape = RoundedCornerShape(4.dp), border = BorderStroke(1.dp, priorityColor.copy(0.3f))) {
                        Text(ticket.priority, color = priorityColor, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.PrecisionManufacturing, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(ticket.machineId, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                }
                Text(ticket.issueDescription, fontSize = 14.sp, color = PremiumColors.SecondaryText, maxLines = 2, overflow = TextOverflow.Ellipsis)

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = PremiumColors.Border.copy(0.5f))
                Spacer(Modifier.height(12.dp))

                // Audit Trail (Matches Image)
                AuditTrailRow(Icons.Rounded.Warning, "Logged by: ", ticket.raisedBy, ticket.timestamp)
                if (ticket.status != "PENDING" && ticket.status != "PREDICTIVE") {
                    AuditTrailRow(Icons.Rounded.Engineering, "Dispatched to: ", ticket.assignedMechanic ?: "", ticket.assignedTime ?: "")
                }
                if (ticket.status == "RESOLVED") {
                    AuditTrailRow(Icons.Rounded.AssignmentTurnedIn, "Closed by: ", ticket.assignedMechanic ?: "", ticket.resolvedTime ?: "", PremiumColors.StatusGood)
                    // Render Signature preview here if it exists...
                }

                Spacer(Modifier.height(16.dp))

                // Actions
                if (ticket.status == "PENDING" || ticket.status == "PREDICTIVE") {
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        Button(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6).copy(0.1f)), shape = RoundedCornerShape(6.dp)) {
                            Icon(Icons.Rounded.PersonAdd, null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("DISPATCH TECHNICIAN", color = Color(0xFF8B5CF6), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        DropdownMenu(expanded, { expanded = false }) {
                            mechanicsList.forEach { DropdownMenuItem(text = { Text(it) }, onClick = { onAssignMechanic(it); expanded = false }) }
                        }
                    }
                } else if (ticket.status == "ACTIVE") {
                    Button(onClick = onActionClick, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = PremiumColors.StatusGood.copy(0.1f)), shape = RoundedCornerShape(6.dp)) {
                        Icon(Icons.Rounded.Create, null, tint = PremiumColors.StatusGood, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("SIGN & CLOSE WORK ORDER", color = PremiumColors.StatusGood, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

@Composable
fun AuditTrailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, name: String, time: String, tint: Color = PremiumColors.SecondaryText) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(label, fontSize = 12.sp, color = PremiumColors.SecondaryText)
            Text(name, fontSize = 12.sp, color = PremiumColors.PrimaryText, fontWeight = FontWeight.Bold)
        }
        Text(time, fontSize = 12.sp, color = tint, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SignatureDialog(ticketId: String, onDismiss: () -> Unit, onSubmit: (List<List<Offset>>) -> Unit) {
    var lines by remember { mutableStateOf(emptyList<List<Offset>>()) }
    var currentLine by remember { mutableStateOf(emptyList<Offset>()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = PremiumColors.CardSurface)) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Technician Sign-Off", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Draw signature to close $ticketId", color = PremiumColors.SecondaryText)
                Box(Modifier.fillMaxWidth().height(200.dp).background(Color(0xFFF8FAFC)).border(1.dp, PremiumColors.Border)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { currentLine = listOf(it) },
                            onDrag = { change, _ -> currentLine = currentLine + change.position },
                            onDragEnd = { lines = lines + listOf(currentLine); currentLine = emptyList() }
                        )
                    }
                ) {
                    Canvas(Modifier.fillMaxSize()) {
                        (lines + listOf(currentLine)).forEach { line ->
                            if (line.size > 1) {
                                val path = Path().apply {
                                    moveTo(line[0].x, line[0].y)
                                    line.drop(1).forEach { lineTo(it.x, it.y) }
                                }
                                drawPath(path, Color.Black, style = Stroke(6f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                            }
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { lines = emptyList() }) { Text("CLEAR") }
                    Button(onClick = { onSubmit(lines) }, enabled = lines.isNotEmpty(), colors = ButtonDefaults.buttonColors(containerColor = PremiumColors.StatusGood)) { Text("SUBMIT") }
                }
            }
        }
    }
}