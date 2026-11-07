package com.invex.sportkid.core.model

/** Estilos de natación. [MEDLEY] (combinado individual) requiere los otros cuatro. */
enum class SwimStyle { FREESTYLE, BACKSTROKE, BREASTSTROKE, BUTTERFLY, MEDLEY }

/** Estado de un estilo (README §5.2). */
enum class SwimStyleStatus { MASTERED, LEARNING, PENDING }
