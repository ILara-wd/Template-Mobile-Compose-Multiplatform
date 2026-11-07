package com.invex.one.template

import com.invex.sportkid.feature.athlete.AthleteProfile
import com.invex.sportkid.feature.athlete.AthleteSamples
import com.invex.sportkid.feature.tkd.TkdAthlete
import com.invex.sportkid.feature.tkd.TkdSamples

/**
 * Traduce el perfil del hijo (feature:athlete) al contexto de Taekwondo (feature:tkd).
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
