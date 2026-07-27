package com.dosemate.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Vaccines
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dosemate.app.data.local.entity.MedicationIcon
import com.dosemate.app.ui.theme.SoftCoralContainer
import com.dosemate.app.ui.theme.TealLight
import com.dosemate.app.ui.theme.TealPrimary

@Composable
fun MedicationIconBadge(
    icon: MedicationIcon,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    tintBackground: Color? = null
) {
    val bg = tintBackground ?: when (icon) {
        MedicationIcon.PILL -> SoftCoralContainer
        MedicationIcon.KIT -> TealLight
        MedicationIcon.SYRINGE -> Color(0xFFEEEEEE)
        MedicationIcon.CLIPBOARD -> SoftCoralContainer
        MedicationIcon.BOTTLE -> Color(0xFFE8EAF6)
        MedicationIcon.BLISTER -> Color(0xFFE0F2F1)
    }
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(14.dp))
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon.toImageVector(),
            contentDescription = null,
            tint = TealPrimary,
            modifier = Modifier.size(size * 0.45f)
        )
    }
}

fun MedicationIcon.toImageVector(): ImageVector = when (this) {
    MedicationIcon.PILL -> Icons.Outlined.Medication
    MedicationIcon.KIT -> Icons.Outlined.MedicalServices
    MedicationIcon.SYRINGE -> Icons.Outlined.Vaccines
    MedicationIcon.CLIPBOARD -> Icons.Outlined.Assignment
    MedicationIcon.BOTTLE -> Icons.Outlined.Science
    MedicationIcon.BLISTER -> Icons.Outlined.LocalHospital
}

@Composable
fun AppLogoIcon(modifier: Modifier = Modifier, size: Dp = 28.dp) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp))
            .background(TealPrimary),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.MedicalServices,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(size * 0.6f)
        )
    }
}

@Composable
fun TimeOfDayIcon(
    background: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TealPrimary,
            modifier = Modifier.size(18.dp)
        )
    }
}
