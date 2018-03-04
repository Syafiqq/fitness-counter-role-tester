package com.github.syafiqq.fitnesscounter.role.tester.ext.android.text

import android.text.Editable

/**
 * This fitness-counter-role-tester project created by :
 * Name         : syafiq
 * Date / Time  : 04 March 2018, 9:50 PM.
 * Email        : id.muhammad.syafiq@gmail.com
 * Github       : Syafiqq
 */


fun Editable.toReadableFloat(): Float?
{
    return this.toString().toFloatOrNull()?.takeIf { it.isFinite() }
}