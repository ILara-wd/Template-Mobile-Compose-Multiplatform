package com.invex.one.template

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.invex.one.template.feature.games.hub.DodgeGameScreen
import com.invex.one.template.feature.games.hub.GalaxyDefenderGame
import com.invex.one.template.feature.games.hub.Game2048Screen
import com.invex.one.template.feature.games.hub.LandscapeFightingGame
import com.invex.one.template.feature.games.hub.MazeDragScreen
import com.invex.one.template.feature.games.hub.NeonBrickBreakerScreen
import com.invex.one.template.feature.games.hub.OrbitGuardScreen
import com.invex.one.template.feature.games.hub.PongScreen
import com.invex.one.template.feature.games.hub.RaceCarGame
import com.invex.one.template.feature.games.hub.SimonSaysScreen
import com.invex.one.template.feature.games.hub.SlashGameScreen
import com.invex.one.template.feature.games.hub.SmashGameScreen
import com.invex.one.template.feature.games.hub.SnakeScreen
import com.invex.one.template.feature.games.hub.SpaceInvadersScreen
import com.invex.one.template.feature.games.hub.TetrisGameScreen
import com.invex.one.template.feature.games.hub.WhackAMoleScreen

@Composable
fun CarGame() {
    RaceCarGame(
        modifier = Modifier.fillMaxSize(),
        //soundResMap = mapOf(
        //    //SmashSoundEvent.SMASH to R.raw.sfx_smash,
        //    //SmashSoundEvent.ARMOR_HIT to R.raw.sfx_armor,
        //    //SmashSoundEvent.COMBO_UP to R.raw.sfx_combo,
        //    //SmashSoundEvent.PENALTY to R.raw.sfx_penalty,
        //    //SmashSoundEvent.GAME_OVER to R.raw.sfx_gameover
        //),
        //initialLives = 3,
    )
}

@Composable
fun DodgeBoard() {
    DodgeGameScreen(
        modifier = Modifier.fillMaxSize(),
        //soundResMap = mapOf(
        //    //SmashSoundEvent.SMASH to R.raw.sfx_smash,
        //    //SmashSoundEvent.ARMOR_HIT to R.raw.sfx_armor,
        //    //SmashSoundEvent.COMBO_UP to R.raw.sfx_combo,
        //    //SmashSoundEvent.PENALTY to R.raw.sfx_penalty,
        //    //SmashSoundEvent.GAME_OVER to R.raw.sfx_gameover
        //),
        //initialLives = 3,
    )
}

@Composable
fun Figth() {
    LandscapeFightingGame(
        modifier = Modifier.fillMaxSize(),
        //soundResMap = mapOf(
        //    //SmashSoundEvent.SMASH to R.raw.sfx_smash,
        //    //SmashSoundEvent.ARMOR_HIT to R.raw.sfx_armor,
        //    //SmashSoundEvent.COMBO_UP to R.raw.sfx_combo,
        //    //SmashSoundEvent.PENALTY to R.raw.sfx_penalty,
        //    //SmashSoundEvent.GAME_OVER to R.raw.sfx_gameover
        //),
        //initialLives = 3,
    )
}

@Composable
fun Galaxy() {
    GalaxyDefenderGame(
        modifier = Modifier.fillMaxSize(),
        //soundResMap = mapOf(
        //    //SmashSoundEvent.SMASH to R.raw.sfx_smash,
        //    //SmashSoundEvent.ARMOR_HIT to R.raw.sfx_armor,
        //    //SmashSoundEvent.COMBO_UP to R.raw.sfx_combo,
        //    //SmashSoundEvent.PENALTY to R.raw.sfx_penalty,
        //    //SmashSoundEvent.GAME_OVER to R.raw.sfx_gameover
        //),
        //initialLives = 3,
    )
}

@Composable
fun Laberinto() {
    MazeDragScreen(
        modifier = Modifier.fillMaxSize(),
        //soundResMap = mapOf(
        //    //SmashSoundEvent.SMASH to R.raw.sfx_smash,
        //    //SmashSoundEvent.ARMOR_HIT to R.raw.sfx_armor,
        //    //SmashSoundEvent.COMBO_UP to R.raw.sfx_combo,
        //    //SmashSoundEvent.PENALTY to R.raw.sfx_penalty,
        //    //SmashSoundEvent.GAME_OVER to R.raw.sfx_gameover
        //),
        //initialLives = 3,
    )
}

@Composable
fun NeonBrickBreaker() {
    NeonBrickBreakerScreen(
        modifier = Modifier.fillMaxSize(),
        //soundResMap = mapOf(
        //    //SmashSoundEvent.SMASH to R.raw.sfx_smash,
        //    //SmashSoundEvent.ARMOR_HIT to R.raw.sfx_armor,
        //    //SmashSoundEvent.COMBO_UP to R.raw.sfx_combo,
        //    //SmashSoundEvent.PENALTY to R.raw.sfx_penalty,
        //    //SmashSoundEvent.GAME_OVER to R.raw.sfx_gameover
        //),
        //initialLives = 3,
    )
}

@Composable
fun Numbers() {
    Game2048Screen(
        modifier = Modifier.fillMaxSize(),
        //soundResMap = mapOf(
        //    //SmashSoundEvent.SMASH to R.raw.sfx_smash,
        //    //SmashSoundEvent.ARMOR_HIT to R.raw.sfx_armor,
        //    //SmashSoundEvent.COMBO_UP to R.raw.sfx_combo,
        //    //SmashSoundEvent.PENALTY to R.raw.sfx_penalty,
        //    //SmashSoundEvent.GAME_OVER to R.raw.sfx_gameover
        //),
        //initialLives = 3,
    )
}

