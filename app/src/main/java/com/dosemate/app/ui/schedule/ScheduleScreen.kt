package com.dosemate.app.ui.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dosemate.app.data.local.entity.DoseStatus
import com.dosemate.app.data.local.entity.MedicationIcon
import com.dosemate.app.data.local.entity.TimeOfDay
import com.dosemate.app.ui.components.AppLogoIcon
import com.dosemate.app.ui.components.MedicationIconBadge
import com.dosemate.app.ui.components.TimeOfDayIcon
import com.dosemate.app.ui.theme.AfternoonIconBg
import com.dosemate.app.ui.theme.BackgroundLight
import com.dosemate.app.ui.theme.BorderSubtle
import com.dosemate.app.ui.theme.EveningIconBg
import com.dosemate.app.ui.theme.InactiveButton
import com.dosemate.app.ui.theme.MorningIconBg
import com.dosemate.app.ui.theme.TealPrimary
import com.dosemate.app.ui.theme.TextPrimary
import com.dosemate.app.ui.theme.TextSecondary
import com.dosemate.app.viewmodel.DateChip
import com.dosemate.app.viewmodel.DoseCardUi
import com.dosemate.app.viewmodel.ScheduleSection
import com.dosemate.app.viewmodel.ScheduleViewModel

@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundLight,
        contentWindowInsets = WindowInsets.statusBars,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = TealPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("+", style = MaterialTheme.typography.headlineSmall, color = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ScheduleTopBar()
            DateSelectorRow(
                dates = state.weekDates,
                onSelect = viewModel::selectDate
            )
            Spacer(Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                state.sections.forEachIndexed { index, section ->
                    item(key = "header_${section.timeOfDay}") {
                        TimelineSectionHeader(
                            section = section,
                            isLast = index == state.sections.lastIndex && section.doses.isEmpty()
                        )
                    }
                    items(section.doses, key = { it.doseLogId }) { dose ->
                        TimelineDoseItem(
                            dose = dose,
                            showConnector = true,
                            onTake = { viewModel.takeDose(dose.doseLogId) },
                            onSnooze = { viewModel.snoozeDose(dose.doseLogId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppLogoIcon()
        Spacer(Modifier.width(10.dp))
        Text(
            text = "DoseMate",
            style = MaterialTheme.typography.titleLarge,
            color = TealPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = {}) {
            Icon(
                imageVector = Icons.Outlined.CalendarMonth,
                contentDescription = "Calendar",
                tint = TextSecondary
            )
        }
    }
}

@Composable
private fun DateSelectorRow(
    dates: List<DateChip>,
    onSelect: (java.time.LocalDate) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(dates, key = { it.date.toEpochDay() }) { chip ->
            DateChipItem(chip = chip, onClick = { onSelect(chip.date) })
        }
    }
}

@Composable
private fun DateChipItem(chip: DateChip, onClick: () -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier = Modifier
            .width(56.dp)
            .clip(shape)
            .background(if (chip.isSelected) TealPrimary else Color.White)
            .then(
                if (!chip.isSelected) Modifier.border(1.dp, BorderSubtle, shape)
                else Modifier
            )
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = chip.dayLabel,
            style = MaterialTheme.typography.labelMedium,
            color = if (chip.isSelected) Color.White.copy(alpha = 0.9f) else TextSecondary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = chip.dayNumber,
            style = MaterialTheme.typography.titleLarge,
            color = if (chip.isSelected) Color.White else TextPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun TimelineSectionHeader(section: ScheduleSection, isLast: Boolean) {
    val (bg, icon) = when (section.timeOfDay) {
        TimeOfDay.MORNING -> MorningIconBg to Icons.Outlined.WbSunny
        TimeOfDay.AFTERNOON -> AfternoonIconBg to Icons.Outlined.WbSunny
        TimeOfDay.EVENING -> EveningIconBg to Icons.Outlined.DarkMode
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 8.dp)
            .drawBehind {
                if (!isLast) {
                    drawLine(
                        color = BorderSubtle,
                        start = Offset(18.dp.toPx(), 36.dp.toPx()),
                        end = Offset(18.dp.toPx(), size.height + 40.dp.toPx()),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        TimeOfDayIcon(background = bg, icon = icon)
        Spacer(Modifier.width(12.dp))
        Text(
            text = section.title,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun TimelineDoseItem(
    dose: DoseCardUi,
    showConnector: Boolean,
    onTake: () -> Unit,
    onSnooze: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(1.dp)
                .align(Alignment.Top)
        )
        Spacer(Modifier.width(12.dp))
        DoseCard(
            dose = dose,
            onTake = onTake,
            onSnooze = onSnooze,
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 12.dp)
        )
    }
}

@Composable
private fun DoseCard(
    dose: DoseCardUi,
    onTake: () -> Unit,
    onSnooze: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = runCatching { MedicationIcon.valueOf(dose.iconName) }
        .getOrDefault(MedicationIcon.PILL)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MedicationIconBadge(icon = icon)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dose.medicationName,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = dose.dosageLine,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                Text(
                    text = dose.timeLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = TealPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(14.dp))
            when {
                dose.status == DoseStatus.TAKEN -> {
                    Text(
                        text = "Taken",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TealPrimary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                dose.isTooEarly -> {
                    OutlinedButton(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            disabledContentColor = TextSecondary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, TealPrimary.copy(alpha = 0.4f))
                    ) {
                        Text("Too early.")
                    }
                }
                else -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onTake,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TealPrimary),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, TealPrimary)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Take")
                        }
                        TextButton(
                            onClick = onSnooze,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(InactiveButton),
                            colors = ButtonDefaults.textButtonColors(contentColor = TextPrimary)
                        ) {
                            Text("Snooze")
                        }
                    }
                }
            }
        }
    }
}
