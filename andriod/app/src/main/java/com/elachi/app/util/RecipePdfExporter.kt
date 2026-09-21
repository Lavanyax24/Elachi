package com.elachi.app.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.StaticLayout
import android.text.TextPaint
import android.text.style.StyleSpan
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.elachi.app.data.local.entities.IngredientEntity
import com.elachi.app.data.local.entities.RecipeEntity
import com.elachi.app.data.local.entities.StepEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

//This is the class that exports the recipe into a pdf

// ---- Elachi palette, as android.graphics colours ----
private val GREEN = Color.parseColor("#425529")
private val GREEN_LIGHT = Color.parseColor("#5A6E3F")
private val GREEN_TINT = Color.parseColor("#EEF2E8")
private val ROW_ALT = Color.parseColor("#F6F3EE")
private val TEXT_DARK = Color.parseColor("#1C1C19")
private val TEXT_MUTED = Color.parseColor("#75786D")
private val DANGER = Color.parseColor("#BA1A1A")
private val DANGER_TINT = Color.parseColor("#FDECEA")

private const val PAGE_W = 595
private const val PAGE_H = 842
private const val MARGIN = 40f
private const val FOOTER_SPACE = 34f
private const val HERO_HEIGHT = 200f

object RecipePdfExporter {

    suspend fun export(
        context: Context,
        recipe: RecipeEntity,
        ingredients: List<IngredientEntity>,
        steps: List<StepEntity>,
        currentServings: Int,
    ): File {
        val hero = recipe.imageUrl?.takeIf { it.isNotBlank() }?.let { loadBitmap(context, it) }

        return withContext(Dispatchers.IO) {
            val doc = PdfDocument()
            try {
                val w = PageWriter(doc)
                w.startPage()
                drawRecipe(w, recipe, ingredients, steps, currentServings, hero)
                w.finishPage()

                val dir = File(context.cacheDir, "pdfs").apply { mkdirs() }
                val file = File(dir, recipe.title.replace(Regex("[^A-Za-z0-9]+"), "_") + ".pdf")
                FileOutputStream(file).use { doc.writeTo(it) }
                file
            } finally {
                doc.close()
            }
        }
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

    private suspend fun loadBitmap(context: Context, url: String): Bitmap? = try {
        val request = ImageRequest.Builder(context)
            .data(url)
            .allowHardware(false)
            .size(1200)
            .build()
        (context.imageLoader.execute(request) as? SuccessResult)?.drawable?.toBitmap()
    } catch (e: Exception) {
        android.util.Log.e("RecipePdfExporter", "Could not load recipe image", e)
        null
    }

    private fun formatQty(q: Double): String =
        if (q % 1.0 == 0.0) q.toInt().toString() else q.toString()

    private fun drawRecipe(
        w: PageWriter,
        recipe: RecipeEntity,
        ingredients: List<IngredientEntity>,
        steps: List<StepEntity>,
        currentServings: Int,
        hero: Bitmap?,
    ) {
        val contentW = PAGE_W - 2 * MARGIN

        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 26f; typeface = Typeface.DEFAULT_BOLD; color = Color.WHITE
        }
        val metaPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 12f; color = Color.argb(215, 255, 255, 255)
        }
        val headingPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 17f; typeface = Typeface.DEFAULT_BOLD; color = GREEN
        }
        val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 12f; color = TEXT_DARK }
        val qtyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 12f; typeface = Typeface.DEFAULT_BOLD; color = GREEN
        }

        // ---------- Green header band ----------
        val titleLayout = layout(recipe.title, titlePaint, contentW.toInt())
        val meta = listOf(
            recipe.cuisine.takeIf { it.isNotBlank() },
            "${recipe.cookTimeMinutes} min",
            recipe.difficulty.takeIf { it.isNotBlank() },
            "Serves $currentServings",
        ).filterNotNull().joinToString("  ·  ")
        val metaLayout = layout(meta, metaPaint, contentW.toInt())

        val bandHeight = 30f + titleLayout.height + 8f + metaLayout.height + 24f
        w.canvas.drawRect(0f, 0f, PAGE_W.toFloat(), bandHeight, Paint().apply { color = GREEN })
        w.canvas.drawRect(0f, bandHeight, PAGE_W.toFloat(), bandHeight + 4f, Paint().apply { color = GREEN_LIGHT })
        w.drawLayout(titleLayout, MARGIN, 30f)
        w.drawLayout(metaLayout, MARGIN, 30f + titleLayout.height + 8f)
        w.y = bandHeight + 22f

        // ---------- Photo ----------
        if (hero != null) {
            drawHero(w.canvas, hero, MARGIN, w.y, contentW, HERO_HEIGHT)
            w.y += HERO_HEIGHT + 18f
        }

        // ---------- Allergens ----------
        val allergens = recipe.allergensCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        drawAllergenBox(w, allergens, contentW)

        // ---------- Ingredients ----------
        w.heading("Ingredients", headingPaint)
        val qtyColW = 130f
        ingredients.forEachIndexed { index, ing ->
            val q = ServingScaler.scale(ing.quantity, recipe.servings, currentServings)
            val nameLayout = layout(ing.name, bodyPaint, (contentW - qtyColW - 24f).toInt())
            val qtyLayout = layout(
                "${formatQty(q)} ${ing.unit}".trim(), qtyPaint, (qtyColW - 12f).toInt(), Layout.Alignment.ALIGN_OPPOSITE,
            )
            val rowH = maxOf(nameLayout.height, qtyLayout.height) + 12f
            w.ensureSpace(rowH)
            if (index % 2 == 0) {
                w.canvas.drawRoundRect(
                    RectF(MARGIN, w.y, MARGIN + contentW, w.y + rowH), 6f, 6f,
                    Paint().apply { color = ROW_ALT },
                )
            }
            w.drawLayout(nameLayout, MARGIN + 12f, w.y + 6f)
            w.drawLayout(qtyLayout, MARGIN + contentW - qtyColW, w.y + 6f)
            w.y += rowH
        }
        w.y += 12f

        // ---------- Steps ----------
        w.heading("Method", headingPaint)
        val circleR = 11f
        val numberPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 11f; typeface = Typeface.DEFAULT_BOLD; color = Color.WHITE; textAlign = Paint.Align.CENTER
        }
        steps.sortedBy { it.order }.forEach { step ->
            val textLayout = layout(step.instruction, bodyPaint, (contentW - 36f).toInt())
            val rowH = maxOf(textLayout.height.toFloat(), circleR * 2) + 12f
            w.ensureSpace(rowH)
            val cx = MARGIN + circleR
            val cy = w.y + circleR
            w.canvas.drawCircle(cx, cy, circleR, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = GREEN })
            w.canvas.drawText("${step.order + 1}", cx, cy + 4f, numberPaint)
            w.drawLayout(textLayout, MARGIN + 36f, w.y + 3f)
            w.y += rowH
        }
    }

    private fun drawHero(canvas: Canvas, bmp: Bitmap, x: Float, y: Float, w: Float, h: Float) {
        val targetRatio = w / h
        val srcRatio = bmp.width.toFloat() / bmp.height
        val src = if (srcRatio > targetRatio) {
            val sw = (bmp.height * targetRatio).toInt()
            Rect((bmp.width - sw) / 2, 0, (bmp.width + sw) / 2, bmp.height)
        } else {
            val sh = (bmp.width / targetRatio).toInt()
            Rect(0, (bmp.height - sh) / 2, bmp.width, (bmp.height + sh) / 2)
        }
        val dst = RectF(x, y, x + w, y + h)
        canvas.save()
        canvas.clipPath(Path().apply { addRoundRect(dst, 14f, 14f, Path.Direction.CW) })
        canvas.drawBitmap(bmp, src, dst, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
        canvas.restore()
    }

    private fun drawAllergenBox(w: PageWriter, allergens: List<String>, contentW: Float) {
        val has = allergens.isNotEmpty()
        val colour = if (has) DANGER else GREEN
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 12f; color = colour }

        val text = SpannableStringBuilder("Allergens: ").apply {
            setSpan(StyleSpan(Typeface.BOLD), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            append(if (has) allergens.joinToString(", ") else "none listed")
        }
        val l = StaticLayout.Builder.obtain(text, 0, text.length, paint, (contentW - 28f).toInt()).build()
        val boxH = l.height + 20f
        w.ensureSpace(boxH + 16f)

        val rect = RectF(MARGIN, w.y, MARGIN + contentW, w.y + boxH)
        w.canvas.drawRoundRect(rect, 10f, 10f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (has) DANGER_TINT else GREEN_TINT
        })
        w.canvas.drawRoundRect(rect, 10f, 10f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE; strokeWidth = 1f; color = colour
        })
        w.drawLayout(l, MARGIN + 14f, w.y + 10f)
        w.y += boxH + 20f
    }

    private fun layout(
        text: String, paint: TextPaint, width: Int,
        align: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL,
    ): StaticLayout =
        StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
            .setAlignment(align)
            .setLineSpacing(2f, 1f)
            .build()
}

