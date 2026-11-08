package com.invex.sportkid.feature.sharing

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.model.Belt
import com.invex.sportkid.core.model.FamilyRole
import com.invex.sportkid.core.model.Sport
import kotlinx.coroutines.delay
import java.time.LocalDate
import kotlin.random.Random

/** Deportista que se comparte o al que te unes. `app` lo arma desde el perfil del hijo. */
@Immutable
data class ShareAthlete(
    val id: String,
    val firstName: String,
    val fullName: String,
    val colorIndex: Int,
    val birthDate: LocalDate,
    val sports: List<Sport>,
    @DrawableRes val photoRes: Int? = null,
)

@Composable
@ReadOnlyComposable
internal fun ShareAthlete.color(): Color =
    SportKidTheme.sportColors.athlete.all.let { it[colorIndex.mod(it.size)] }

/**
 * Código de invitación de un solo uso (README §5.6). [value] son 8 caracteres sin guion;
 * se muestra como "K7QM-4PZX" y el QR lleva [qrPayload].
 */
@Immutable
data class InviteCode(val value: String, val expiresAtMillis: Long) {
    val display: String get() = format(value)

    /** Contenido del QR: modo alfanumérico (mayúsculas) para que quepa en un QR versión 2-H. */
    val qrPayload: String get() = "$QR_PREFIX$value"

    companion object {
        const val LENGTH = 8
        const val VALIDITY_HOURS = 24
        const val QR_PREFIX = "SPORTKID:"

        /** Sin 0/O ni 1/I/L para que se pueda dictar sin confusiones. */
        private const val ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"

        fun generate(nowMillis: Long = System.currentTimeMillis(), random: Random = Random.Default): InviteCode =
            InviteCode(
                value = (1..LENGTH).map { ALPHABET[random.nextInt(ALPHABET.length)] }.joinToString(""),
                expiresAtMillis = nowMillis + VALIDITY_HOURS * 3_600_000L,
            )

        /** Lo que escribe el usuario → solo letras y dígitos en mayúsculas, máximo [LENGTH]. */
        fun normalize(input: String): String =
            input.uppercase().filter { it in 'A'..'Z' || it in '0'..'9' }.take(LENGTH)

        /** "K7QM4PZX" → "K7QM-4PZX". */
        fun format(value: String): String = if (value.length > 4) value.take(4) + "-" + value.drop(4) else value
    }
}

/** Resultado de validar un código (README §5.6). */
enum class InviteStatus { VALID, EXPIRED, USED, INVALID }

/** Avance del hijo en un deporte para la vista previa: cinta (TKD) o estilos dominados (natación). */
@Immutable
data class SportSnapshot(
    val sport: Sport,
    val belt: Belt? = null,
    val stylesMastered: Int? = null,
    val stylesTotal: Int = 5,
)

/** Lo que ve quien se une antes de descargar el expediente (P18, vista previa). */
@Immutable
data class JoinPreview(
    val athlete: ShareAthlete,
    val tutorName: String,
    val tutorRelation: String,
    val sports: List<SportSnapshot>,
    val medals: Int,
    val events: Int,
    val role: FamilyRole,
)

/** Bloques que se descargan al unirse, en orden. */
enum class SyncSection { PROFILE, EVENTS, MEDIA, REMINDERS }

/** Avance de un bloque: [done] de [total] elementos. */
@Immutable
data class SyncStep(val section: SyncSection, val done: Int, val total: Int) {
    val isDone: Boolean get() = done >= total
    val isPending: Boolean get() = done == 0 && total > 0
}

/** Avance total (0–1) como promedio de los bloques. */
val List<SyncStep>.progress: Float
    get() = if (isEmpty()) 1f else sumOf { (it.done.coerceAtMost(it.total).toDouble() / it.total.coerceAtLeast(1)) }.toFloat() / size

/**
 * Datos de demostración (Figma "Compartir a Mateo", "Escanear código" y "Vista previa y
 * descarga"). TODO: reemplazar por el repositorio de invitaciones y la sincronización real.
 */
object SharingSamples {
    const val CODE = "K7QM4PZX"

    /** Código de la demo: expira en 23:59 h como en Figma. */
    fun code(nowMillis: Long = System.currentTimeMillis()): InviteCode =
        InviteCode(CODE, nowMillis + InviteCode.VALIDITY_HOURS * 3_600_000L - 60_000L)

    /** Solo [CODE] es válido; cualquier otro código completo se rechaza. */
    fun validate(code: String): InviteStatus = if (InviteCode.normalize(code) == CODE) InviteStatus.VALID else InviteStatus.INVALID

    fun preview(athlete: ShareAthlete, belt: Belt?, stylesMastered: Int?, medals: Int): JoinPreview = JoinPreview(
        athlete = athlete,
        tutorName = "Mariana Gómez",
        tutorRelation = "Mamá",
        sports = athlete.sports.map { sport ->
            when (sport) {
                Sport.TAEKWONDO -> SportSnapshot(sport, belt = belt)
                Sport.SWIMMING -> SportSnapshot(sport, stylesMastered = stylesMastered)
                Sport.ATHLETICS -> SportSnapshot(sport)
            }
        },
        medals = medals,
        events = 12,
        role = FamilyRole.EDITOR,
    )

    /** Estado de Figma: perfil y eventos listos, fotos en 8 de 12 y recordatorios en espera. */
    val initialSync: List<SyncStep> = listOf(
        SyncStep(SyncSection.PROFILE, 1, 1),
        SyncStep(SyncSection.EVENTS, 1, 1),
        SyncStep(SyncSection.MEDIA, 8, 12),
        SyncStep(SyncSection.REMINDERS, 0, 3),
    )
}

/** Simula la descarga a partir de [SharingSamples.initialSync]: un elemento cada [stepMillis]. */
@Composable
fun rememberSimulatedSync(stepMillis: Long = 900): List<SyncStep> {
    var steps by rememberSaveable { mutableStateOf(SharingSamples.initialSync.map { it.done }) }
    LaunchedEffect(Unit) {
        while (true) {
            val index = SharingSamples.initialSync.indices.firstOrNull { steps[it] < SharingSamples.initialSync[it].total } ?: break
            delay(stepMillis)
            steps = steps.toMutableList().also { it[index]++ }
        }
    }
    return SharingSamples.initialSync.mapIndexed { i, step -> step.copy(done = steps[i]) }
}
