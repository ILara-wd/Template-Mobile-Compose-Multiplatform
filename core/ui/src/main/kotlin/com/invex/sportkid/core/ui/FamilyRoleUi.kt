package com.invex.sportkid.core.ui

import androidx.annotation.StringRes
import com.invex.sportkid.core.model.FamilyRole

/** Nombre visible del rol ("Propietaria", "Editor", "Lector / Entrenador"). */
@get:StringRes
val FamilyRole.labelRes: Int
    get() = when (this) {
        FamilyRole.OWNER -> R.string.family_role_owner
        FamilyRole.EDITOR -> R.string.family_role_editor
        FamilyRole.VIEWER -> R.string.family_role_viewer
    }
