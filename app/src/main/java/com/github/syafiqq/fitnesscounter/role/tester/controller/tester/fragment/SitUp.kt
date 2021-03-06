package com.github.syafiqq.fitnesscounter.role.tester.controller.tester.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import android.widget.Toast
import com.danielbostwick.stopwatch.core.model.Stopwatch
import com.github.syafiqq.fitnesscounter.core.db.external.poko.Event
import com.github.syafiqq.fitnesscounter.core.helpers.tester.PresetHelper
import com.github.syafiqq.fitnesscounter.role.tester.R
import com.github.syafiqq.fitnesscounter.role.tester.controller.service.StopwatchService
import com.github.syafiqq.fitnesscounter.role.tester.controller.tester.Dashboard
import com.github.syafiqq.fitnesscounter.role.tester.custom.android.text.CTextWatcher
import com.github.syafiqq.fitnesscounter.role.tester.ext.com.afollestad.materialdialogs.changeAndShow
import com.github.syafiqq.fitnesscounter.role.tester.ext.com.afollestad.materialdialogs.org.joda.time.toFormattedStopwatch
import com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.Database
import com.google.firebase.database.DatabaseReference
import kotlinx.android.synthetic.main.fragment_tester_sit_up.*
import org.joda.time.DateTime
import org.joda.time.Duration
import timber.log.Timber
import java.util.*
import kotlin.properties.Delegates
import com.github.syafiqq.fitnesscounter.core.db.external.poko.tester.SitUp as MSitUp
import com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.dao.tester.SitUp as DSitUp
import com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.poko.tester.SitUp as PSitUp

class SitUp: IdentifiableFragment()
{
    override val identifier: String
        get() = SitUp.IDENTIFIER
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
    private val sitUp = MSitUp()
    private val dSitUp = PSitUp()

    private val limit = 60000L

