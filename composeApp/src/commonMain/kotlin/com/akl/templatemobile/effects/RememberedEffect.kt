package com.akl.templatemobile.effects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.remember

/**
 * `RememberEffect` is a side-effect API that executes the provided [effect] lambda when it enters
 * the composition and re-executes it whenever [key1] changes.
 *
 * Unlike [LaunchedEffect], `RememberEffect` does not create or launch a new coroutine scope
 * on each key change, making it a more efficient option for remembering the execution of side-effects,
 * if you don't to launch a coroutine task.
 */
@Composable
@NonRestartableComposable
fun RememberedEffect(
    key1: Any?,
    effect: () -> Unit,
) {
    remember(key1) { RememberedEffectImpl(effect = effect) }
}

/**
 * `RememberEffect` is a side-effect API that executes the provided [effect] lambda when it enters
 * the composition and re-executes it whenever any of [key1] and [key2] changes.
 *
 * Unlike [LaunchedEffect], `RememberEffect` does not create or launch a new coroutine scope
 * on each key change, making it a more efficient option for remembering the execution of side-effects,
 * if you don't to launch a coroutine task.
 */
@Composable
@NonRestartableComposable
fun RememberedEffect(
    key1: Any?,
    key2: Any?,
    effect: () -> Unit,
) {
    remember(key1, key2) { RememberedEffectImpl(effect = effect) }
}

/**
 * `RememberEffect` is a side-effect API that executes the provided [effect] lambda when it enters
 * the composition and re-executes it whenever any of [key1], [key2], and [key3] changes.
 *
 * Unlike [LaunchedEffect], `RememberEffect` does not create or launch a new coroutine scope
 * on each key change, making it a more efficient option for remembering the execution of side-effects,
 * if you don't to launch a coroutine task.
 */
@Composable
@NonRestartableComposable
fun RememberedEffect(
    key1: Any?,
    key2: Any?,
    key3: Any?,
    effect: () -> Unit,
) {
    remember(key1, key2, key3) { RememberedEffectImpl(effect = effect) }
}

/**
 * `RememberEffect` is a side-effect API that executes the provided [effect] lambda when it enters
 * the composition and re-executes it whenever any of [keys] changes.
 *
 * Unlike [LaunchedEffect], `RememberEffect` does not create or launch a new coroutine scope
 * on each key change, making it a more efficient option for remembering the execution of side-effects,
 * if you don't to launch a coroutine task.
 */
@Composable
@NonRestartableComposable
fun RememberedEffect(
    vararg keys: Any?,
    effect: () -> Unit,
) {
    remember(*keys) { RememberedEffectImpl(effect = effect) }
}

/**
 * Launches the provided [effect] lambda when it enters the composition.
 */
internal class RememberedEffectImpl(
    private val effect: () -> Unit,
) : RememberObserver {

    override fun onRemembered() {
        effect.invoke()
    }

    override fun onAbandoned() {
        // no-op
    }

    override fun onForgotten() {
        // no-op
    }
}
