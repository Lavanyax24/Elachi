package com.elachi.app.ui.help

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elachi.app.ui.common.ElachiTopBar
import com.elachi.app.ui.theme.ElachiCream
import com.elachi.app.ui.theme.ElachiGreen
import com.elachi.app.ui.theme.ElachiTextPrimary
import com.elachi.app.ui.theme.ElachiTextSecondary

private data class Faq(val question: String, val answer: String)

private val FAQS = listOf(
    Faq(
        "How do I add a recipe?",
        "From My Cookbook, tap the + button, then choose Manual, Camera, or " +
                "Screenshot. Camera and Screenshot use on-device text recognition " +
                "plus an AI service to fill in the recipe form automatically, which " +
                "you can review and edit before saving.",
    ),
    Faq(
        "How does the OCR import work?",
        "When you photograph a recipe card or import a screenshot, the app " +
                "reads the text on-device, then sends it to a cooking-specific AI " +
                "service that organises it into a title, ingredients, steps, method, " +
                "servings, and cook time. You always get to review and edit before " +
                "saving.",
    ),
    Faq(
        "How do I use the serving scaler?",
        "On Recipe Detail, use the plus and minus buttons next to Servings, " +
                "and every ingredient quantity recalculates automatically. Perfect " +
                "for cooking for a different number of people than the recipe was " +
                "written for.",
    ),
    Faq(
        "What are streaks and achievements?",
        "Every time you finish Cook Mode for a recipe, it counts toward your " +
                "daily cooking streak and toward achievement badges. Check the " +
                "Streak Calendar to see which days you've cooked on and track your " +
                "progress over time.",
    ),
    Faq(
        "What can the AI Chef Assistant do?",
        "Ask it anything cooking-related - substitutions, recipe ideas from " +
                "leftovers, nutritional information, or general cooking tips. It " +
                "also knows what's in your pantry and can suggest meals based on " +
                "what you already have at home.",
    ),
    Faq(
        "How does the pantry match work?",
        "The app compares each of your recipes against what you've added to " +
                "your pantry. Recipes where you already have most of the " +
                "ingredients are shown as suggestions on the Home screen, so you " +
                "can decide what to cook based on what's in your kitchen.",
    ),
)

@Composable
fun HelpScreen(
    onBack: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit = {},
    onNavigateToTerms: () -> Unit = {},
    onOpenGettingStarted: () -> Unit = {},
) {
    Scaffold(
        topBar = { ElachiTopBar(title = "Help & Support", onBackClick = onBack) },
        containerColor = ElachiCream,
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            // ---------- Getting Started CTA ----------
            Surface(
                color = ElachiGreen,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenGettingStarted),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Filled.AutoStories,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Getting Started",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                        )
                        Text(
                            "Revisit the Elachi introduction slides",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                        )
                    }
                    Icon(
                        Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ---------- FAQs ----------
            SectionHeader("Frequently Asked Questions")
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E2DD)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    FAQS.forEachIndexed { index, faq ->
                        FaqRow(faq)
                        if (index < FAQS.lastIndex) {
                            Divider(
                                color = Color(0xFFE5E2DD),
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ---------- About ----------
            SectionHeader("About")
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E2DD)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    IconRow(
                        icon = Icons.Filled.Info,
                        title = "App Version",
                        subtitle = "v1.0.0-part2",
                        onClick = null,
                    )
                    Divider(color = Color(0xFFE5E2DD), modifier = Modifier.padding(start = 56.dp))
                    IconRow(
                        icon = Icons.Filled.PrivacyTip,
                        title = "Privacy Policy",
                        onClick = onNavigateToPrivacyPolicy,
                    )
                    Divider(color = Color(0xFFE5E2DD), modifier = Modifier.padding(start = 56.dp))
                    IconRow(
                        icon = Icons.Filled.Description,
                        title = "Terms of Service",
                        onClick = onNavigateToTerms,
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ---------- Building blocks ----------

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            fontSize = 11.sp,
        ),
        color = ElachiTextSecondary,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
    )
}

@Composable
private fun FaqRow(faq: Faq) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                faq.question,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = ElachiTextPrimary,
                modifier = Modifier.weight(1f),
            )
            Icon(
                if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = null,
                tint = ElachiTextSecondary,
                modifier = Modifier.size(20.dp),
            )
        }
        if (expanded) {
            Spacer(Modifier.height(8.dp))
            Text(
                faq.answer,
                style = MaterialTheme.typography.bodyMedium,
                color = ElachiTextSecondary,
                lineHeight = 21.sp,
            )
        }
    }
}

@Composable
private fun IconRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)?,
) {
    val modifier = Modifier
        .fillMaxWidth()
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)

    Row(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = ElachiGreen, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = ElachiTextPrimary,
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = ElachiTextSecondary,
                )
            }
        }
        if (onClick != null) {
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = ElachiTextSecondary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}