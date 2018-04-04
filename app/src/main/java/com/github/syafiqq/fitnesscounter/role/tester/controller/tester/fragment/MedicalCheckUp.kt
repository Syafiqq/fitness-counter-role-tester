package com.github.syafiqq.fitnesscounter.role.tester.controller.tester.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextUtils
import android.view.*
import android.widget.RadioButton
import android.widget.Toast
import com.github.syafiqq.fitnesscounter.core.db.external.poko.Event
import com.github.syafiqq.fitnesscounter.core.db.external.poko.tester.MedicalCheckup
import com.github.syafiqq.fitnesscounter.core.helpers.tester.PresetHelper
import com.github.syafiqq.fitnesscounter.role.tester.R
import com.github.syafiqq.fitnesscounter.role.tester.custom.android.text.CTextWatcher
import com.github.syafiqq.fitnesscounter.role.tester.ext.android.text.toReadableFloat
import com.github.syafiqq.fitnesscounter.role.tester.ext.com.afollestad.materialdialogs.changeAndShow
import com.google.firebase.database.DatabaseReference
import kotlinx.android.synthetic.main.fragment_tester_medical_check_up.*
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import timber.log.Timber
import java.util.*
import kotlin.math.pow

class MedicalCheckUp: IdentifiableFragment()
{
    override val identifier: String
        get() = MedicalCheckUp.IDENTIFIER
    private lateinit var listener: OnInteractionListener
    private val checkUp = MedicalCheckup()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View?
    {
        Timber.d("onCreateView [$inflater, $container, $state]")
        return inflater.inflate(R.layout.fragment_tester_medical_check_up, container, false)
    }

    override fun onViewCreated(view: View, state: Bundle?)
    {
        Timber.d("onViewCreated [$view, $state]")

        this.h_edittext_tbb.addTextChangedListener(object: CTextWatcher
        {
            override fun afterTextChanged(s: Editable?)
            {
                with(this@MedicalCheckUp)
                {
                    this.checkUp.tbb = s?.toReadableFloat()
                    this.checkUp.ratio = this.calculateRatio(this.checkUp.tbb, this.checkUp.tbd)
                    this.checkUp.bmi = this.calculateBmi(this.checkUp.tbb, this.checkUp.weight)
                }
            }
        })
        this.h_edittext_tbd.addTextChangedListener(object: CTextWatcher
        {
            override fun afterTextChanged(s: Editable?)
            {
                with(this@MedicalCheckUp)
                {
                    this.checkUp.tbd = s?.toReadableFloat()
                    this.checkUp.ratio = this.calculateRatio(this.checkUp.tbb, this.checkUp.tbd)
                }
            }
        })
        this.h_edittext_weight.addTextChangedListener(object: CTextWatcher
        {
            override fun afterTextChanged(s: Editable?)
            {
                with(this@MedicalCheckUp)
                {
                    this.checkUp.weight = s?.toReadableFloat()
                    this.checkUp.bmi = this.calculateBmi(this.checkUp.tbb, this.checkUp.weight)
                }
            }
        })
        this.button_send.setOnClickListener { _ -> this.dialog.changeAndShow(this.dialogs["confirmation-send"].apply { this?.setContent("Apakah anda yakin mengirim nilai peserta ${this@MedicalCheckUp.h_edittext_participant.text}") }!!) }
        this.h_edittext_participant.addTextChangedListener(object : CTextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.isNotEmpty() == true) {
                    this@MedicalCheckUp.group_entry.visibility = View.VISIBLE
                    this@MedicalCheckUp.button_send.isEnabled = true
                } else {
                    this@MedicalCheckUp.group_entry.visibility = View.GONE
                    this@MedicalCheckUp.button_send.isEnabled = false
                }
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


        state.putSerializable(M_RESULT, this.checkUp)
        state.putInt(M_PARTICIPANT, this.h_edittext_participant.text.toString().toIntOrNull() ?: 0)
        super.onSaveInstanceState(state)
    }

    override fun onActivityCreated(state: Bundle?)
    {
        Timber.d("onActivityCreated [$state]")

        super.onActivityCreated(state)
        state?.let {
            it.getSerializable(M_RESULT)?.let { this.checkUp.set(it as MedicalCheckup) }
            it.getInt(M_PARTICIPANT).let { this.h_edittext_participant.setText(if (it == 0) "" else it.toString()) }
        }

        this.loadChanges()
    }

