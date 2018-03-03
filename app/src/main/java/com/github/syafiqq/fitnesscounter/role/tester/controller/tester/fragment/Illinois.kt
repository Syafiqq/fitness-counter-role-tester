package com.github.syafiqq.fitnesscounter.role.tester.controller.tester.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.github.syafiqq.fitnesscounter.core.db.external.poko.Event
import com.github.syafiqq.fitnesscounter.role.tester.R
import com.github.syafiqq.fitnesscounter.role.tester.ext.com.afollestad.materialdialogs.changeAndShow
import kotlinx.android.synthetic.main.fragment_tester_illinois.*
import timber.log.Timber
import com.github.syafiqq.fitnesscounter.core.db.external.poko.tester.Illinois as MIllinois

class Illinois: IdentifiableFragment()
{
    override val identifier: String
        get() = Illinois.IDENTIFIER
    private lateinit var listener: OnInteractionListener
    private val illinois = MIllinois()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View?
    {
        Timber.d("onCreateView [$inflater, $container, $state]")
        return inflater.inflate(R.layout.fragment_tester_illinois, container, false)
    }

    override fun onViewCreated(view: View, state: Bundle?)
    {
        Timber.d("onViewCreated [$view, $state]")

        this.button_send.setOnClickListener { _ -> this.dialog.changeAndShow(this.dialogs["confirmation-send"].apply { this?.setContent("Apakah anda yakin mengirim nilai peserta ${this@Illinois.edittext_participant.text}") }!!) }

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
                        .onPositive { _, _ -> doSend() }
                        .build()))
        super.onViewCreated(view, state)
    }

    override fun onAttach(context: Context)
    {
        Timber.d("onAttach [$context]")

        super.onAttach(context)
        if (context is OnInteractionListener)
        {
            listener = context
        }
        else
        {
            throw RuntimeException(context.toString() + " must implement OnInteractionListener")
        }
    }

    override fun onSaveInstanceState(state: Bundle)
    {
        Timber.d("onSaveInstanceState [$state]")

        state.putSerializable(M_RESULT, this.illinois)
        state.putInt(M_PARTICIPANT, this.edittext_participant.text.toString().toIntOrNull() ?: 0)
        super.onSaveInstanceState(state)
    }

    override fun onActivityCreated(state: Bundle?)
    {
        Timber.d("onActivityCreated [$state]")

        super.onActivityCreated(state)
        state?.let {
            it.getSerializable(M_RESULT)?.let { this.illinois.set(it as MIllinois) }
            it.getInt(M_PARTICIPANT).let { this.edittext_participant.setText(if (it == 0) "" else it.toString()) }
        }

        this.loadChanges()
    }

    override fun doSend(v: View?)
    {
        Timber.d("doSend [$v]")

        this.saveChanges()
        val event = this.listener.getEvent()
        if (event.presetActive != null)
        {
            if (this.edittext_participant.text.toString().toIntOrNull() == null)
            {
                Toast.makeText(this.context!!, "Nomor Peserta Tidak Valid", Toast.LENGTH_LONG).show()
            }
            else
            {
                this.dialog.changeAndShow(this.dialogs["please-wait"]!!)
            }
        }
    }

    override fun saveChanges()
    {
        Timber.d("saveChanges")
    }

    override fun loadChanges()
    {
        Timber.d("loadChanges")
    }

    interface OnInteractionListener
    {
        fun getEvent(): Event
    }

    companion object
    {
        fun newInstance(): Fragment
        {
            return Illinois()
        }

        const val IDENTIFIER = "Illinois"
        const val M_RESULT = "m_result"
        const val M_PARTICIPANT = "m_participant"
    }
}