@Composable
fun OrbiGuard() {
    OrbitGuardScreen(
        modifier = Modifier.fillMaxSize(),
        //soundResMap = mapOf(
        //    //SmashSoundEvent.SMASH to R.raw.sfx_smash,
        //    //SmashSoundEvent.ARMOR_HIT to R.raw.sfx_armor,
        //    //SmashSoundEvent.COMBO_UP to R.raw.sfx_combo,
        //    //SmashSoundEvent.PENALTY to R.raw.sfx_penalty,
        //    //SmashSoundEvent.GAME_OVER to R.raw.sfx_gameover
        //),
        //initialLives = 3,
    )
}

@Composable
fun PeleasGame() {
}

@Composable
fun Pong() {
    PongScreen(
        modifier = Modifier.fillMaxSize(),
        //soundResMap = mapOf(
        //    //SmashSoundEvent.SMASH to R.raw.sfx_smash,
        //    //SmashSoundEvent.ARMOR_HIT to R.raw.sfx_armor,
        //    //SmashSoundEvent.COMBO_UP to R.raw.sfx_combo,
        //    //SmashSoundEvent.PENALTY to R.raw.sfx_penalty,
        //    //SmashSoundEvent.GAME_OVER to R.raw.sfx_gameover
        //),
        //initialLives = 3,
    )
}

@Composable
fun Simon() {
    SimonSaysScreen(
        modifier = Modifier.fillMaxSize(),
        //soundResMap = mapOf(
        //    //SmashSoundEvent.SMASH to R.raw.sfx_smash,
        //    //SmashSoundEvent.ARMOR_HIT to R.raw.sfx_armor,
        //    //SmashSoundEvent.COMBO_UP to R.raw.sfx_combo,
        //    //SmashSoundEvent.PENALTY to R.raw.sfx_penalty,
        //    //SmashSoundEvent.GAME_OVER to R.raw.sfx_gameover
        //),
        //initialLives = 3,
    )
}

@Composable
fun Slash() {
    SlashGameScreen(
        modifier = Modifier.fillMaxSize(),
        //soundResMap = mapOf(
        //    //SmashSoundEvent.SMASH to R.raw.sfx_smash,
        //    //SmashSoundEvent.ARMOR_HIT to R.raw.sfx_armor,
        //    //SmashSoundEvent.COMBO_UP to R.raw.sfx_combo,
        //    //SmashSoundEvent.PENALTY to R.raw.sfx_penalty,
        //    //SmashSoundEvent.GAME_OVER to R.raw.sfx_gameover
        //),
        //initialLives = 3,
    )
}

@Composable
fun SmashBoard() {
    SmashGameScreen(
        modifier = Modifier.fillMaxSize(),
        //soundResMap = mapOf(
        //    //SmashSoundEvent.SMASH to R.raw.sfx_smash,
        //    //SmashSoundEvent.ARMOR_HIT to R.raw.sfx_armor,
        //    //SmashSoundEvent.COMBO_UP to R.raw.sfx_combo,
        //    //SmashSoundEvent.PENALTY to R.raw.sfx_penalty,
        //    //SmashSoundEvent.GAME_OVER to R.raw.sfx_gameover
        //),
        //initialLives = 3,
    )
}

@Composable
fun Snake() {
    SnakeScreen(
        modifier = Modifier.fillMaxSize(),
        //soundResMap = mapOf(
        //    //SmashSoundEvent.SMASH to R.raw.sfx_smash,
        //    //SmashSoundEvent.ARMOR_HIT to R.raw.sfx_armor,
        //    //SmashSoundEvent.COMBO_UP to R.raw.sfx_combo,
        //    //SmashSoundEvent.PENALTY to R.raw.sfx_penalty,
        //    //SmashSoundEvent.GAME_OVER to R.raw.sfx_gameover
        //),
        //initialLives = 3,
    )
}

@Composable
fun SpaceInvadersGame() {
    SpaceInvadersScreen(
        modifier = Modifier.fillMaxSize(),
        //soundResMap = mapOf(
        //    //SmashSoundEvent.SMASH to R.raw.sfx_smash,
        //    //SmashSoundEvent.ARMOR_HIT to R.raw.sfx_armor,
        //    //SmashSoundEvent.COMBO_UP to R.raw.sfx_combo,
        //    //SmashSoundEvent.PENALTY to R.raw.sfx_penalty,
        //    //SmashSoundEvent.GAME_OVER to R.raw.sfx_gameover
        //),
        //initialLives = 3,
    )
}

@Composable
fun TetrisCompose() {
    TetrisGameScreen(
        modifier = Modifier.fillMaxSize(),
        //soundResMap = mapOf(
        //    //SmashSoundEvent.SMASH to R.raw.sfx_smash,
        //    //SmashSoundEvent.ARMOR_HIT to R.raw.sfx_armor,
        //    //SmashSoundEvent.COMBO_UP to R.raw.sfx_combo,
        //    //SmashSoundEvent.PENALTY to R.raw.sfx_penalty,
        //    //SmashSoundEvent.GAME_OVER to R.raw.sfx_gameover
        //),
        //initialLives = 3,
    )
}

@Composable
fun WhackAmole() {
    WhackAMoleScreen(
        modifier = Modifier.fillMaxSize(),
        //soundResMap = mapOf(
        //    //SmashSoundEvent.SMASH to R.raw.sfx_smash,
        //    //SmashSoundEvent.ARMOR_HIT to R.raw.sfx_armor,
        //    //SmashSoundEvent.COMBO_UP to R.raw.sfx_combo,
        //    //SmashSoundEvent.PENALTY to R.raw.sfx_penalty,
        //    //SmashSoundEvent.GAME_OVER to R.raw.sfx_gameover
        //),
        //initialLives = 3,
    )
}
