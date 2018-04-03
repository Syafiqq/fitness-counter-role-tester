package com.github.syafiqq.fitnesscounter.role.tester.controller.tester.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.github.syafiqq.fitnesscounter.role.tester.R
import timber.log.Timber
import java.util.concurrent.atomic.AtomicReference

/**
 * This fitness-counter-role-tester project created by :
 * Name         : syafiq
 * Date / Time  : 03 March 2018, 3:49 PM.
 * Email        : id.muhammad.syafiq@gmail.com
 * Github       : Syafiqq
 */
abstract class IdentifiableFragment: Fragment()
{ // @formatter:off
    abstract val identifier: String
    protected var dialog = AtomicReference<MaterialDialog>(null)
    protected val dialogs = mutableMapOf<String, MaterialDialog>()
    protected open fun doSend(v: View? = null){}
    protected open fun doSave(v: View? = null) {}
    protected open fun saveChanges(){}
    protected open fun loadChanges(){}

    override fun onViewCreated(view: View, state: Bundle?)
    {
        Timber.d("onViewCreated [$view, $state]")
        this.dialogs.putAll(mapOf(
                "please-wait" to MaterialDialog.Builder(this.context!!)
                        .canceledOnTouchOutside(false)
                        .content(super.getResources().getString(R.string.label_please_wait))
                        .progress(true, 0)
                        .build(),
                "confirmation-send" to MaterialDialog.Builder(this.context!!)
                        .title("Konfirmasi")
                        .positiveText("Ya")
                        .negativeText("Tidak")
                        .onPositive { _, _ -> doSave(); doSend() }
                        .build()))
        super.onViewCreated(view, state)
    }
} // @formatter:on