    private fun calculateRatio(tbb: Float?, tbd: Float?): Float?
    {
        Timber.d("calculateRatio [$tbb, $tbd]")

        val ratio = if (tbb == null || tbd == null) null else ((tbb - tbd) / tbd).takeIf { it.isFinite() }
        this.h_edittext_ratio.setText(String.format(Locale.getDefault(), "%.3f", ratio ?: 0f))
        return ratio
    }

    private fun calculateBmi(tbb: Float?, weight: Float?): Float?
    {
        Timber.d("calculateBmi [$tbb, $weight]")

        val bmi = if (tbb == null || weight == null) null else (weight / (tbb / 100).pow(2)).takeIf { it.isFinite() }
        this.h_edittext_bmi.setText(String.format(Locale.getDefault(), "%.3f", bmi ?: 0f))
        return bmi
    }

    override fun doSend(v: View?)
    {
        Timber.d("doSend [$v]")

        this.saveChanges()
        val event = this.listener.getEvent()
        if (event.presetActive != null)
        {
            if (h_edittext_participant.text.toString().toIntOrNull() == null)
            {
                Toast.makeText(this.context!!, "Nomor Peserta Tidak Valid", Toast.LENGTH_LONG).show()
            }
            else
            {
                this.dialog.changeAndShow(this.dialogs["please-wait"]!!)
                PresetHelper.saveMedicalCheckUp(event.presetActive!!, DateTime.now(DateTimeZone.forID("Asia/Jakarta")).toString(DateTimeFormat.forPattern("yyyyMMdd")), h_edittext_participant.text.toString().toInt(), this.checkUp, DatabaseReference.CompletionListener { error, _ ->
                    with(this@MedicalCheckUp)
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
                })
            }
        }
    }

    override fun doSave(v: View?) {
        Timber.d("doSave [$v]")
        super.doSave(v)
    }

