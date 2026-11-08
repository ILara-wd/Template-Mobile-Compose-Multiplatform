package com.invex.sportkid.core.model

/**
 * Rol de un miembro del círculo familiar de un deportista (P17–P19).
 * [OWNER] gestiona miembros y puede dar de baja el expediente; [EDITOR] registra entrenamientos,
 * torneos y fotos; [VIEWER] solo consulta el calendario y las marcas (abuelos, entrenadores).
 */
enum class FamilyRole { OWNER, EDITOR, VIEWER }
