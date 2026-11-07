package com.invex.sportkid.feature.tkd

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.invex.sportkid.core.designsystem.component.SkAvatar
import com.invex.sportkid.core.designsystem.component.SkPill
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.model.Sport
import com.invex.sportkid.core.ui.AthleteDot
import com.invex.sportkid.core.ui.ageOn
import com.invex.sportkid.core.ui.colors
import java.time.LocalDate

/**
 * Contexto del deportista en los formularios de TKD: avatar con el punto del deporte, nombre,
 * escuela y un elemento a la derecha (cinta o "Kup oficial").
 */
@Composable
internal fun TkdAthleteStrip(
    athlete: TkdAthlete,
    today: LocalDate,
    modifier: Modifier = Modifier,
    showAge: Boolean = false,
    trailing: @Composable () -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    val tkd = Sport.TAEKWONDO.colors().content
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        SkAvatar(
            photo = athlete.photoRes?.let { painterResource(it) },
            name = athlete.fullName,
            size = 44.dp,
            ringColor = athlete.color(),
        ) {
            AthleteDot(tkd, size = 14.dp, modifier = Modifier.align(Alignment.BottomEnd))
        }
        Spacer(Modifier.width(SportKidTheme.spacing.sm))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(athlete.fullName, style = MaterialTheme.typography.titleSmall, color = colors.onSurface, maxLines = 1)
                if (showAge) {
                    Spacer(Modifier.width(SportKidTheme.spacing.xs))
                    SkPill(
                        stringResource(R.string.tkd_age, athlete.birthDate.ageOn(today)),
                        containerColor = colors.surfaceContainerHigh,
                        contentColor = colors.onSurfaceVariant,
                        contentPadding = PaddingValues(horizontal = SportKidTheme.spacing.sm, vertical = 2.dp),
                    )
                }
            }
            Text(
                stringResource(R.string.tkd_sport_school, athlete.school),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(SportKidTheme.spacing.sm))
        trailing()
    }
}