    override fun clearField(v: View?) {
        Timber.d("clearField [$v]")

        this.checkUp.set(MedicalCheckup.EMPTY_DATA)
        this.loadChanges()
        super.clearField(v)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        Timber.d("onCreateOptionsMenu [$menu, $inflater]")

        inflater?.inflate(R.menu.menu_fragment_medical, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        Timber.d("onOptionsItemSelected [$item]")

        return when (item?.itemId) {
            R.id.action_save -> {
                this@MedicalCheckUp.doSave()
                true
            }
            R.id.action_clear -> {
                this@MedicalCheckUp.clearField()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun saveChanges()
    {
        Timber.d("saveChanges")

        // Anthropometric
        this.checkUp.tbb = this.h_edittext_tbb.text.toReadableFloat()
        this.checkUp.tbd = this.h_edittext_tbd.text.toReadableFloat()
        this.checkUp.ratio = this.calculateRatio(this.checkUp.tbb, this.checkUp.tbd)
        this.checkUp.weight = this.h_edittext_weight.text.toReadableFloat()
        this.checkUp.bmi = this.calculateBmi(this.checkUp.tbb, this.checkUp.weight)
        // Posture and Gait
        this.checkUp.posture = if (this.radiogroup_postur.checkedRadioButtonId < 0) null else view?.findViewById<RadioButton>(this.radiogroup_postur.checkedRadioButtonId)?.text.toString()
        this.checkUp.gait = if (this.radiogroup_aa.checkedRadioButtonId < 0) null else view?.findViewById<RadioButton>(this.radiogroup_aa.checkedRadioButtonId)?.text.toString()
        // Cardiovascular
        this.checkUp.pulse = this.h_edittext_denyut.text.toReadableFloat()
        this.checkUp.pressure = if (this.h_edittext_mm.text.toReadableFloat() == null || this.h_edittext_hg.text.toReadableFloat() == null) null else "${this.h_edittext_mm.text.toReadableFloat()} / ${this.h_edittext_hg.text.toReadableFloat()}"
        this.checkUp.ictus = if (this.radiogroup_ictus.checkedRadioButtonId < 0) null else view?.findViewById<RadioButton>(this.radiogroup_ictus.checkedRadioButtonId)?.text.toString()
        this.checkUp.heart = if (this.radiogroup_jantung.checkedRadioButtonId < 0) null else view?.findViewById<RadioButton>(this.radiogroup_jantung.checkedRadioButtonId)?.text.toString()
        // Respiratory
        this.checkUp.frequency = this.h_edittext_frekuensi.text.toReadableFloat()
        this.checkUp.retraction = if (this.radiogroup_retraksi.checkedRadioButtonId < 0) null else view?.findViewById<RadioButton>(this.radiogroup_retraksi.checkedRadioButtonId)?.text.toString()
        this.checkUp.rLocation = if (TextUtils.isEmpty(this.h_edittext_lokasi_retraksi.text.toString())) null else this.h_edittext_lokasi_retraksi.text.toString()
        this.checkUp.breath = if (this.radiogroup_suara_napas.checkedRadioButtonId < 0) null else view?.findViewById<RadioButton>(this.radiogroup_suara_napas.checkedRadioButtonId)?.text.toString()
        this.checkUp.bPipeline = if (this.radiogroup_saluran_napas.checkedRadioButtonId < 0) null else view?.findViewById<RadioButton>(this.radiogroup_saluran_napas.checkedRadioButtonId)?.text.toString()
        // Verbal
        this.checkUp.vision = if (this.radiogroup_mata.checkedRadioButtonId < 0) null else view?.findViewById<RadioButton>(this.radiogroup_mata.checkedRadioButtonId)?.text.toString()
        this.checkUp.hearing = if (this.radiogroup_telinga.checkedRadioButtonId < 0) null else view?.findViewById<RadioButton>(this.radiogroup_telinga.checkedRadioButtonId)?.text.toString()
        this.checkUp.verbal = if (this.radiogroup_verbal.checkedRadioButtonId < 0) null else view?.findViewById<RadioButton>(this.radiogroup_verbal.checkedRadioButtonId)?.text.toString()
        // Conclusion
        this.checkUp.conclusion = this.radiogroup_kesimpulan.checkedRadioButtonId == this.radiobutton_kesimpulan_yes.id
    }

    override fun loadChanges()
    {
        Timber.d("loadChanges")

        // Anthropometric
        this.h_edittext_tbb.setText(if (this.checkUp.tbb == null) "" else String.format(Locale.getDefault(), "%.2f", this.checkUp.tbb))
        this.h_edittext_tbd.setText(if (this.checkUp.tbd == null) "" else String.format(Locale.getDefault(), "%.2f", this.checkUp.tbd))
        this.h_edittext_weight.setText(if (this.checkUp.weight == null) "" else String.format(Locale.getDefault(), "%.2f", this.checkUp.weight))
        // Posture and Gait
        when (this.checkUp.posture)
        {
            this.radiobutton_postur_normal.text    -> this.radiobutton_postur_normal.isSelected = true
            this.radiobutton_postur_kifosis.text   -> this.radiobutton_postur_kifosis.isSelected = true
            this.radiobutton_postur_skoliosis.text -> this.radiobutton_postur_skoliosis.isSelected = true
            this.radiobutton_postur_ordosis.text   -> this.radiobutton_postur_ordosis.isSelected = true
            else -> this.radiogroup_postur.clearCheck()
        }
        when (this.checkUp.gait)
        {
            this.radiobutton_aa_deformitas.text -> this.radiobutton_aa_deformitas.isSelected = true
            this.radiobutton_aa_kelainan.text   -> this.radiobutton_aa_kelainan.isSelected = true
            this.radiobutton_aa_kelemahan.text  -> this.radiobutton_aa_kelemahan.isSelected = true
            this.radiobutton_aa_normal.text     -> this.radiobutton_aa_normal.isSelected = true
            else -> this.radiogroup_aa.clearCheck()
        }
        // Cardiovascular
        this.h_edittext_denyut.setText(if (this.checkUp.pulse == null) "" else String.format(Locale.getDefault(), "%.2f", this.checkUp.pulse))
        if (this.checkUp.pressure == null)
        {
            this.h_edittext_mm.setText("")
            this.h_edittext_hg.setText("")
        }
        else
        {
            val mmHGSplitter = this.checkUp.pressure!!.split(" / ")
            val mm = mmHGSplitter[0].toFloatOrNull()
            val hg = mmHGSplitter[1].toFloatOrNull()
            this.h_edittext_mm.setText(if (mm == null) "" else String.format(Locale.getDefault(), "%.2f", mm))
            this.h_edittext_hg.setText(if (hg == null) "" else String.format(Locale.getDefault(), "%.2f", hg))
        }
        when (this.checkUp.ictus)
        {
            this.radiobutton_ictus_minus.text -> this.radiobutton_ictus_minus.isSelected = true
            this.radiobutton_ictus_plus.text  -> this.radiobutton_ictus_plus.isSelected = true
            else -> this.radiogroup_ictus.clearCheck()
        }
        when (this.checkUp.heart)
        {
            this.radiobutton_jantung_normal.text -> this.radiobutton_jantung_normal.isSelected = true
            this.radiobutton_jantung_tidak.text  -> this.radiobutton_jantung_tidak.isSelected = true
            else -> this.radiogroup_jantung.clearCheck()
        }
        // Respiratory
        this.h_edittext_frekuensi.setText(if (this.checkUp.frequency == null) "" else String.format(Locale.getDefault(), "%.2f", this.checkUp.frequency))
        when (this.checkUp.retraction)
        {
            this.radiobutton_retraksi_minus.text -> this.radiobutton_retraksi_minus.isSelected = true
            this.radiobutton_retraksi_plus.text  -> this.radiobutton_retraksi_plus.isSelected = true
            else -> this.radiogroup_retraksi.clearCheck()
        }
        this.h_edittext_lokasi_retraksi.setText(this.checkUp.rLocation ?: "")
        when (this.checkUp.breath)
        {
            this.radiobutton_suara_napas_abnormal.text -> this.radiobutton_suara_napas_abnormal.isSelected = true
            this.radiobutton_suara_napas_normal.text   -> this.radiobutton_suara_napas_normal.isSelected = true
            else -> this.radiogroup_suara_napas.clearCheck()
        }
        when (this.checkUp.bPipeline)
        {
            this.radiobutton_saluran_napas_normal.text    -> this.radiobutton_saluran_napas_normal.isSelected = true
            this.radiobutton_saluran_napas_obstruksi.text -> this.radiobutton_saluran_napas_obstruksi.isSelected = true
            else -> this.radiogroup_saluran_napas.clearCheck()
        }
        // Verbal
        when (this.checkUp.vision)
        {
            this.radiobutton_mata_julig.text  -> this.radiobutton_mata_julig.isSelected = true
            this.radiobutton_mata_normal.text -> this.radiobutton_mata_normal.isSelected = true
            this.radiobutton_mata_pms.text    -> this.radiobutton_mata_pms.isSelected = true
            else -> this.radiogroup_mata.clearCheck()
        }
        when (this.checkUp.hearing)
        {
            this.radiobutton_telinga_normal.text     -> this.radiobutton_telinga_normal.isSelected = true
            this.radiobutton_telinga_tuli.text       -> this.radiobutton_telinga_tuli.isSelected = true
            this.radiobutton_telinga_obstruktif.text -> this.radiobutton_telinga_obstruktif.isSelected = true
            else -> this.radiogroup_telinga.clearCheck()
        }
        when (this.checkUp.verbal)
        {
            this.radiobutton_verbal_latah.text  -> this.radiobutton_verbal_latah.isSelected = true
            this.radiobutton_verbal_normal.text -> this.radiobutton_verbal_normal.isSelected = true
            this.radiobutton_verbal_tuna.text   -> this.radiobutton_verbal_tuna.isSelected = true
            else -> this.radiogroup_verbal.clearCheck()
        }
        // Conclusion
        when (this.checkUp.conclusion)
        {
            false -> this.radiobutton_kesimpulan_no.isSelected = true
            true  -> this.radiobutton_kesimpulan_yes.isSelected = true
        }
    }

    interface OnInteractionListener
    {
        fun getEvent(): Event
    }

    companion object
    {
        fun newInstance(): Fragment
        {
            return MedicalCheckUp()
        }

        const val IDENTIFIER = "Medical Check"
        const val M_RESULT = "m_result"
        const val M_PARTICIPANT = "m_participant"
    }
}
