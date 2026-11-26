package com.invex.one.template.game

import androidx.compose.runtime.Composable
import com.invex.one.template.CarGame
import com.invex.one.template.DodgeBoard
import com.invex.one.template.Figth
import com.invex.one.template.Galaxy
import com.invex.one.template.Laberinto
import com.invex.one.template.NeonBrickBreaker
import com.invex.one.template.Numbers
import com.invex.one.template.OrbiGuard
import com.invex.one.template.PeleasGame
import com.invex.one.template.Pong
import com.invex.one.template.Simon
import com.invex.one.template.Slash
import com.invex.one.template.SmashBoard
import com.invex.one.template.Snake
import com.invex.one.template.SpaceInvadersGame
import com.invex.one.template.TetrisCompose
import com.invex.one.template.WhackAmole

/**
 * Maps catalog ids (see `GameCatalog` in :feature:games, also used by home and records)
 * to the composable that runs each game.
 */
object GameRegistry {
    private val games: Map<String, @Composable () -> Unit> = mapOf(
        "2048" to { Numbers() },
        "whack" to { WhackAmole() },
        "maze" to { Laberinto() },
        "simon" to { Simon() },
        "snake" to { Snake() },
        "pong" to { Pong() },
        "invaders" to { SpaceInvadersGame() },
        "blocks" to { TetrisCompose() },
        "car" to { CarGame() },
        "dodge" to { DodgeBoard() },
        "slash" to { Slash() },
        "smash" to { SmashBoard() },
        "bricks" to { NeonBrickBreaker() },
        "fight" to { Figth() },
        "galaxy" to { Galaxy() },
        "orbiguard" to { OrbiGuard() },
        "peleas" to { PeleasGame() },
    )

    fun content(gameId: String): (@Composable () -> Unit)? = games[gameId]
}
