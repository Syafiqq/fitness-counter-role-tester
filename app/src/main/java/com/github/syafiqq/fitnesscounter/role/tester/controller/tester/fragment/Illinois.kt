package com.github.syafiqq.fitnesscounter.role.tester.controller.tester.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.github.syafiqq.fitnesscounter.core.db.external.poko.Event
import com.github.syafiqq.fitnesscounter.role.tester.R
import com.github.syafiqq.fitnesscounter.role.tester.controller.service.StopwatchService
import com.github.syafiqq.fitnesscounter.role.tester.ext.com.afollestad.materialdialogs.changeAndShow
import com.github.syafiqq.fitnesscounter.role.tester.ext.com.afollestad.materialdialogs.org.joda.time.toFormattedStopwatch
import kotlinx.android.synthetic.main.fragment_tester_illinois.*
import org.joda.time.DateTime
import timber.log.Timber
import java.util.Observer
import java.util.Timer
import java.util.TimerTask
import com.github.syafiqq.fitnesscounter.core.db.external.poko.tester.Illinois as MIllinois

class Illinois: IdentifiableFragment()
{
    override val identifier: String
        get() = Illinois.IDENTIFIER
    private lateinit var listener: OnInteractionListener
    private lateinit var timer: Timer

    private var updateText = createTimerTask()
    private var stopwatchService: StopwatchService? = null
    private val stopwatchO = Observer { o, arg ->
        if (o is StopwatchService.Observable)
        {
            Timber.d("Stopwatch Initialized")
            stopwatchService = arg as StopwatchService
        }
    }

    private val illinois = MIllinois()

    override fun onCreate(state: Bundle?)
    {
        Timber.d("onCreate [$state]")
        super.onCreate(state)

        with(this.listener.getOService())
        {
            this.addObserver(stopwatchO)
            stopwatchService = this.service
        }
    }

    override fun onDestroy()
    {
        Timber.d("onDestroy")
        this.listener.getOService().deleteObserver(stopwatchO)
        super.onDestroy()
    }

    override fun onResume()
    {
        Timber.d("onResume")

        super.onResume()
        timer = Timer()

        try
        {
            timer.scheduleAtFixedRate(updateText, 0, TIMER_DELAY)
        }
        catch (e: Exception)
        {
            updateText = createTimerTask()
            timer.scheduleAtFixedRate(updateText, 0, TIMER_DELAY)
            Timber.e(e)
        }
    }

    override fun onPause()
    {
        Timber.d("onPause")

        timer.cancel()
        super.onPause()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View?
    {
        Timber.d("onCreateView [$inflater, $container, $state]")
        return inflater.inflate(R.layout.fragment_tester_illinois, container, false)
    }

    override fun onViewCreated(view: View, state: Bundle?)
    {
        Timber.d("onViewCreated [$view, $state]")

        this.button_send.setOnClickListener { _ -> this.dialog.changeAndShow(this.dialogs["confirmation-send"].apply { this?.setContent("Apakah anda yakin mengirim nilai peserta ${this@Illinois.edittext_participant.text}") }!!) }

        this.button_start.setOnClickListener {
            Timber.d("button_start ${this.stopwatchService}")
            this.stopwatchService?.run { this.start(this.getStopwatch(), DateTime.now(), 0) }
        }
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
        fun getOService(): StopwatchService.Observable
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
        const val TIMER_DELAY: Long = 500
    }

    private fun createTimerTask(): TimerTask
    {
        Timber.d("createTimerTask")

        return object: TimerTask()
        {
            override fun run()
            {
                this@Illinois.activity?.runOnUiThread {
                    this@Illinois.stopwatchService?.let {
                        this@Illinois.textview_clock.text = it.timeElapsed(it.getStopwatch(), DateTime.now()).toFormattedStopwatch()
                    }
                }
            }
        }
    }
}
