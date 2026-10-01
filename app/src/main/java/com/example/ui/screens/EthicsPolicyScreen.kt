package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.SafeSaffron
import com.example.ui.theme.SafeTeal

@Composable
fun EthicsPolicyScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Responsible AI & Ethics Architecture",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "SafePath India is designed strictly as an environmental risk-awareness tool, adhering to Indian digital privacy standards and responsible AI ethics.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 17.sp
        )

        EthicsCard(
            icon = Icons.Default.Shield,
            title = "1. No Absolute Safety Guarantee",
            description = "The application explicitly never claims that any route is 100% safe. Safety scores reflect relative environmental indicators (lighting, footfall, accessibility). Users must always exercise personal situational awareness.",
            accent = SafeSaffron
        )

        EthicsCard(
            icon = Icons.Default.Lock,
            title = "2. Strict Victim & Individual Privacy",
            description = "SafePath never identifies alleged criminals or reveals victims' identities. Historical data is generalized to road segments with time-decay weighting rather than pinpointing sensitive private locations.",
            accent = SafeTeal
        )

        EthicsCard(
            icon = Icons.Default.LocationOff,
            title = "3. Zero Location Harvesting",
            description = "Traveler location is processed entirely on-device and shared only with the designated emergency contact (parent/guardian) via user-triggered SMS or during active journey check-ins. No persistent tracking trails are saved on central servers.",
            accent = SafeGreen
        )

        EthicsCard(
            icon = Icons.Default.NotListedLocation,
            title = "4. Preventing Community Stigmatization",
            description = "Entire neighborhoods are never categorized or labeled as 'unsafe'. Only specific road segments with actionable infrastructure deficits (e.g. broken streetlights, road obstruction) are flagged for municipal rectification.",
            accent = SafeTeal
        )

        EthicsCard(
            icon = Icons.Default.Verified,
            title = "5. Transparent Explainable Scoring",
            description = "No black-box machine learning is used to predict individual crimes. Instead, an explainable weighted multi-criteria model (25% Lighting, 20% Crowd, 25% Incident History, 10% Activity, 10% Emergency Access, 10% Time of Day) is fully inspectable by the user.",
            accent = SafeSaffron
        )

        EthicsCard(
            icon = Icons.Default.Emergency,
            title = "6. ERSS 112 Official Integration",
            description = "SafePath does not pretend to substitute official law enforcement. India's Emergency Response Support System (ERSS 112) is natively integrated for rapid one-touch emergency response.",
            accent = SafeGreen
        )
    }
}

@Composable
fun EthicsCard(
    icon: ImageVector,
    title: String,
    description: String,
    accent: androidx.compose.ui.graphics.Color
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
            }
        }
    }
}
