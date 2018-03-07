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
import com.github.syafiqq.fitnesscounter.core.helpers.tester.PresetHelper
import com.github.syafiqq.fitnesscounter.role.tester.R
import com.github.syafiqq.fitnesscounter.role.tester.controller.service.StopwatchService
import com.github.syafiqq.fitnesscounter.role.tester.ext.com.afollestad.materialdialogs.changeAndShow
import com.github.syafiqq.fitnesscounter.role.tester.ext.com.afollestad.materialdialogs.org.joda.time.toFormattedStopwatch
import com.google.firebase.database.DatabaseReference
import kotlinx.android.synthetic.main.fragment_tester_push_up.*
import org.joda.time.DateTime
import org.joda.time.Duration
import timber.log.Timber
import java.util.Observer
import java.util.Timer
import java.util.TimerTask
import kotlin.properties.Delegates
import com.github.syafiqq.fitnesscounter.core.db.external.poko.tester.PushUp as MPushUp

class PushUp: IdentifiableFragment()
{
    override val identifier: String
        get() = PushUp.IDENTIFIER
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
    private val pushUp = MPushUp()
    private val limit = 5000L

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
        return inflater.inflate(R.layout.fragment_tester_push_up, container, false)
    }

    override fun onViewCreated(view: View, state: Bundle?)
    {
        Timber.d("onViewCreated [$view, $state]")

        this.displayStopwatch(Duration.millis(0L))
        this.button_send.setOnClickListener { _ -> this.dialog.changeAndShow(this.dialogs["confirmation-send"].apply { this?.setContent("Apakah anda yakin mengirim nilai peserta ${this@PushUp.edittext_participant.text}") }!!) }
        this.button_start.setOnClickListener(this::startStopwatch)
        this.button_counter.setOnClickListener(this::countStopwatch)
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

        state.putSerializable(M_RESULT, this.pushUp)
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
            it.getSerializable(M_RESULT)?.let { this.pushUp.set(it as MPushUp) }
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
                PresetHelper.savePushUp(event.presetActive!!, this.edittext_participant.text.toString().toInt(), this.pushUp, DatabaseReference.CompletionListener { error, _ ->
                    run {
                        with(this@PushUp)
                        {
                            if (error == null)
                            {
                                android.widget.Toast.makeText(this.context!!, "Pengiriman Berhasil", android.widget.Toast.LENGTH_LONG).show()
                            }
                            else
                            {
                                android.widget.Toast.makeText(this.context!!, "Error Pengiriman, Lebih baik simpan terlebih dahulu", android.widget.Toast.LENGTH_LONG).show()
                            }
                            this.dialog.get()?.dismiss()
                        }
                    }
                })
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
        this.pushUp.start?.let { if (this.stopwatchService != null) this.stopwatchService?.getStopwatch()?.startedAt = DateTime(it) }
        this.textview_counter.text = (this.pushUp.counter ?: 0).toString()
    }

    private fun startStopwatch(view: View? = null)
    {
        Timber.d("startStopwatch [$view]")

        this.stopwatchService?.run {
            this.reset(this.getStopwatch())
            this.start(this.getStopwatch(), DateTime.now())
            with(this@PushUp)
            {
                this.pushUp.start = this@run.getStopwatch().startedAt.millis
                this.stopwatchState = StopwatchStatus.STARTED
            }
        }
    }

    private fun countStopwatch(view: View? = null)
    {
        Timber.d("countStopwatch [$view]")

        this.stopwatchService?.run {
            with(this@PushUp)
            {
                if ((org.joda.time.DateTime.now().millis - this@run.getStopwatch().startedAt.millis <= this@PushUp.limit))
                {
                    this.pushUp.counter = (this.pushUp.counter ?: 0) + 1
                    this.textview_counter.text = this.pushUp.counter.toString()
                }
                else
                {
                    this.displayStopwatch(org.joda.time.Duration.millis(this.limit))
                    this.stopStopwatch()
                }
            }
        }
    }

    private fun stopStopwatch(view: View? = null)
    {
        Timber.d("stopStopwatch [$view]")

        this.stopwatchService?.run {
            this.pause(this.getStopwatch(), DateTime.now())
            with(this@PushUp)
            {
                this.stopwatchState = StopwatchStatus.STOPPED
            }
        }
    }

    private fun resetStopwatch(view: View? = null)
    {
        Timber.d("resetStopwatch [$view]")

        this.stopwatchService?.run {
            this.reset(this.getStopwatch())
            with(this@PushUp)
            {
                this.pushUp.counter = 0
                this.textview_counter.text = this.pushUp.counter.toString()
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
                this.button_counter.visibility = View.GONE
                this.group_finish.visibility = View.GONE
                this.unwatchStopwatch()
            }
            StopwatchStatus.STARTED  ->
            {
                this.button_start.visibility = View.GONE
                this.button_counter.visibility = View.VISIBLE
                this.group_finish.visibility = View.GONE
                this.watchStopwatch()
            }
            StopwatchStatus.STOPPED  ->
            {
                this.button_start.visibility = View.GONE
                this.button_counter.visibility = View.GONE
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
                with(this@PushUp)
                {
                    this.activity?.runOnUiThread {
                        this.stopwatchService?.let {
                            val duration = it.timeElapsed(it.getStopwatch(), org.joda.time.DateTime.now())
                            if (duration.millis < this.limit)
                            {
                                this.displayStopwatch(duration)
                            }
                            else
                            {
                                this.stopStopwatch()
                                this.displayStopwatch(org.joda.time.Duration.millis(this.limit))
                            }
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
        this.textview_clock.text = Duration.millis(this.limit).minus(duration).toFormattedStopwatch()
    }

    private enum class StopwatchStatus
    {
        PREPARED,
        STARTED,
        STOPPED
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
            return PushUp()
        }

        const val IDENTIFIER = "Push Up"
        const val M_RESULT = "m_result"
        const val M_PARTICIPANT = "m_participant"
        const val M_STOPWATCH_STATE = "m_stopwatch_state"
        const val M_STOPWATCH = "m_stopwatch"
        const val TIMER_DELAY: Long = 1000
    }
}
