package com.dosemate.app.ui.cabinet

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dosemate.app.data.local.entity.TimeOfDay
import com.dosemate.app.ui.components.AppLogoIcon
import com.dosemate.app.ui.components.MedicationIconBadge
import com.dosemate.app.ui.theme.BackgroundLight
import com.dosemate.app.ui.theme.InactiveButton
import com.dosemate.app.ui.theme.MintBadge
import com.dosemate.app.ui.theme.MintText
import com.dosemate.app.ui.theme.SoftCoral
import com.dosemate.app.ui.theme.SoftCoralBadge
import com.dosemate.app.ui.theme.TealPrimary
import com.dosemate.app.ui.theme.TextPrimary
import com.dosemate.app.ui.theme.TextSecondary
import com.dosemate.app.viewmodel.CabinetCardUi
import com.dosemate.app.viewmodel.CabinetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CabinetScreen(
    viewModel: CabinetViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundLight,
        contentWindowInsets = WindowInsets.statusBars,
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::openAddSheet,
                containerColor = TealPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Add New", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            CabinetTopBar()
            InventoryHeader(
                count = state.medicationCount,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = state.cards,
                    key = { it.medicationId },
                    span = { if (it.isWide) GridItemSpan(2) else GridItemSpan(1) }
                ) { card ->
                    if (card.isWide) {
                        WideMedicationCard(card = card, onAdjust = {
                            viewModel.adjustStock(card.medicationId, (card.currentCount - 1).coerceAtLeast(0))
                        })
                    } else {
                        MedicationStockCard(card = card)
                    }
                }
            }
        }
    }

    if (state.showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = viewModel::dismissAddSheet,
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            AddMedicationSheetContent(
                onDismiss = viewModel::dismissAddSheet,
                onSave = { name, dosage, form, current, max, hour, minute, tod ->
                    viewModel.addMedication(name, dosage, form, current, max, hour, minute, tod)
                }
            )
        }
    }
}

@Composable
private fun CabinetTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppLogoIcon()
        Spacer(Modifier.width(10.dp))
        Text(
            text = "Medicine Cabinet",
            style = MaterialTheme.typography.titleLarge,
            color = TealPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(TealPrimary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text("DM", color = TealPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
private fun InventoryHeader(count: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Inventory Status",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Text(
                text = "$count Medications Tracked",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }
        TextButton(
            onClick = {},
            colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary),
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(InactiveButton)
        ) {
            Icon(Icons.Outlined.History, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Refill Log", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun MedicationStockCard(card: CabinetCardUi) {
    val progressColor = if (card.isCritical || card.isLowStock) SoftCoral else TealPrimary
    val badgeBg = when {
        card.isCritical || card.isLowStock -> SoftCoralBadge
        card.percent >= 70 -> MintBadge
        else -> InactiveButton
    }
    val badgeFg = when {
        card.isCritical || card.isLowStock -> SoftCoral
        card.percent >= 70 -> MintText
        else -> TextSecondary
    }
    val badgeIcon = when {
        card.isCritical -> Icons.Outlined.NotificationsActive
        card.isLowStock -> Icons.Outlined.WarningAmber
        card.percent >= 70 -> Icons.Outlined.CheckCircle
        else -> Icons.Outlined.Science
    }
    val unitLabel = when (card.unit) {
        "ml" -> "${card.currentCount}/${card.maxCount} ml"
        "doses" -> "${card.currentCount}/${card.maxCount} left"
        else -> "${card.currentCount}/${card.maxCount} left"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                MedicationIconBadge(icon = card.icon, size = 44.dp)
                CircularStockProgress(percent = card.percent, color = progressColor)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = card.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = card.formLine,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(badgeBg)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = badgeIcon,
                    contentDescription = null,
                    tint = badgeFg,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = unitLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = badgeFg,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun WideMedicationCard(card: CabinetCardUi, onAdjust: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MedicationIconBadge(icon = card.icon, size = 72.dp)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = card.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Course: 10 days",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = "${card.currentCount}/${card.maxCount} doses left",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularStockProgress(percent = card.percent, color = TealPrimary, size = 48.dp)
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onAdjust) {
                    Text("Adjust", color = TealPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun CircularStockProgress(
    percent: Int,
    color: Color,
    size: androidx.compose.ui.unit.Dp = 40.dp
) {
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            drawArc(
                color = color.copy(alpha = 0.15f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = stroke
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * (percent / 100f),
                useCenter = false,
                style = stroke
            )
        }
        Text(
            text = "$percent%",
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun AddMedicationSheetContent(
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        dosage: String,
        form: String,
        current: Int,
        max: Int,
        hour: Int,
        minute: Int,
        timeOfDay: TimeOfDay
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var form by remember { mutableStateOf("Tablet") }
    var current by remember { mutableStateOf("30") }
    var max by remember { mutableStateOf("30") }
    var hour by remember { mutableIntStateOf(8) }
    var minute by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Add Medication",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = dosage,
            onValueChange = { dosage = it },
            label = { Text("Dosage (e.g. 500mg)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = form,
            onValueChange = { form = it },
            label = { Text("Form") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = current,
                onValueChange = { current = it.filter(Char::isDigit) },
                label = { Text("Current") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            OutlinedTextField(
                value = max,
                onValueChange = { max = it.filter(Char::isDigit) },
                label = { Text("Capacity") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        }
        Text("Morning reminder time", style = MaterialTheme.typography.labelLarge, color = TealPrimary)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = hour.toString().padStart(2, '0'),
                onValueChange = {
                    hour = it.filter(Char::isDigit).toIntOrNull()?.coerceIn(0, 23) ?: hour
                },
                label = { Text("Hour") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            OutlinedTextField(
                value = minute.toString().padStart(2, '0'),
                onValueChange = {
                    minute = it.filter(Char::isDigit).toIntOrNull()?.coerceIn(0, 59) ?: minute
                },
                label = { Text("Minute") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        }
        Button(
            onClick = {
                if (name.isNotBlank() && dosage.isNotBlank()) {
                    val tod = when (hour) {
                        in 5..11 -> TimeOfDay.MORNING
                        in 12..16 -> TimeOfDay.AFTERNOON
                        else -> TimeOfDay.EVENING
                    }
                    onSave(
                        name,
                        dosage,
                        form.ifBlank { "Tablet" },
                        current.toIntOrNull() ?: 30,
                        max.toIntOrNull() ?: 30,
                        hour,
                        minute,
                        tod
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
        ) {
            Text("Save Medication", fontWeight = FontWeight.SemiBold)
        }
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Cancel", color = TextSecondary)
        }
    }
}
