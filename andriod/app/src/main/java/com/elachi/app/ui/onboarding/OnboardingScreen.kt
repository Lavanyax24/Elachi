package com.elachi.app.ui.onboarding

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

// This the onboarding screens to show the user how to naviagate through the application

data class OnboardingSlide(
    val title: String,
    val subtitle: String,
    val icon: String,
    val img: String,
    val color: Color,
    val features: List<String>,
)

val ONBOARDING_SLIDES = listOf(
    OnboardingSlide(
        title = "Welcome to Elachi!",
        subtitle = "Your all-in-one culinary companion for discovering, saving, cooking, and sharing delicious meals from around the world.",
        icon = "\uD83C\uDF73",
        img = "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=500&fit=crop&auto=format",
        color = Color(0xFF3D2E13),
        features = listOf("Discover thousands of recipes", "Import from photos & screenshots", "Personalised meal suggestions"),
    ),
    OnboardingSlide(
        title = "Your Recipe Library",
        subtitle = "Organise every recipe in custom Recipe Books. Add personal notes, scale servings, and import from physical cookbooks using your camera.",
        icon = "\uD83D\uDCD6",
        img = "https://images.unsplash.com/photo-1476124369491-e7addf5db371?w=500&fit=crop&auto=format",
        color = Color(0xFF1A3A2A),
        features = listOf("Custom Recipe Books", "Camera & screenshot import", "Serving scaler & cook mode"),
    ),
    OnboardingSlide(
        title = "Smart Pantry",
        subtitle = "Track what you have at home, get automatic grocery lists for missing ingredients, and discover what you can cook right now.",
        icon = "\uD83E\uDD51",
        img = "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=500&fit=crop&auto=format",
        color = Color(0xFF1A2A3A),
        features = listOf("Pantry inventory tracking", "Auto grocery list generation", "Unit converter & kitchen timer"),
    ),
    OnboardingSlide(
        title = "Cook Mode",
        subtitle = "Step-by-step full-screen cooking guidance with a built-in timer, voice mode, and automatic step tracking so you never lose your place.",
        icon = "\uD83D\uDC68\u200D\uD83C\uDF73",
        img = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500&fit=crop&auto=format",
        color = Color(0xFF2A1A3A),
        features = listOf("Full-screen dark cook mode", "Built-in step timer", "Voice-guided cooking"),
    ),
    OnboardingSlide(
        title = "AI Chef Assistant",
        subtitle = "Ask our AI chef anything — substitutions, recipe ideas from leftovers, nutritional info, and cooking tips.",
        icon = "\uD83E\uDD16",
        img = "https://images.unsplash.com/photo-1490645935967-10de6ba17061?w=500&fit=crop&auto=format",
        color = Color(0xFF1A3A35),
        features = listOf("Ingredient substitution help", "Recipe ideas from leftovers", "Cooking tips & nutrition info"),
    ),
    OnboardingSlide(
        title = "Achievements & Streaks",
        subtitle = "Earn badges, build cooking streaks, and track your progress on the streak calendar as you cook your way through the app.",
        icon = "\uD83C\uDFC6",
        img = "https://images.unsplash.com/photo-1455619452474-d2be8b1e70cd?w=500&fit=crop&auto=format",
        color = Color(0xFF3A2A1A),
        features = listOf("Cooking streaks & badges", "Progress tracking", "Monthly streak calendar"),
    ),
    OnboardingSlide(
        title = "You're All Set!",
        subtitle = "Your culinary journey starts now. Create your profile, build your first Recipe Book, and start cooking something amazing!",
        icon = "\uD83C\uDF89",
        img = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500&fit=crop&auto=format",
        color = Color(0xFF425529),
        features = listOf("Set up your profile", "Create your first Recipe Book", "Discover trending recipes"),
    ),
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    tutorialMode: Boolean = false,
) {
    val pagerState = rememberPagerState(pageCount = { ONBOARDING_SLIDES.size })
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f), userScrollEnabled = true) { page ->
            val slide = ONBOARDING_SLIDES[page]
            OnboardingContent(
                slide = slide,
                pageIndex = page,
                totalPages = ONBOARDING_SLIDES.size,
                onSkip = onFinish,
                tutorialMode = tutorialMode,
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                repeat(ONBOARDING_SLIDES.size) { i ->
                    val width by animateDpAsState(targetValue = if (pagerState.currentPage == i) 24.dp else 8.dp, label = "")
                    Box(
                        modifier = Modifier.padding(horizontal = 3.dp).size(width = width, height = 8.dp).clip(CircleShape)
                            .background(if (pagerState.currentPage == i) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)),
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (pagerState.currentPage > 0) {
                    IconButton(
                        onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                        modifier = Modifier.size(50.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }

                val isLastSlide = pagerState.currentPage == ONBOARDING_SLIDES.size - 1
                val buttonLabel = when {
                    isLastSlide && tutorialMode -> "Done"
                    isLastSlide -> "\uD83C\uDF73 Start Cooking!"
                    else -> "Next"
                }

                Button(
                    onClick = {
                        if (pagerState.currentPage < ONBOARDING_SLIDES.size - 1) {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        } else {
                            onFinish()
                        }
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Text(
                            text = buttonLabel,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                        )
                        if (!isLastSlide) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingContent(
    slide: OnboardingSlide,
    pageIndex: Int,
    totalPages: Int,
    onSkip: () -> Unit,
    tutorialMode: Boolean,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth().height(260.dp).background(slide.color)) {
            AsyncImage(
                model = slide.img, contentDescription = null,
                modifier = Modifier.fillMaxSize().clip(RectangleShape),
                contentScale = ContentScale.Crop, alpha = 0.55f,
            )
            Box(
                modifier = Modifier.fillMaxSize().background(
                    brush = Brush.verticalGradient(colors = listOf(slide.color.copy(alpha = 0.53f), slide.color)),
                ),
            )

            TextButton(
                onClick = onSkip,
                modifier = Modifier.align(Alignment.TopEnd).padding(top = 44.dp, end = 16.dp).height(28.dp)
                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(20.dp)),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
            ) {
                Text(
                    if (tutorialMode) "CLOSE" else "SKIP",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, letterSpacing = 0.96.sp),
                    color = Color.White.copy(alpha = 0.85f),
                )
            }

            Row(
                modifier = Modifier.align(Alignment.BottomStart).padding(start = 20.dp, bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier.size(52.dp).background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(16.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(slide.icon, fontSize = 28.sp)
                }
                Text(
                    text = "${pageIndex + 1} / $totalPages",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, letterSpacing = 0.72.sp),
                    color = Color.White.copy(alpha = 0.65f),
                )
            }
        }

        Column(modifier = Modifier.fillMaxWidth().padding(top = 260.dp).padding(24.dp)) {
            Text(text = slide.title, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold), color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = slide.subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 23.sp)
            Spacer(modifier = Modifier.height(20.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                slide.features.forEachIndexed { index, feature -> FeatureItem(feature, slide.color, index) }
            }
        }
    }
}

@Composable
private fun FeatureItem(text: String, color: Color, index: Int) {
    val icons = listOf("\u2705", "\uD83C\uDFAF", "\u26A1")
    Row(
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(12.dp)).padding(11.dp, 14.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(modifier = Modifier.size(28.dp).background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
            Text(icons[index % icons.size], fontSize = 14.sp)
        }
        Text(text = text, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onSurface)
    }
}
