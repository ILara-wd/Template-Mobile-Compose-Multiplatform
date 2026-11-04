package com.invex.one.demo.ui.components.atoms

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Carga una imagen local (content:// o file://) reducida a ~[maxSide]px en un
 * hilo de IO. Devuelve null mientras carga o si no se puede leer. Evita
 * depender de una librería de imágenes para fotos de evidencia y logotipos.
 */
@Composable
fun rememberUriPainter(uri: String?, maxSide: Int = 512): Painter? {
    if (uri == null) return null
    val context = LocalContext.current
    val bitmap by produceState<ImageBitmap?>(null, uri, maxSide) {
        value = withContext(Dispatchers.IO) { decodeSampled(context, Uri.parse(uri), maxSide) }
    }
    return bitmap?.let { remember(it) { BitmapPainter(it) } }
}

private fun decodeSampled(context: Context, uri: Uri, maxSide: Int): ImageBitmap? = runCatching {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
    var sample = 1
    while (bounds.outWidth / (sample * 2) >= maxSide && bounds.outHeight / (sample * 2) >= maxSide) sample *= 2
    val opts = BitmapFactory.Options().apply { inSampleSize = sample }
    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }?.asImageBitmap()
}.getOrNull()

/** Tamaño en bytes reportado por el proveedor de contenido, si lo expone. */
fun uriFileSize(context: Context, uri: Uri): Long? = runCatching {
    context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { c ->
        if (c.moveToFirst() && !c.isNull(0)) c.getLong(0) else null
    }
}.getOrNull()
