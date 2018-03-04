package com.github.syafiqq.fitnesscounter.role.tester.controller.tester.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.danielbostwick.stopwatch.core.model.Stopwatch
import com.github.syafiqq.fitnesscounter.core.db.external.poko.Event
import com.github.syafiqq.fitnesscounter.role.tester.R
import com.github.syafiqq.fitnesscounter.role.tester.controller.service.StopwatchService
import com.github.syafiqq.fitnesscounter.role.tester.ext.com.afollestad.materialdialogs.changeAndShow
import com.github.syafiqq.fitnesscounter.role.tester.ext.com.afollestad.materialdialogs.org.joda.time.toFormattedStopwatch
import kotlinx.android.synthetic.main.fragment_tester_illinois.*
import org.joda.time.DateTime
import org.joda.time.Duration
import timber.log.Timber
import java.util.Observer
import java.util.Timer
import java.util.TimerTask
import kotlin.properties.Delegates
import com.github.syafiqq.fitnesscounter.core.db.external.poko.tester.Illinois as MIllinois

class Illinois: IdentifiableFragment()
{
    override val identifier: String
        get() = Illinois.IDENTIFIER
    private lateinit var listener: OnInteractionListener
    private var timer: Timer? = null

    private var updateText = createTimerTask()
    private var stopwatchService: StopwatchService? = null
    private val stopwatchO = Observer { o, arg ->
        if (o is StopwatchService.Observable)
        {
            Timber.d("Stopwatch Initialized")
            stopwatchService = arg as StopwatchService
        }
    }
    private var stopwatchState by Delegates.observable(StopwatchStatus.PREPARED) { _, _, new -> shiftUI(new) }

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
        this.watchStopwatch()
    }

    override fun onPause()
    {
        Timber.d("onPause")

        this.unwatchStopwatch()
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

        this.button_start.setOnClickListener(this::startStopwatch)
        this.button_stop.setOnClickListener(this::stopStopwatch)
        this.button_reset.setOnClickListener(this::resetStopwatch)
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
        state.putString(M_STOPWATCH_STATE, this.stopwatchState.name)
        this.stopwatchService?.let { state.putSerializable(M_STOPWATCH, it.getStopwatch()) }
        super.onSaveInstanceState(state)
    }

    override fun onActivityCreated(state: Bundle?)
    {
        Timber.d("onActivityCreated [$state]")

        super.onActivityCreated(state)
        state?.let {
            it.getSerializable(M_RESULT)?.let { this.illinois.set(it as MIllinois) }
            it.getInt(M_PARTICIPANT).let { this.edittext_participant.setText(if (it == 0) "" else it.toString()) }
            it.getString(M_STOPWATCH_STATE)?.let { this.stopwatchState = StopwatchStatus.valueOf(it) }
            if (this.stopwatchService != null) it.getSerializable(M_STOPWATCH)?.let { this.stopwatchService!!.getStopwatch().set(it as Stopwatch) }
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
        this.illinois.start?.let { if (this.stopwatchService != null) this.stopwatchService?.getStopwatch()?.startedAt = DateTime(it) }
        this.illinois.elapsed?.let { this.displayStopwatch(Duration.millis(if (stopwatchState == StopwatchStatus.PREPARED) 0L else it)) }
    }

    interface OnInteractionListener
    {
        fun getEvent(): Event
        fun getOService(): StopwatchService.Observable
    }

    private fun startStopwatch(view: View? = null)
    {
        Timber.d("startStopwatch [$view]")

        this.stopwatchService?.run {
            this.start(this.getStopwatch(), DateTime.now())
            with(this@Illinois)
            {
                this.illinois.start = this@run.getStopwatch().startedAt.millis
                this.stopwatchState = StopwatchStatus.STARTED
            }
        }
    }

    private fun stopStopwatch(view: View? = null)
    {
        Timber.d("stopStopwatch [$view]")

        this.stopwatchService?.run {
            this.pause(this.getStopwatch(), DateTime.now())
            with(this@Illinois)
            {
                this.illinois.end = this@run.getStopwatch().startedAt.millis
                this.illinois.elapsed = (this.illinois.end ?: 0L) - (this.illinois.start ?: 0L)
                this.stopwatchState = StopwatchStatus.STOPPED
                this.illinois.elapsed?.let { this.displayStopwatch(org.joda.time.Duration.millis(it)) }
            }
        }
    }

    private fun resetStopwatch(view: View? = null)
    {
        Timber.d("resetStopwatch [$view]")

        this.stopwatchService?.run {
            this.reset(this.getStopwatch())
            with(this@Illinois)
            {
                this.stopwatchState = StopwatchStatus.PREPARED
                this.displayStopwatch(org.joda.time.Duration.millis(0L))
            }
        }
    }

    private fun shiftUI(state: StopwatchStatus)
    {
        when (state)
        {
            StopwatchStatus.PREPARED ->
            {
                this.button_start.visibility = View.VISIBLE
                this.button_stop.visibility = View.GONE
                this.group_finish.visibility = View.GONE
                this.unwatchStopwatch()
            }
            StopwatchStatus.STARTED  ->
            {
                this.button_start.visibility = View.GONE
                this.button_stop.visibility = View.VISIBLE
                this.group_finish.visibility = View.GONE
                this.watchStopwatch()
            }
            StopwatchStatus.STOPPED  ->
            {
                this.button_start.visibility = View.GONE
                this.button_stop.visibility = View.GONE
                this.group_finish.visibility = View.VISIBLE
                this.unwatchStopwatch()
            }
        }
    }

    private fun createTimerTask(): TimerTask
    {
        Timber.d("createTimerTask")

        return object: TimerTask()
        {
            override fun run()
            {
                with(this@Illinois)
                {
                    this.activity?.runOnUiThread {
                        this.stopwatchService?.let {
                            this.displayStopwatch(it.timeElapsed(it.getStopwatch(), DateTime.now()))
                        }
                    }
                }
            }
        }
    }

    private fun watchStopwatch()
    {
        Timber.d("watchStopwatch")

        if (stopwatchState == StopwatchStatus.STARTED)
        {
            this.unwatchStopwatch()
            timer = Timer().apply {
                try
                {
                    this.scheduleAtFixedRate(updateText, 0, TIMER_DELAY)
                }
                catch (e: Exception)
                {
                    updateText = createTimerTask()
                    this.scheduleAtFixedRate(updateText, 0, TIMER_DELAY)
                    Timber.e(e)
                }
            }
        }
    }

    private fun unwatchStopwatch()
    {
        Timber.d("unwatchStopwatch")

        timer?.cancel()
    }

    private fun displayStopwatch(duration: Duration)
    {
        this.textview_clock.text = duration.toFormattedStopwatch()
    }

    private enum class StopwatchStatus
    {
        PREPARED,
        STARTED,
        STOPPED
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
        const val M_STOPWATCH_STATE = "m_stopwatch_state"
        const val M_STOPWATCH = "m_stopwatch"
        const val TIMER_DELAY: Long = 1000
    }
}
