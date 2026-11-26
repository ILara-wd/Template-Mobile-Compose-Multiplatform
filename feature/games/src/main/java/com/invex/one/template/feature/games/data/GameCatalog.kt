package com.invex.one.template.feature.games.data

import com.invex.one.template.feature.games.model.Game
import com.invex.one.template.feature.games.model.GameCategory

/**
 * Catalog of the mini games available in the app (one per composable in `app/.../Screen.kt`).
 *
 * The first seven come from Figma. The rest have no design yet: their names and descriptions
 * are placeholders and they render a monogram instead of artwork until icons exist.
 */
object GameCatalog {
    val games: List<Game> = listOf(
        Game("2048", "2048", "Une fichas y llega al 2048", GameCategory.Puzzle),
        Game("whack", "Whack-a-Mole", "Golpea topos a toda velocidad", GameCategory.Reflexes),
        Game("maze", "Laberinto", "Encuentra la salida sin perderte", GameCategory.Logic),
        Game("simon", "Simón Dice", "Repite la secuencia de luces", GameCategory.Memory),
        Game("snake", "Snake", "Come manzanas y crece más", GameCategory.Arcade),
        Game("pong", "Pong", "Duelo clásico de raquetas", GameCategory.Retro),
        Game("invaders", "Space Invaders", "Defiende la galaxia entera", GameCategory.Action),
        // TODO: names, descriptions and artwork pending design.
        Game("blocks", "Bloques", "Encaja las piezas que caen", GameCategory.Puzzle),
        Game("car", "Carreras", "Esquiva el tráfico a toda velocidad", GameCategory.Reflexes),
        Game("dodge", "Dodge Board", "Esquiva todo lo que cae", GameCategory.Reflexes),
        Game("slash", "Slash", "Corta todo lo que vuela", GameCategory.Reflexes),
        Game("smash", "Smash Board", "Rompe bloques sin parar", GameCategory.Arcade),
        Game("bricks", "Neon Brick Breaker", "Rompe ladrillos de neón", GameCategory.Retro),
        Game("fight", "Fight", "Combate en la arena", GameCategory.Action),
        Game("galaxy", "Galaxy", "Navega entre asteroides", GameCategory.Action),
        Game("orbiguard", "Orbi Guard", "Protege tu órbita", GameCategory.Action),
        Game("peleas", "Peleas", "Duelo de lucha uno contra uno", GameCategory.Action),
    )
}
