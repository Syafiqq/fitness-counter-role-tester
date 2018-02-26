package com.github.syafiqq.fitnesscounter.role.tester.controller.tester.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.syafiqq.fitnesscounter.core.db.external.poko.Event
import com.github.syafiqq.fitnesscounter.role.tester.R
import com.github.syafiqq.fitnesscounter.role.tester.custom.android.text.CTextWatcher
import kotlinx.android.synthetic.main.fragment_tester_medical_check_up.*
import timber.log.Timber
import java.util.Locale
import kotlin.math.pow

class MedicalCheckUp: Fragment()
{
    private lateinit var listener: OnInteractionListener
    private val values = mutableMapOf<String, Any>("tbb" to 0f, "tbd" to 0f, "ratio" to 0f, "weight" to 0f, "bmi" to 0f)

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
                    this.values["tbb"] = s?.toFloat() ?: 0f
                    this.changeRatio(this.values["tbb"] as Float, this.values["tbd"] as Float)
                    this.changeBmi(this.values["tbb"] as Float, this.values["weight"] as Float)
                }
            }
        })
        this.h_edittext_tbd.addTextChangedListener(object: CTextWatcher
        {
            override fun afterTextChanged(s: Editable?)
            {
                with(this@MedicalCheckUp)
                {
                    this.values["tbd"] = s?.toFloat() ?: 0f
                    this.changeRatio(this.values["tbb"] as Float, this.values["tbd"] as Float)
                }
            }
        })
        this.h_edittext_weight.addTextChangedListener(object: CTextWatcher
        {
            override fun afterTextChanged(s: Editable?)
            {
                with(this@MedicalCheckUp)
                {
                    this.values["weight"] = s?.toFloat() ?: 0f
                    this.changeBmi(this.values["tbb"] as Float, this.values["weight"] as Float)
                }
            }
        })
        super.onViewCreated(view, state)
    }

    private fun changeRatio(tbb: Float, tbd: Float)
    {
        this.values["ratio"] = ((tbb - tbd) / tbd).takeIf { it.isFinite() } ?: 0f
        this.h_edittext_ratio.setText(String.format(Locale.getDefault(), "%.3f", this.values["ratio"] as Float))
    }

    private fun changeBmi(tbb: Float, weight: Float)
    {
        this.values["bmi"] = (weight / (tbb / 100).pow(2)).takeIf { it.isFinite() } ?: 0f
        this.h_edittext_bmi.setText(String.format(Locale.getDefault(), "%.3f", this.values["bmi"] as Float))
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

    fun Editable.toFloat(): Float
    {
        return this.toString().toFloatOrNull()?.takeIf { it.isFinite() } ?: 0f
    }
}
