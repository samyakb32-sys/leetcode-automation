package com.leetcodeautomation.data

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.util.Date

/**
 * Draws a branded "proof" image summarizing a solved (or attempted) problem — a shareable
 * certificate rather than a fragile live screen-capture, so it's reliable regardless of what's
 * on screen when the run finishes.
 */
object ProofImageGenerator {
    private const val WIDTH = 1080
    private const val HEIGHT = 1080
    private const val BACKGROUND = "#0D1117"
    private const val ACCENT_GREEN = "#94DA32"
    private const val ACCENT_RED = "#FF6B6B"
    private const val TEXT_PRIMARY = "#DFE2EB"
    private const val TEXT_SECONDARY = "#8B93A7"

    private fun bitmap(entry: RunHistoryEntry): Bitmap {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.parseColor(BACKGROUND))

        val accent = Color.parseColor(if (entry.accepted) ACCENT_GREEN else ACCENT_RED)

        val brand = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accent
            textSize = 40f
            typeface = Typeface.MONOSPACE
            isFakeBoldText = true
        }
        canvas.drawText("REVERSE_LEETCODE", 72f, 140f, brand)

        val badgeRect = android.graphics.RectF(72f, 220f, 560f, 320f)
        val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accent
            alpha = 30
        }
        canvas.drawRoundRect(badgeRect, 20f, 20f, badgePaint)
        val badgeBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accent
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRoundRect(badgeRect, 20f, 20f, badgeBorder)
        val badgeText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accent
            textSize = 44f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }
        val label = if (entry.accepted) "✓ ACCEPTED" else entry.statusMsg.uppercase()
        canvas.drawText(label, 100f, 288f, badgeText)

        val slugPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor(TEXT_PRIMARY)
            textSize = 56f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }
        canvas.drawText(entry.titleSlug, 72f, 440f, slugPaint)

        val metaLabel = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor(TEXT_SECONDARY)
            textSize = 32f
            typeface = Typeface.MONOSPACE
        }
        canvas.drawText("Attempts: ${entry.attempts}", 72f, 520f, metaLabel)
        canvas.drawText(
            DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(entry.timestampMillis)),
            72f,
            568f,
            metaLabel,
        )
        if (entry.fromBackground) {
            canvas.drawText("Solved automatically in the background", 72f, 616f, metaLabel)
        }

        val footer = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor(TEXT_SECONDARY)
            textSize = 28f
            typeface = Typeface.MONOSPACE
        }
        canvas.drawText("reversed the odds on this one 🔁", 72f, HEIGHT - 72f, footer)

        return bitmap
    }

    /** Renders, saves to the device's Pictures, and returns a shareable content Uri. */
    suspend fun saveToGallery(context: Context, entry: RunHistoryEntry): Uri = withContext(Dispatchers.IO) {
        val bmp = bitmap(entry)
        val filename = "reverse-leetcode-${entry.titleSlug}-${entry.timestampMillis}.png"
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ReverseLeetCode")
            }
        }
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: throw IllegalStateException("Couldn't create image in the gallery")
        resolver.openOutputStream(uri)?.use { out ->
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
        } ?: throw IllegalStateException("Couldn't open the saved image for writing")
        uri
    }
}
