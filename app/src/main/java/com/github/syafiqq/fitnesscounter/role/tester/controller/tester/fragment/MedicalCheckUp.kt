package com.github.syafiqq.fitnesscounter.role.tester.controller.tester.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.github.syafiqq.fitnesscounter.core.db.external.poko.Event
import com.github.syafiqq.fitnesscounter.core.db.external.poko.tester.MedicalCheckup
import com.github.syafiqq.fitnesscounter.core.helpers.tester.PresetHelper
import com.github.syafiqq.fitnesscounter.role.tester.R
import com.github.syafiqq.fitnesscounter.role.tester.custom.android.text.CTextWatcher
import com.google.firebase.database.DatabaseReference
import kotlinx.android.synthetic.main.fragment_tester_medical_check_up.*
import timber.log.Timber
import java.util.Locale
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.pow

class MedicalCheckUp: Fragment()
{
    private lateinit var listener: OnInteractionListener
    private val checkUp = MedicalCheckup()
    private var dialog = AtomicReference<MaterialDialog>(null)
    private val dialogs = mutableMapOf<String, MaterialDialog>()

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
                    this.checkUp.tbb = s?.toFloat()
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
                    this.checkUp.tbd = s?.toFloat()
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
                    this.checkUp.weight = s?.toFloat()
                    this.checkUp.bmi = this.calculateBmi(this.checkUp.tbb, this.checkUp.weight)
                }
            }
        })
        this.button_send.setOnClickListener { _ -> this.dialog.changeAndShow(this.dialogs["confirmation-send"].apply { this?.setContent("Apakah anda yakin mengirim nilai peserta ${this@MedicalCheckUp.h_edittext_participant.text}") }!!) }

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

    private fun calculateRatio(tbb: Float?, tbd: Float?): Float?
    {
        val ratio = if (tbb == null || tbd == null) null else ((tbb - tbd) / tbd).takeIf { it.isFinite() }
        this.h_edittext_ratio.setText(String.format(Locale.getDefault(), "%.3f", ratio ?: 0f))
        return ratio
    }

    private fun calculateBmi(tbb: Float?, weight: Float?): Float?
    {
        val bmi = if (tbb == null || weight == null) null else (weight / (tbb / 100).pow(2)).takeIf { it.isFinite() }
        this.h_edittext_bmi.setText(String.format(Locale.getDefault(), "%.3f", bmi ?: 0f))
        return bmi
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

    fun doSend(v: View? = null)
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
                PresetHelper.saveMedicalCheckUp(event.presetActive!!, h_edittext_participant.text.toString().toInt(), this.checkUp, DatabaseReference.CompletionListener { error, _ ->
                    with(this@MedicalCheckUp)
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
                })
            }
        }
    }

    private fun saveChanges()
    {
        Timber.d("saveChanges")

        // Anthropometric
        this.checkUp.tbb = this.h_edittext_tbb.text.toFloat()
        this.checkUp.tbd = this.h_edittext_tbd.text.toFloat()
        this.checkUp.ratio = this.calculateRatio(this.checkUp.tbb, this.checkUp.tbd)
        this.checkUp.weight = this.h_edittext_weight.text.toFloat()
        this.checkUp.bmi = this.calculateBmi(this.checkUp.tbb, this.checkUp.weight)
        // Posture and Gait
        this.checkUp.posture = if (this.radiogroup_postur.checkedRadioButtonId < 0) null else view?.findViewById<RadioButton>(this.radiogroup_postur.checkedRadioButtonId)?.text.toString()
        this.checkUp.gait = if (this.radiogroup_aa.checkedRadioButtonId < 0) null else view?.findViewById<RadioButton>(this.radiogroup_aa.checkedRadioButtonId)?.text.toString()
        // Cardiovascular
        this.checkUp.pulse = this.h_edittext_denyut.text.toFloat()
        this.checkUp.pressure = if (this.h_edittext_mm.text.toFloat() == null || this.h_edittext_hg.text.toFloat() == null) null else "${this.h_edittext_mm.text.toFloat()} / ${this.h_edittext_hg.text.toFloat()}"
        this.checkUp.ictus = if (this.radiogroup_ictus.checkedRadioButtonId < 0) null else view?.findViewById<RadioButton>(this.radiogroup_ictus.checkedRadioButtonId)?.text.toString()
        this.checkUp.heart = if (this.radiogroup_jantung.checkedRadioButtonId < 0) null else view?.findViewById<RadioButton>(this.radiogroup_jantung.checkedRadioButtonId)?.text.toString()
        // Respiratory
        this.checkUp.frequency = this.h_edittext_frekuensi.text.toFloat()
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
    }

    fun Editable.toFloat(): Float?
    {
        return this.toString().toFloatOrNull()?.takeIf { it.isFinite() }
    }

    fun AtomicReference<MaterialDialog>.changeAndShow(dialog: MaterialDialog)
    {
        Timber.d("changeAndShow [$dialog]")
        this.get()?.dismiss()
        this.set(dialog)
        this.get()?.show()
    }
}