    override fun onCreate(state: Bundle?)
    {
        Timber.d("onCreate [$state]")
        super.onCreate(state)

        with(this.listener.getOService())
        {
            this.addObserver(stopwatchO)
            stopwatchService = this.service
        }

        super.setHasOptionsMenu(true)
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
        this.button_send.setOnClickListener { _ -> this.dialog.changeAndShow(this.dialogs["confirmation-send"].apply { this?.setContent("Apakah anda yakin mengirim nilai peserta ${this@SitUp.edittext_participant.text}") }!!) }
        this.button_start.setOnClickListener(this::startStopwatch)
        this.button_counter.setOnClickListener(this::countStopwatch)
        this.edittext_participant.addTextChangedListener(object : CTextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                this@SitUp.button_start.isEnabled = s?.length!! > 0
            }
        })
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

        state.putSerializable(M_RESULT, this.sitUp)
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
            it.getSerializable(M_RESULT)?.let { this.sitUp.set(it as MSitUp) }
            it.getInt(M_PARTICIPANT).let { this.edittext_participant.setText(if (it == 0) "" else it.toString()) }
            it.getString(M_STOPWATCH_STATE)?.let { this.stopwatchState = StopwatchStatus.valueOf(it) }
            if (this.stopwatchService != null) it.getSerializable(M_STOPWATCH)?.let { this.stopwatchService!!.getStopwatch().set(it as Stopwatch) }
        }

        this.loadChanges()
    }


    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        Timber.d("onCreateOptionsMenu [$menu, $inflater]")
        menu?.clear()
        inflater?.inflate(R.menu.menu_fragment_push, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        Timber.d("onOptionsItemSelected [$item]")

        return when (item?.itemId) {
            R.id.action_reset -> {
                this.clearField()
                true
            }
            R.id.action_save -> {
                this.doSave()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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
                this.doSave()
                PresetHelper.saveSitUp(dSitUp.preset, dSitUp.stamp!!, dSitUp.queue, this.sitUp, DatabaseReference.CompletionListener { error, _ ->
                    run {
                        with(this@SitUp)
                        {
                            if (error == null)
                            {
                                android.widget.Toast.makeText(this.context!!, "Pengiriman Berhasil", android.widget.Toast.LENGTH_LONG).show()
                                this.clearField()
                                Dashboard.DoAsync({
                                    this.listener.getDb().sit().delete(dSitUp.preset, dSitUp.queue)
                                }, {}).execute()
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

    override fun doSave(v: View?) {
        Timber.d("doSave [$v]")
        if (stopwatchState == StopwatchStatus.STOPPED) {
            dSitUp.set(this.listener.getEvent().presetActive!!, this.listener.getStamp(), this.edittext_participant.text.toString().toInt(), this.sitUp)
            Dashboard.DoAsync({
                this.listener.getDb().sit().insert(dSitUp)
            }, {
                Toast.makeText(this.context, "Data Berhasil Disimpan", Toast.LENGTH_SHORT).show()
            }
            ).execute()
        }
        super.doSave(v)
    }

    override fun clearField(v: View?) {
        Timber.d("clearField [$v]")
        this.sitUp.set(MSitUp.EMPTY_DATA)
        this.textview_counter.text = "0"
        if (this.stopwatchState == StopwatchStatus.STARTED) {
            this.stopStopwatch()
        }
        if (this.stopwatchState == StopwatchStatus.STOPPED) {
            this.resetStopwatch()
        }
        super.clearField(v)
    }

    override fun saveChanges()
    {
        Timber.d("saveChanges")
    }

    override fun loadChanges()
    {
        Timber.d("loadChanges")
        this.sitUp.start?.let { if (this.stopwatchService != null) this.stopwatchService?.getStopwatch()?.startedAt = DateTime(it) }
        this.textview_counter.text = (this.sitUp.counter ?: 0).toString()
    }

    private fun startStopwatch(view: View? = null)
    {
        Timber.d("startStopwatch [$view]")

        this.stopwatchService?.run {
            this.reset(this.getStopwatch())
            this.start(this.getStopwatch(), DateTime.now())
            with(this@SitUp)
            {
                this.sitUp.start = this@run.getStopwatch().startedAt.millis
                this.stopwatchState = StopwatchStatus.STARTED
            }
        }
    }

    private fun countStopwatch(view: View? = null)
    {
        Timber.d("countStopwatch [$view]")

        this.stopwatchService?.run {
            with(this@SitUp)
            {
                if ((DateTime.now().millis - this@run.getStopwatch().startedAt.millis <= this@SitUp.limit))
                {
                    this.sitUp.counter = (this.sitUp.counter ?: 0) + 1
                    this.textview_counter.text = this.sitUp.counter.toString()
                }
                else
                {
                    this.displayStopwatch(Duration.millis(this.limit))
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
            with(this@SitUp)
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
            with(this@SitUp)
            {
                this.sitUp.counter = 0
                this.textview_counter.text = this.sitUp.counter.toString()
                this.stopwatchState = StopwatchStatus.PREPARED
                this.displayStopwatch(Duration.millis(0L))
            }
        }
    }

    private fun shiftUI(state: StopwatchStatus)
    {
        when (state)
        {
            StopwatchStatus.PREPARED ->
            {
                this.edittext_participant.isEnabled = true
                this.button_start.visibility = View.VISIBLE
                this.button_counter.visibility = View.GONE
                this.group_finish.visibility = View.GONE
                this.unwatchStopwatch()
            }
            StopwatchStatus.STARTED  ->
            {
                this.edittext_participant.isEnabled = false
                this.button_start.visibility = View.GONE
                this.button_counter.visibility = View.VISIBLE
                this.group_finish.visibility = View.GONE
                this.watchStopwatch()
            }
            StopwatchStatus.STOPPED  ->
            {
                this.edittext_participant.isEnabled = false
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
                with(this@SitUp)
                {
                    this.activity?.runOnUiThread {
                        this.stopwatchService?.let {
                            val duration = it.timeElapsed(it.getStopwatch(), DateTime.now())
                            if (duration.millis < this.limit)
                            {
                                this.displayStopwatch(duration)
                            }
                            else
                            {
                                this.stopStopwatch()
                                this.displayStopwatch(Duration.millis(this.limit))
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
        fun getStamp(): String
        fun getDb(): Database
        fun getOService(): StopwatchService.Observable
    }

    companion object
    {
        fun newInstance(): Fragment
        {
            return SitUp()
        }

        const val IDENTIFIER = "Sit Up"
        const val M_RESULT = "m_result"
        const val M_PARTICIPANT = "m_participant"
        const val M_STOPWATCH_STATE = "m_stopwatch_state"
        const val M_STOPWATCH = "m_stopwatch"
        const val TIMER_DELAY: Long = 1000
    }
}

private fun PSitUp.set(preset: String, stamp: String, queue: Int, sit: MSitUp) {
    this.queue = queue
    this.preset = preset
    this.stamp = stamp
    this.start = sit.start
    this.counter = sit.counter
}