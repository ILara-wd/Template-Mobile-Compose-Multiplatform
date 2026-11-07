package com.invex.one.template

import com.invex.sportkid.core.model.Sport
import com.invex.sportkid.feature.athletics.AthleticsAthlete
import com.invex.sportkid.feature.athletics.AthleticsSamples
import com.invex.sportkid.feature.athlete.AthleteProfile
import com.invex.sportkid.feature.athlete.AthleteSamples
import com.invex.sportkid.feature.swimming.SwimAthlete
import com.invex.sportkid.feature.swimming.SwimSamples
import com.invex.sportkid.feature.tkd.TkdAthlete
import com.invex.sportkid.feature.tkd.TkdSamples

/**
 * Traduce el perfil del hijo (feature:athlete) al contexto de cada deporte (feature:tkd, feature:swimming, feature:athletics).
 * Vive en `app` porque los features no dependen entre sí. TODO: un modelo `Athlete` en core:model.
 */
internal fun AthleteProfile.toTkdAthlete(): TkdAthlete? = tkd?.let { tkd ->
    TkdAthlete(
        id = id,
        firstName = firstName,
        fullName = fullName,
        colorIndex = colorIndex,
        birthDate = birthDate,
        sports = sports,
        belt = tkd.belt,
        beltSince = tkd.history.lastOrNull { it.belt == tkd.belt }?.date,
        school = TkdSamples.mateo.school,
        instructor = tkd.instructor,
        schoolAddress = TkdSamples.mateo.schoolAddress,
        photoRes = avatarRes,
    )
}

/** Deportista de TKD por id, o el primero que practica Taekwondo. */
internal fun tkdAthleteFor(id: String?): TkdAthlete? {
    val athletes = AthleteSamples.athletes()
    return (athletes.firstOrNull { it.id == id && it.tkd != null } ?: athletes.firstOrNull { it.tkd != null })?.toTkdAthlete()
}

/** Perfil del hijo → contexto de natación (feature:swimming). */
internal fun AthleteProfile.toSwimAthlete(): SwimAthlete? = takeIf { Sport.SWIMMING in sports }?.let {
    SwimAthlete(
        id = id,
        firstName = firstName,
        fullName = fullName,
        colorIndex = colorIndex,
        birthDate = birthDate,
        sports = sports,
        club = SwimSamples.mateo.club,
        category = SwimSamples.mateo.category,
        photoRes = avatarRes,
    )
}

/** Deportista de natación por id, o el primero que nada. */
internal fun swimAthleteFor(id: String?): SwimAthlete? {
    val athletes = AthleteSamples.athletes()
    return (athletes.firstOrNull { it.id == id && Sport.SWIMMING in it.sports } ?: athletes.firstOrNull { Sport.SWIMMING in it.sports })?.toSwimAthlete()
}

/** Perfil del hijo → contexto de atletismo (feature:athletics). */
internal fun AthleteProfile.toAthleticsAthlete(): AthleticsAthlete? = takeIf { Sport.ATHLETICS in sports }?.let {
    AthleticsAthlete(
        id = id,
        firstName = firstName,
        fullName = fullName,
        colorIndex = colorIndex,
        birthDate = birthDate,
        sports = sports,
        club = AthleticsSamples.sofia.club,
        category = AthleticsSamples.sofia.category,
        photoRes = avatarRes,
    )
}

/** Deportista de atletismo por id, o el primero que corre. */
internal fun athleticsAthleteFor(id: String?): AthleticsAthlete? {
    val athletes = AthleteSamples.athletes()
    return (athletes.firstOrNull { it.id == id && Sport.ATHLETICS in it.sports } ?: athletes.firstOrNull { Sport.ATHLETICS in it.sports })?.toAthleticsAthlete()
}
