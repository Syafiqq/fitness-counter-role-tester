package com.github.syafiqq.fitnesscounter.role.tester.controller.tester.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.view.*
import android.widget.Toast
import com.github.syafiqq.fitnesscounter.core.db.external.poko.Event
import com.github.syafiqq.fitnesscounter.core.helpers.tester.PresetHelper
import com.github.syafiqq.fitnesscounter.role.tester.R
import com.github.syafiqq.fitnesscounter.role.tester.custom.android.text.CTextWatcher
import com.github.syafiqq.fitnesscounter.role.tester.ext.android.text.toReadableFloat
import com.github.syafiqq.fitnesscounter.role.tester.ext.com.afollestad.materialdialogs.changeAndShow
import com.google.firebase.database.DatabaseReference
import kotlinx.android.synthetic.main.fragment_tester_vertical_jump.*
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import timber.log.Timber
import java.util.*
import com.github.syafiqq.fitnesscounter.core.db.external.poko.tester.VerticalJump as MVerticalJump

class VerticalJump: IdentifiableFragment()
{
    override val identifier: String
        get() = VerticalJump.IDENTIFIER
    private lateinit var listener: OnInteractionListener
    private val result = MVerticalJump()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View?
    {
        Timber.d("onCreateView [$inflater, $container, $state]")
        return inflater.inflate(R.layout.fragment_tester_vertical_jump, container, false)
    }

    override fun onViewCreated(view: View, state: Bundle?)
    {
        Timber.d("onViewCreated [$view, $state]")

        this.button_send.setOnClickListener { _ -> this.dialog.changeAndShow(this.dialogs["confirmation-send"].apply { this?.setContent("Apakah anda yakin mengirim nilai peserta ${this@VerticalJump.edittext_participant.text}") }!!) }

        val textChangeListener = object: CTextWatcher
        {
            override fun afterTextChanged(s: Editable?)
            {
                with(this@VerticalJump)
                {
                    this.result.deviation = this.calculateDeviation()
                }
            }
        }

        this.edittext_raihan_tegak.addTextChangedListener(textChangeListener)
        this.edittext_jump_1.addTextChangedListener(textChangeListener)
        this.edittext_jump_2.addTextChangedListener(textChangeListener)
        this.edittext_jump_3.addTextChangedListener(textChangeListener)
        this.edittext_participant.addTextChangedListener(object : CTextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.isNotEmpty() == true) {
                    this@VerticalJump.group_entry.visibility = View.VISIBLE
                    this@VerticalJump.button_send.isEnabled = true
                } else {
                    this@VerticalJump.group_entry.visibility = View.GONE
                    this@VerticalJump.button_send.isEnabled = false
                }
            }
        })

        super.onViewCreated(view, state)
    }

    private fun calculateDeviation(
            initial: Float? = this.edittext_raihan_tegak.text.toReadableFloat(),
            try1: Float? = this.edittext_jump_1.text.toReadableFloat(),
            try2: Float? = this.edittext_jump_2.text.toReadableFloat(),
            try3: Float? = this.edittext_jump_3.text.toReadableFloat()): Float?
    {
        Timber.d("calculateDeviation [$initial, $try1, $try2, $try3]")

        val result =
                if (initial == null) null
                else Math.max(
                        Math.max(
                                Math.max(
                                        (try1 ?: -initial) - initial,
                                        (try2 ?: -initial) - initial),
                                (try3 ?: -initial) - initial),
                        -initial)

        this.result.initial = initial
        this.result.try1 = try1
        this.result.try2 = try2
        this.result.try3 = try3
        this.result.deviation = result

        this.edittext_deviation.setText(if (result == null) "" else String.format(Locale.getDefault(), "%.2f", result))
        return result
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

        this.saveChanges()
        state.putSerializable(M_RESULT, this.result)
        state.putInt(M_PARTICIPANT, this.edittext_participant.text.toString().toIntOrNull() ?: 0)
        super.onSaveInstanceState(state)
    }

    override fun onActivityCreated(state: Bundle?)
    {
        Timber.d("onActivityCreated [$state]")

        super.onActivityCreated(state)
        state?.let {
            it.getSerializable(M_RESULT)?.let { this.result.set(it as MVerticalJump) }
            it.getInt(M_PARTICIPANT).let { this.edittext_participant.setText(if (it == 0) "" else it.toString()) }
        }

        this.loadChanges()
    }

    override fun doSave(v: View?) {
        Timber.d("doSave [$v]")
        super.doSave(v)
    }

    override fun clearField(v: View?) {
        Timber.d("clearField [$v]")

        this.result.set(MVerticalJump.EMPTY_DATA)

        Timber.d("Result : ${this.result}")
        this.edittext_raihan_tegak.setText("")
        this.edittext_jump_1.setText("")
        this.edittext_jump_2.setText("")
        this.edittext_jump_3.setText("")
        this.loadChanges()
        super.clearField(v)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        Timber.d("onCreateOptionsMenu [$menu, $inflater]")

        inflater?.inflate(R.menu.menu_fragment_vertical, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        Timber.d("onOptionsItemSelected [$item]")

        return when (item?.itemId) {
            R.id.action_save -> {
                this@VerticalJump.doSave()
                true
            }
            R.id.action_clear -> {
                this@VerticalJump.clearField()
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
                PresetHelper.saveVerticalJump(event.presetActive!!, DateTime.now(DateTimeZone.forID("Asia/Jakarta")).toString(DateTimeFormat.forPattern("yyyyMMdd")), this.edittext_participant.text.toString().toInt(), this.result, DatabaseReference.CompletionListener { error, _ ->
                    run {
                        with(this@VerticalJump)
                        {
                            if (error == null)
                            {
                                Toast.makeText(this.context!!, "Pengiriman Berhasil", Toast.LENGTH_LONG).show()
                                this.clearField()
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

        this.result.initial = this.edittext_raihan_tegak.text.toReadableFloat()
        this.result.try1 = this.edittext_jump_1.text.toReadableFloat()
        this.result.try2 = this.edittext_jump_2.text.toReadableFloat()
        this.result.try3 = this.edittext_jump_3.text.toReadableFloat()
        this.result.deviation = this.edittext_deviation.text.toReadableFloat()
    }

    override fun loadChanges()
    {
        Timber.d("loadChanges")

        fun displayValue(value: Float?): String
        {
            return if (value == null) "" else String.format(Locale.getDefault(), "%.2f", value)
        }

        this.edittext_raihan_tegak.setText(displayValue(this.result.initial))
        this.edittext_jump_1.setText(displayValue(this.result.try1))
        this.edittext_jump_2.setText(displayValue(this.result.try2))
        this.edittext_jump_3.setText(displayValue(this.result.try3))
        this.edittext_deviation.setText(displayValue(this.result.deviation))
    }

    interface OnInteractionListener
    {
        fun getEvent(): Event
    }

    companion object
    {
        fun newInstance(): Fragment
        {
            return VerticalJump()
        }

        const val IDENTIFIER = "VerticalJump"
        const val M_RESULT = "m_result"
        const val M_PARTICIPANT = "m_participant"
    }
}
