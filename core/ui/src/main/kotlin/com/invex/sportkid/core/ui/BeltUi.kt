package com.invex.sportkid.core.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import com.invex.sportkid.core.designsystem.component.SkSelectField
import com.invex.sportkid.core.designsystem.preview.SkPreviews
import com.invex.sportkid.core.designsystem.theme.SkElevation
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.model.Belt

@StringRes
fun Belt.labelRes(): Int = when (this) {
    Belt.WHITE -> R.string.belt_white
    Belt.YELLOW -> R.string.belt_yellow
    Belt.ORANGE -> R.string.belt_orange
    Belt.GREEN -> R.string.belt_green
    Belt.BLUE -> R.string.belt_blue
    Belt.BROWN -> R.string.belt_brown
    Belt.RED -> R.string.belt_red
    Belt.RED_BLACK -> R.string.belt_red_black
    Belt.BLACK -> R.string.belt_black
}

/** "Verde". */
@StringRes
fun Belt.shortLabelRes(): Int = when (this) {
    Belt.WHITE -> R.string.belt_short_white
    Belt.YELLOW -> R.string.belt_short_yellow
    Belt.ORANGE -> R.string.belt_short_orange
    Belt.GREEN -> R.string.belt_short_green
    Belt.BLUE -> R.string.belt_short_blue
    Belt.BROWN -> R.string.belt_short_brown
    Belt.RED -> R.string.belt_short_red
    Belt.RED_BLACK -> R.string.belt_short_red_black
    Belt.BLACK -> R.string.belt_short_black
}

/** "6° Kup". Grados por defecto; TODO: confirmar con la escuela real (README §8). */
@StringRes
fun Belt.gradeRes(): Int = when (this) {
    Belt.WHITE -> R.string.belt_grade_white
    Belt.YELLOW -> R.string.belt_grade_yellow
    Belt.ORANGE -> R.string.belt_grade_orange
    Belt.GREEN -> R.string.belt_grade_green
    Belt.BLUE -> R.string.belt_grade_blue
    Belt.BROWN -> R.string.belt_grade_brown
    Belt.RED -> R.string.belt_grade_red
    Belt.RED_BLACK -> R.string.belt_grade_red_black
    Belt.BLACK -> R.string.belt_grade_black
}

/** Siguiente cinta de la secuencia, o `null` si ya es negra. */
val Belt.next: Belt? get() = Belt.entries.getOrNull(ordinal + 1)

/** Color principal de la cinta (punto, barra de progreso, línea de tiempo). */
@Composable
@ReadOnlyComposable
fun Belt.color(): Color = colors().first

/** Color principal y secundario (cintas divididas) de cada cinta. */
@Composable
@ReadOnlyComposable
private fun Belt.colors(): Pair<Color, Color?> {
    val b = SportKidTheme.sportColors.belt
    return when (this) {
        Belt.WHITE -> b.white to null
        Belt.YELLOW -> b.yellow to null
        Belt.ORANGE -> b.orange to null
        Belt.GREEN -> b.green to null
        Belt.BLUE -> b.blue to null
        Belt.BROWN -> b.brown to null
        Belt.RED -> b.red to null
        Belt.RED_BLACK -> b.red to b.black
        Belt.BLACK -> b.black to b.danStripe
    }
}

/** Muestra de cinta, 24×12dp por defecto (Figma: "Ribbon badge preview"; 14×8 en chips). */
@Composable
fun BeltSwatch(belt: Belt, modifier: Modifier = Modifier, width: Dp = 24.dp, height: Dp = 12.dp) {
    val shape = RoundedCornerShape(2.dp)
    val (main, second) = belt.colors()
    Row(
        modifier
            .size(width = width, height = height)
            .shadow(SkElevation.level1, shape)
            .clip(shape)
            .background(main)
            .then(if (belt == Belt.WHITE) Modifier.border(1.dp, MaterialTheme.colorScheme.outline, shape) else Modifier),
    ) {
        if (second != null) {
            Box(Modifier.weight(1f))
            Box(
                Modifier
                    .then(if (belt == Belt.BLACK) Modifier.width(3.dp) else Modifier.weight(1f))
                    .fillMaxHeight()
                    .background(second),
            )
            if (belt == Belt.BLACK) Box(Modifier.width(3.dp))
        }
    }
}

/**
 * M5. Insignia de cinta: rectángulo de 16dp del color de la cinta con una franja clara y el
 * grado en MAYÚSCULAS ("6° KUP"). El texto es oscuro sobre cintas claras (blanca, amarilla).
 */
@Composable
fun BeltBadge(belt: Belt, modifier: Modifier = Modifier) {
    val shape = MaterialTheme.shapes.extraSmall
    val main = belt.color()
    val text = if (main.luminance() > 0.5f) Color(0xFF191C20) else Color.White
    Box(
        modifier
            .height(16.dp)
            .clip(shape)
            .background(main)
            .then(if (belt == Belt.WHITE) Modifier.border(1.dp, MaterialTheme.colorScheme.outline, shape) else Modifier),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(Modifier.padding(start = 12.dp).width(6.dp).fillMaxHeight().background(Color.White.copy(alpha = 0.4f)))
        Text(
            stringResource(belt.gradeRes()).uppercase(),
            modifier = Modifier.padding(start = 16.dp, end = 8.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, lineHeight = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
            color = text,
        )
    }
}

/** Selector de cinta actual con menú desplegable (Figma: "Cinta actual"). */
@Composable
fun BeltSelectField(
    belt: Belt?,
    onBeltChange: (Belt) -> Unit,
    label: String,
    trailingIcon: Painter,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    options: List<Belt> = Belt.entries,
    required: Boolean = false,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        SkSelectField(
            value = belt?.let { stringResource(it.labelRes()) }.orEmpty(),
            onClick = { expanded = true },
            label = if (required) "$label *" else label,
            placeholder = placeholder,
            emphasized = true,
            leading = belt?.let { { BeltSwatch(it) } },
            trailing = { Icon(trailingIcon, contentDescription = null, tint = MaterialTheme.colorScheme.outline) },
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(stringResource(option.labelRes())) },
                    leadingIcon = { BeltSwatch(option) },
                    onClick = {
                        onBeltChange(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@SkPreviews
@Composable
private fun BeltSwatchPreview() = SportKidTheme {
    Row(Modifier.padding(16.dp)) {
        Belt.entries.forEach { BeltSwatch(it, Modifier.padding(4.dp)) }
    }
}
