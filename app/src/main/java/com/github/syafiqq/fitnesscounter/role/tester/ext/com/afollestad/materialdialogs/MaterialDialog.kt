package com.github.syafiqq.fitnesscounter.role.tester.ext.com.afollestad.materialdialogs

import com.afollestad.materialdialogs.MaterialDialog
import timber.log.Timber
import java.util.concurrent.atomic.AtomicReference

/**
 * This fitness-counter-role-tester project created by :
 * Name         : syafiq
 * Date / Time  : 03 March 2018, 8:30 PM.
 * Email        : id.muhammad.syafiq@gmail.com
 * Github       : Syafiqq
 */

fun AtomicReference<MaterialDialog>.changeAndShow(dialog: MaterialDialog)
{
    Timber.d("changeAndShow [$dialog]")
    this.get()?.dismiss()
    this.set(dialog)
    this.get()?.show()
}
