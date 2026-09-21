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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.elachi.app.ui.common.ElachiTopBar

// This screen displays Elachi's Terms of Service and explains the conditions users agree to when using the application.
// It presents information about user accounts, uploaded content, cooking safety, application availability, and acceptable use.
// The terms are organised into clear sections using reusable cards within a vertically scrollable Compose interface.

private val TERMS_SECTIONS = listOf(
    PolicySection(
        title = "About Elachi",
        icon = Icons.Filled.Info,
        body = "Elachi is a recipe management Android app developed as a student " +
                "project for the Programming 3D module (PROG7314) at The Independent " +
                "Institute of Education. The name comes from the Hindi and Urdu word " +
                "for cardamom, one of the most widely used spices in Indian cooking.\n\n" +
                "The app helps home cooks save, organise and cook their own recipes. " +
                "Users can build a personal digital cookbook, track ingredients in " +
                "their pantry, get recipe suggestions based on what they already have " +
                "at home, follow step-by-step cooking instructions with built-in " +
                "timers, and chat with an AI Chef Assistant for cooking tips and " +
                "substitutions.\n\n" +
                "Elachi also includes features like camera-based recipe import (scan " +
                "a recipe card and have the app read it in), an achievements and " +
                "streaks system to encourage regular cooking, and a suite of kitchen " +
                "tools including a unit converter, calculator and timer.\n\n" +
                "It is provided for educational and demonstration purposes only, " +
                "without warranty of any kind.",
    ),
    PolicySection(
        title = "Your Account",
        icon = Icons.Filled.AccountCircle,
        body = "You are responsible for keeping your account credentials " +
                "secure. You must be at least 13 years old to use the app. " +
                "Do not create accounts on behalf of others without their " +
                "permission.",
    ),
    PolicySection(
        title = "Your Content",
        icon = Icons.Filled.Description,
        body = "You retain ownership of any recipes, photos, notes and " +
                "other content you upload. By uploading, you grant us a limited " +
                "licence to store and display that content within the app so " +
                "that we can provide the service.",
    ),
    PolicySection(
        title = "What Not to Upload",
        icon = Icons.Filled.Warning,
        body = "Content that infringes copyright or trademarks, contains " +
                "illegal material, is abusive or harmful, or contains malware " +
                "or attempts to disrupt the service. Accounts that violate " +
                "this may be suspended or deleted.",
    ),
    PolicySection(
        title = "Recipes and Cooking",
        icon = Icons.Filled.Restaurant,
        body = "Recipes from other users, or suggested by the AI Chef " +
                "Assistant, are not professional advice. Always use your own " +
                "judgement when cooking, especially regarding allergens, food " +
                "safety and dietary requirements.",
    ),
    PolicySection(
        title = "Availability",
        icon = Icons.Filled.Sync,
        body = "The app may be unavailable from time to time for " +
                "maintenance, hosting limits (such as free-tier cloud services " +
                "waking up), or reasons outside our control. We do not " +
                "guarantee continuous availability.",
    ),
    PolicySection(
        title = "Changes and Termination",
        icon = Icons.Filled.Gavel,
        body = "We may update these terms or discontinue the app at any " +
                "time. Continued use after changes means you accept the new " +
                "terms. We may suspend or delete accounts that violate these " +
                "terms.",
    ),
)

@Composable
fun TermsOfServiceScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = { ElachiTopBar(title = "Terms of Service", onBackClick = onBack) },
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
                icon = Icons.Filled.Gavel,
                title = "Terms of Service",
                subtitle = "Last updated: September 2026",
            )

            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "By using Elachi, you agree to these terms. " +
                            "If you do not agree, please do not use the app.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(16.dp),
                )
            }

            TERMS_SECTIONS.forEachIndexed { index, section ->
                LegalSectionCard(number = index + 1, section = section)
            }

            Text(
                text = "For questions, use the Help & Support screen in the app.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}