private class PageWriter(private val doc: PdfDocument) {
    private var pageNumber = 0
    private lateinit var page: PdfDocument.Page
    var y = MARGIN
    val canvas: Canvas get() = page.canvas

    fun startPage() {
        pageNumber++
        page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, pageNumber).create())
        y = MARGIN
    }

    fun finishPage() {
        val footer = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 9f; color = TEXT_MUTED }
        val baseline = PAGE_H - 22f
        canvas.drawLine(MARGIN, baseline - 14f, PAGE_W - MARGIN, baseline - 14f, Paint().apply {
            color = Color.parseColor("#E5E2DD"); strokeWidth = 0.8f
        })
        canvas.drawText("Made with Elachi", MARGIN, baseline, footer)
        val label = "Page $pageNumber"
        canvas.drawText(label, PAGE_W - MARGIN - footer.measureText(label), baseline, footer)
        doc.finishPage(page)
    }

    fun ensureSpace(height: Float) {
        if (y + height > PAGE_H - MARGIN - FOOTER_SPACE) {
            finishPage()
            startPage()
        }
    }

    fun drawLayout(layout: StaticLayout, x: Float, top: Float) {
        canvas.save()
        canvas.translate(x, top)
        layout.draw(canvas)
        canvas.restore()
    }

    fun heading(text: String, paint: TextPaint) {
        ensureSpace(46f)
        canvas.drawText(text, MARGIN, y + paint.textSize, paint)
        val ruleY = y + paint.textSize + 7f
        canvas.drawRoundRect(
            RectF(MARGIN, ruleY, MARGIN + 44f, ruleY + 3f), 2f, 2f,
            Paint().apply { color = GREEN_LIGHT },
        )
        y = ruleY + 16f
    }
}