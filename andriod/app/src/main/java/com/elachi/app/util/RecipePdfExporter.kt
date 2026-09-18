package com.elachi.app.util

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.elachi.app.data.local.entities.IngredientEntity
import com.elachi.app.data.local.entities.RecipeEntity
import com.elachi.app.data.local.entities.StepEntity
import java.io.File
import java.io.FileOutputStream

object RecipePdfExporter {
    private const val PAGE_W = 595
    private const val PAGE_H = 842
    private const val MARGIN = 40

    fun export(
        context: Context, recipe: RecipeEntity,
        ingredients: List<IngredientEntity>, steps: List<StepEntity>,
        currentServings: Int,
    ): File {
        val doc = PdfDocument()
        val title = TextPaint().apply { textSize = 22f; typeface = Typeface.DEFAULT_BOLD; isAntiAlias = true }
        val heading = TextPaint().apply { textSize = 16f; typeface = Typeface.DEFAULT_BOLD; isAntiAlias = true }
        val body = TextPaint().apply { textSize = 12f; isAntiAlias = true }
        val width = PAGE_W - 2 * MARGIN

        var pageNumber = 1
        var page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, pageNumber).create())
        var y = MARGIN.toFloat()

        fun draw(text: String, paint: TextPaint, gap: Float = 6f) {
            val layout = StaticLayout.Builder.obtain(text, 0, text.length, paint, width).build()
            if (y + layout.height > PAGE_H - MARGIN) {
                doc.finishPage(page)
                pageNumber++
                page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, pageNumber).create())
                y = MARGIN.toFloat()
            }
            page.canvas.save()
            page.canvas.translate(MARGIN.toFloat(), y)
            layout.draw(page.canvas)
            page.canvas.restore()
            y += layout.height + gap
        }

        draw(recipe.title, title, 8f)
        draw("${recipe.cuisine} · ${recipe.cookTimeMinutes} min · ${recipe.difficulty} · Serves $currentServings", body, 14f)
        draw("Ingredients", heading)
        ingredients.forEach {
            val q = ServingScaler.scale(it.quantity, recipe.servings, currentServings)
            val qText = if (q % 1.0 == 0.0) q.toInt().toString() else q.toString()
            draw("• $qText ${it.unit} ${it.name}", body, 3f)
        }
        y += 10f
        draw("Steps", heading)
        steps.sortedBy { it.order }.forEach { draw("${it.order + 1}. ${it.instruction}", body, 6f) }

        doc.finishPage(page)
        val dir = File(context.cacheDir, "pdfs").apply { mkdirs() }
        val file = File(dir, recipe.title.replace(Regex("[^A-Za-z0-9]+"), "_") + ".pdf")
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        return file
    }

    fun share(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, "Share recipe PDF").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}