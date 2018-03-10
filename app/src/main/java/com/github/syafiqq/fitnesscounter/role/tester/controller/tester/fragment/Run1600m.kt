package com.github.syafiqq.fitnesscounter.role.tester.controller.tester.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import com.danielbostwick.stopwatch.core.model.Stopwatch
import com.github.syafiqq.fitnesscounter.core.db.external.poko.Event
import com.github.syafiqq.fitnesscounter.core.helpers.tester.PresetHelper
import com.github.syafiqq.fitnesscounter.role.tester.R
import com.github.syafiqq.fitnesscounter.role.tester.controller.service.StopwatchService
import com.github.syafiqq.fitnesscounter.role.tester.ext.com.afollestad.materialdialogs.changeAndShow
import com.github.syafiqq.fitnesscounter.role.tester.ext.com.afollestad.materialdialogs.org.joda.time.toFormattedStopwatch
import com.google.firebase.database.DatabaseReference
import kotlinx.android.synthetic.main.fragment_tester_run1600m.*
import org.joda.time.DateTime
import org.joda.time.Duration
import timber.log.Timber
import java.io.Serializable
import java.util.Observer
import java.util.Timer
import java.util.TimerTask
import kotlin.properties.Delegates
import com.github.syafiqq.fitnesscounter.core.db.external.poko.tester.Run1600m as MRun1600m


class Run1600m: IdentifiableFragment()
{
    override val identifier: String
        get() = Run1600m.IDENTIFIER
    private lateinit var listener: OnInteractionListener
    private var timer: Timer? = null
    private var participant by Delegates.observable(-1) { _, _, new ->
        this.runsV.forEachIndexed { index, layout -> layout.visibility = if (index <= new) View.VISIBLE else View.GONE }
    }

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

    private val runs = arrayOf(
            IdRun1600m(),
            IdRun1600m(),
            IdRun1600m(),
            IdRun1600m(),
            IdRun1600m()
    )

    private lateinit var runsV: Array<LinearLayout>

    override fun onCreate(state: Bundle?)
    {
        Timber.d("onCreate [$state]")
        super.onCreate(state)
        super.setHasOptionsMenu(true)

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
        return inflater.inflate(R.layout.fragment_tester_run1600m, container, false)
    }

    override fun onViewCreated(view: View, state: Bundle?)
    {
        Timber.d("onViewCreated [$view, $state]")

        this.runsV = arrayOf(
                this.container_1,
                this.container_2,
                this.container_3,
                this.container_4,
                this.container_5
        )
        this.button_send.setOnClickListener { _ -> this.dialog.changeAndShow(this.dialogs["confirmation-send"].apply { this?.setContent("Apakah anda yakin mengirim nilai paserta ?") }!!) }

        this.button_start.setOnClickListener(this::startStopwatch)
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

        state.putSerializable(M_RESULT, this.runs)
        state.putInt(M_PARTICIPANT, this.participant)
        state.putString(M_STOPWATCH_STATE, this.stopwatchState.name)
        this.stopwatchService?.let { state.putSerializable(M_STOPWATCH, it.getStopwatch()) }
        super.onSaveInstanceState(state)
    }

