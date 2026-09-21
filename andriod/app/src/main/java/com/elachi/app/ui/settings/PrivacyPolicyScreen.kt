package com.elachi.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.ContactMail
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.elachi.app.ui.common.ElachiTopBar

private val PRIVACY_SECTIONS = listOf(
    PolicySection(
        title = "Information We Collect",
        icon = Icons.Filled.AccountCircle,
        body = "Your email address and a unique user ID when you sign up. " +
                "Your display name, bio, profile photo, cooking interests and " +
                "dietary restrictions if you add them. Recipes, ingredients, " +
                "steps, photos, notes and pantry items you create. Basic usage " +
                "data - which recipes you've cooked - to power streaks and " +
                "achievements.",
    ),
    PolicySection(
        title = "How We Use Your Information",
        icon = Icons.Filled.Info,
        body = "Only to provide the app's features: saving your recipes, " +
                "syncing them across devices, giving pantry-aware suggestions, " +
                "and showing your cooking progress. We never sell, rent or " +
                "share your data with third parties.",
    ),
    PolicySection(
        title = "Where Your Data Is Stored",
        icon = Icons.Filled.CloudQueue,
        body = "Authentication data is stored by Google Firebase " +
                "Authentication. Recipe and profile data is stored in a " +
                "Supabase PostgreSQL database. Images are stored in Supabase " +
                "Storage.",
    ),
    PolicySection(
        title = "Your Rights",
        icon = Icons.Filled.Gavel,
        body = "You can edit or delete any content you've created at any " +
                "time from within the app. You can delete your entire account " +
                "from Settings, which permanently removes all associated data " +
                "from our systems.",
    ),
    PolicySection(
        title = "Contact Us",
        icon = Icons.Filled.ContactMail,
        body = "For any questions about this policy, reach the project " +
                "team through the Help & Support screen in the app.",
    ),
)

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = { ElachiTopBar(title = "Privacy Policy", onBackClick = onBack) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LegalHeroCard(
                icon = Icons.Filled.PrivacyTip,
                title = "Your Privacy Matters",
                subtitle = "Last updated: September 2026",
            )

            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Elachi is a student project developed for the Programming 3D " +
                            "module (PROG7314). This policy explains what information the " +
                            "app collects, how it is used, and how you can remove it.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(16.dp),
                )
            }

            PRIVACY_SECTIONS.forEachIndexed { index, section ->
                LegalSectionCard(number = index + 1, section = section)
            }

            Text(
                text = "This policy may be updated as the project develops. " +
                        "Any changes will be reflected in the app.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}
