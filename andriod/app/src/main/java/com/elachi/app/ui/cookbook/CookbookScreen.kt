package com.elachi.app.ui.cookbook

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.elachi.app.data.local.entities.RecipeBookEntity
import com.elachi.app.ui.theme.ElachiCream
import com.elachi.app.ui.theme.ElachiGreen
import androidx.compose.foundation.clickable

/**
 * Minimal My Cookbook screen — shows the user's Recipe Books as a grid.
 * Phase 4 will expand this with the All Recipes tab, filter chips,
 * edit/delete actions, cover photos, etc.
 */
@Composable
fun CookbookScreen(
    viewModel: CookbookViewModel,
    onOpenBook: (String) -> Unit = {},
    onOpenRecipe: (String) -> Unit = {},
    onAddRecipe: (String) -> Unit = {},
) {
    val books by viewModel.books.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(ElachiCream)) {
        if (books.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    "No Recipe Books yet",
                    style = MaterialTheme.typography.titleLarge,
                    color = ElachiGreen,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Create your first book from the Let's Get Organised step, or add one here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF75786D),
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(books) { book ->
                    BookCard(book = book, onClick = { onOpenBook(book.id) })
                }
            }
        }
    }
}

@Composable
private fun BookCard(book: RecipeBookEntity, onClick: () -> Unit) {
    val headerColor = runCatching {
        Color(android.graphics.Color.parseColor(book.colour))
    }.getOrDefault(ElachiGreen)

    Surface(
        modifier = Modifier.fillMaxWidth().height(180.dp),
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFE5E2DD)),
        onClick = onClick,
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .background(headerColor)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Text(
                    book.icon.ifBlank { "📖" },
                    fontSize = 28.sp,
                    modifier = Modifier.align(Alignment.CenterStart),
                )
            }

            Row(modifier = Modifier.fillMaxWidth().height(36.dp)) {
                if (book.coverImageUrl != null) {
                    AsyncImage(
                        model = book.coverImageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF0EDE9)))
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp).weight(1f),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        book.name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                    )
                    book.description?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF75786D),
                            maxLines = 1,
                        )
                    }
                }
                Text(
                    "0 recipes",
                    style = MaterialTheme.typography.labelSmall,
                    color = headerColor,
                )
            }
        }
    }
}