    override fun onActivityCreated(state: Bundle?)
    {
        Timber.d("onActivityCreated [$state]")

        super.onActivityCreated(state)
        state?.let {
            it.getSerializable(M_RESULT)?.let { (it as Array<IdRun1600m>).forEachIndexed { index, data -> this.runs[index].set(data) } }
            it.getInt(M_PARTICIPANT).let { this.participant = it }
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
            if (this.runs.take(this.participant).any { it.id == null })
            {
                Toast.makeText(this.context!!, "Nomor Peserta Tidak Valid", Toast.LENGTH_LONG).show()
            }
            else
            {
                this.dialog.changeAndShow(this.dialogs["please-wait"]!!)
                PresetHelper.savesRun1600m(event.presetActive!!, this.runs.take(this.participant).associate { idRun -> idRun.id!! to idRun.run }, DatabaseReference.CompletionListener { error, _ ->
                    run {
                        with(this@Run1600m)
                        {
                            if (error == null)
                            {
                                Toast.makeText(this.context!!, "Pengiriman Berhasil", Toast.LENGTH_LONG).show()
                            }
                            else
                            {
                                Toast.makeText(this.context!!, "Error Pengiriman, Lebih baik simpan terlebih dahulu", Toast.LENGTH_LONG).show()
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

        //Id
        this.runs[0].id = this.edittext_participant_1.text.toString().toIntOrNull()
        this.runs[1].id = this.edittext_participant_2.text.toString().toIntOrNull()
        this.runs[2].id = this.edittext_participant_3.text.toString().toIntOrNull()
        this.runs[3].id = this.edittext_participant_4.text.toString().toIntOrNull()
        this.runs[4].id = this.edittext_participant_5.text.toString().toIntOrNull()
    }

    override fun loadChanges()
    {
        Timber.d("loadChanges")
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?)
    {
        Timber.d("onCreateOptionsMenu [$menu, $inflater]")

        inflater?.inflate(R.menu.menu_fragment_run, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean
    {
        Timber.d("onOptionsItemSelected [$item]")

        return when (item?.itemId)
        {
            R.id.action_add    ->
            {
                this.participant += if (this.participant < 5 && this.stopwatchState == StopwatchStatus.PREPARED) 1 else 0
                true
            }
            R.id.action_remove ->
            {
                this.participant -= if (this.participant > -1 && this.stopwatchState == StopwatchStatus.PREPARED) 1 else 0
                true
            }
            else               -> super.onOptionsItemSelected(item)
        }
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
            this.reset(this.getStopwatch())
            this.start(this.getStopwatch(), DateTime.now())
            val started = this@run.getStopwatch().startedAt.millis
            with(this@Run1600m)
            {
                this.runs.take(this.participant).forEach {
                    it.status = StopwatchStatus.STARTED
                    it.run.start = started
                }
                this.stopwatchState = StopwatchStatus.STARTED
            }
        }
    }

    private fun stopStopwatch(view: View? = null)
    {
        Timber.d("stopStopwatch [$view]")

        if (this.runs.take(this.participant).all { it.status == StopwatchStatus.STOPPED })
        {
            this.stopwatchService?.run {
                val now = DateTime.now()
                val elapsed = this.timeElapsed(this.getStopwatch(), now)
                this.pause(this.getStopwatch(), now)
                with(this@Run1600m)
                {
                    this.stopwatchState = StopwatchStatus.STOPPED
                    this.displayStopwatch(elapsed)
                }
            }
        }
    }

    private fun resetStopwatch(view: View? = null)
    {
        Timber.d("resetStopwatch [$view]")

        this.stopwatchService?.run {
            this.reset(this.getStopwatch())
            with(this@Run1600m)
            {
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
                this.button_start.visibility = View.VISIBLE
                this.group_finish.visibility = View.GONE
                this.unwatchStopwatch()
            }
            StopwatchStatus.STARTED  ->
            {
                this.button_start.visibility = View.GONE
                this.group_finish.visibility = View.GONE
                this.watchStopwatch()
            }
            StopwatchStatus.STOPPED  ->
            {
                this.button_start.visibility = View.GONE
                this.group_finish.visibility = View.VISIBLE
                this.unwatchStopwatch()
            }
        }

        this.button_counter_1.visibility = if (state == StopwatchStatus.STARTED) View.VISIBLE else View.GONE
        this.button_counter_2.visibility = if (state == StopwatchStatus.STARTED) View.VISIBLE else View.GONE
        this.button_counter_3.visibility = if (state == StopwatchStatus.STARTED) View.VISIBLE else View.GONE
        this.button_counter_4.visibility = if (state == StopwatchStatus.STARTED) View.VISIBLE else View.GONE
        this.button_counter_5.visibility = if (state == StopwatchStatus.STARTED) View.VISIBLE else View.GONE
        this.edittext_participant_1.visibility = if (state == StopwatchStatus.STARTED) View.GONE else View.VISIBLE
        this.edittext_participant_2.visibility = if (state == StopwatchStatus.STARTED) View.GONE else View.VISIBLE
        this.edittext_participant_3.visibility = if (state == StopwatchStatus.STARTED) View.GONE else View.VISIBLE
        this.edittext_participant_4.visibility = if (state == StopwatchStatus.STARTED) View.GONE else View.VISIBLE
        this.edittext_participant_5.visibility = if (state == StopwatchStatus.STARTED) View.GONE else View.VISIBLE
        this.textview_elapsed_1.visibility = if (state == StopwatchStatus.STOPPED) View.VISIBLE else View.GONE
        this.textview_elapsed_2.visibility = if (state == StopwatchStatus.STOPPED) View.VISIBLE else View.GONE
        this.textview_elapsed_3.visibility = if (state == StopwatchStatus.STOPPED) View.VISIBLE else View.GONE
        this.textview_elapsed_4.visibility = if (state == StopwatchStatus.STOPPED) View.VISIBLE else View.GONE
        this.textview_elapsed_5.visibility = if (state == StopwatchStatus.STOPPED) View.VISIBLE else View.GONE
    }

    private fun createTimerTask(): TimerTask
    {
        Timber.d("createTimerTask")

        return object: TimerTask()
        {
            override fun run()
            {
                with(this@Run1600m)
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

    enum class StopwatchStatus
    {
        PREPARED,
        STARTED,
        STOPPED
    }

    companion object
    {
        fun newInstance(): Fragment
        {
            return Run1600m()
        }

        const val IDENTIFIER = "Run1600m"
        const val M_RESULT = "m_result"
        const val M_PARTICIPANT = "m_participant"
        const val M_STOPWATCH_STATE = "m_stopwatch_state"
        const val M_STOPWATCH = "m_stopwatch"
        const val TIMER_DELAY: Long = 1000
    }
}

class IdRun1600m(var id: Int? = null, var run: MRun1600m = MRun1600m(), var current: Int = 0, var status: Run1600m.StopwatchStatus = Run1600m.StopwatchStatus.PREPARED):
        Serializable
{
    fun set(run: IdRun1600m)
    {
        this.id = run.id
        this.status = run.status
        this.current = run.current
        this.run.set(run.run)
    }
}
