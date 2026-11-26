package com.invex.one.template.feature.home.component

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

/** Formats scores with a dot as thousands separator, as in Figma ("4.096", "1.420"). */
internal fun formatScore(value: Int): String = ScoreFormat.get()!!.format(value)

private val ScoreFormat = ThreadLocal.withInitial {
    DecimalFormat("#,##0", DecimalFormatSymbols().apply { groupingSeparator = '.' })
}
