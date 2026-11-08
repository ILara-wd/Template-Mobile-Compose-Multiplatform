package com.invex.sportkid.feature.sharing

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.qrcode.encoder.Encoder
import com.invex.sportkid.core.designsystem.preview.SkPreviews
import com.invex.sportkid.core.designsystem.theme.SportKidTheme

/** Matriz del QR: `true` = módulo oscuro. Corrección H para tolerar el sello del centro. */
private class QrMatrix(val size: Int, private val cells: BooleanArray, val alignmentCenters: List<Int>) {
    operator fun get(x: Int, y: Int): Boolean = cells[y * size + x]

    companion object {
        fun encode(content: String): QrMatrix {
            val qr = Encoder.encode(content, ErrorCorrectionLevel.H)
            val m = qr.matrix
            val cells = BooleanArray(m.width * m.height) { m.get(it % m.width, it / m.width).toInt() == 1 }
            return QrMatrix(m.width, cells, qr.version.alignmentPatternCenters.toList())
        }
    }
}

/**
 * QR de invitación con el estilo de Figma ("Authentic Detailed QR"): módulos redondeados, patrones
 * de posición con esquinas suaves y el sello de SportKid al centro. El contenido es [payload].
 */
@Composable
internal fun InviteQrCode(
    payload: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    size: Dp = 224.dp,
    moduleColor: Color = MaterialTheme.colorScheme.onSurface,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    badgeColor: Color = MaterialTheme.colorScheme.primary,
) {
    val matrix = remember(payload) { QrMatrix.encode(payload) }
    Box(modifier.size(size).semantics { this.contentDescription = contentDescription }, contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(size)) { drawQr(matrix, moduleColor, backgroundColor) }
        // Sello central (Figma: 50.4dp `surface` y 39.2dp `primary` sobre un QR de 224dp).
        Box(
            Modifier
                .size(size * 0.225f)
                .background(backgroundColor, MaterialTheme.shapes.medium),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .size(size * 0.175f)
                    .background(badgeColor, MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painterResource(R.drawable.ic_share_qr_badge), contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

private fun DrawScope.drawQr(matrix: QrMatrix, dark: Color, light: Color) {
    val margin = size.minDimension * 0.05f
    val m = (size.minDimension - 2 * margin) / matrix.size
    val n = matrix.size
    val finders = listOf(0 to 0, n - 7 to 0, 0 to n - 7)
    val alignments = matrix.alignmentCenters.flatMap { cx -> matrix.alignmentCenters.map { cy -> cx to cy } }
        // Las combinaciones que caen sobre un patrón de posición no existen en el estándar.
        .filter { (cx, cy) -> finders.none { (fx, fy) -> cx in fx - 2..fx + 8 && cy in fy - 2..fy + 8 } }
    val center = n / 2f
    val clear = n * 0.225f / 2 + 0.5f

    fun inFinder(x: Int, y: Int) = finders.any { (fx, fy) -> x in fx..fx + 6 && y in fy..fy + 6 }
    fun inAlignment(x: Int, y: Int) = alignments.any { (cx, cy) -> x in cx - 2..cx + 2 && y in cy - 2..cy + 2 }
    fun inBadge(x: Int, y: Int) = x + 0.5f in center - clear..center + clear && y + 0.5f in center - clear..center + clear

    fun roundRect(color: Color, x: Float, y: Float, w: Float, r: Float) =
        drawRoundRect(color, Offset(margin + x * m, margin + y * m), Size(w * m, w * m), CornerRadius(r * m))

    for (y in 0 until n) for (x in 0 until n) {
        if (matrix[x, y] && !inFinder(x, y) && !inAlignment(x, y) && !inBadge(x, y)) {
            roundRect(dark, x + 0.08f, y + 0.08f, 0.84f, 0.28f)
        }
    }
    finders.forEach { (fx, fy) ->
        roundRect(dark, fx.toFloat(), fy.toFloat(), 7f, 1.4f)
        roundRect(light, fx + 1.05f, fy + 1.05f, 4.9f, 0.875f)
        roundRect(dark, fx + 2.1f, fy + 2.1f, 2.8f, 0.525f)
    }
    alignments.forEach { (cx, cy) ->
        roundRect(dark, cx - 2f, cy - 2f, 5f, 1f)
        roundRect(light, cx - 1.1f, cy - 1.1f, 3.2f, 0.6f)
        roundRect(dark, cx - 0.4f, cy - 0.4f, 1.8f, 0.35f)
    }
}

@SkPreviews
@Composable
private fun InviteQrCodePreview() = SportKidTheme {
    InviteQrCode(payload = SharingSamples.code(0).qrPayload, contentDescription = stringResource(R.string.share_qr_cd, "K7QM-4PZX"))
}
