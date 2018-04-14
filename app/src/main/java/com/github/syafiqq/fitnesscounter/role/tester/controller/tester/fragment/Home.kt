package com.github.syafiqq.fitnesscounter.role.tester.controller.tester.fragment


import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.syafiqq.fitnesscounter.core.db.external.poko.Event
import com.github.syafiqq.fitnesscounter.role.tester.R
import com.github.syafiqq.fitnesscounter.role.tester.controller.tester.Dashboard
import com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.Database
import kotlinx.android.synthetic.main.fragment_tester_home.*
import timber.log.Timber
import java.util.*
import com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.poko.tester.Illinois as PIllinois
import com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.poko.tester.MedicalCheckup as PMedicalCheckup
import com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.poko.tester.PushUp as PPushUp
import com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.poko.tester.Run1600m as PRun1600m
import com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.poko.tester.SitUp as PSitUp
import com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.poko.tester.ThrowingBall as PThrowingBall
import com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.poko.tester.VerticalJump as PVerticalJump

class Home : IdentifiableFragment() {
    private lateinit var listener: Home.OnInteractionListener
    private val eventO = Observer { o, arg -> if (o is Event.Observable) this.displaySave(arg as Event?) }

    private val race = mapOf<String, MutableList<*>>(
            "illinois" to mutableListOf<PIllinois>(),
            "medical" to mutableListOf<PMedicalCheckup>(),
            "push" to mutableListOf<PPushUp>(),
            "run" to mutableListOf<PRun1600m>(),
            "sit" to mutableListOf<PSitUp>(),
            "throwing" to mutableListOf<PThrowingBall>(),
            "vertical" to mutableListOf<PVerticalJump>()
    )

    override val identifier: String
        get() = Home.IDENTIFIER

    override fun onCreate(bundle: Bundle?) {
        Timber.d("onCreate [$bundle]")
        this.listener.getOEvent().addObserver(eventO)
        super.onCreate(bundle)
    }

    override fun onDestroy() {
        Timber.d("onDestroy")
        this.listener.getOEvent().deleteObserver(eventO)
        super.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View?
    {
        Timber.d("onCreateView [$inflater, $container, $state]")
        return inflater.inflate(R.layout.fragment_tester_home, container, false)
    }

    override fun onViewCreated(view: View, state: Bundle?)
    {
        Timber.d("onViewCreated [$view, $state]")

        this.displaySave(this.listener.getOEvent().event)
        super.onViewCreated(view, state)
    }

    override fun onAttach(context: Context) {
        Timber.d("onAttach [$context]")

        super.onAttach(context)
        if (context is OnInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnInteractionListener")
        }
    }

    private fun displaySave(event: Event?) {
        Timber.d("displaySave [$event]")

        this.race.values.forEach(MutableList<*>::clear)
        if (event == null) {
            this.cardview_save_container.visibility = View.GONE
        } else {
            Dashboard.DoAsync({
                val db = this.listener.getDb()
                (this.race["illinois"] as MutableList<PIllinois>?)?.addAll(db.illinois().findByPreset(event.presetActive!!))
                (this.race["medical"] as MutableList<PMedicalCheckup>?)?.addAll(db.medical().findByPreset(event.presetActive!!))
                (this.race["push"] as MutableList<PPushUp>?)?.addAll(db.push().findByPreset(event.presetActive!!))
                (this.race["run"] as MutableList<PRun1600m>?)?.addAll(db.run().findByPreset(event.presetActive!!))
                (this.race["sit"] as MutableList<PSitUp>?)?.addAll(db.sit().findByPreset(event.presetActive!!))
                (this.race["throwing"] as MutableList<PThrowingBall>?)?.addAll(db.throwing().findByPreset(event.presetActive!!))
                (this.race["vertical"] as MutableList<PVerticalJump>?)?.addAll(db.vertical().findByPreset(event.presetActive!!))
            }, {
                if (this.race.values.sumBy(MutableList<*>::count) <= 0) {
                    this.cardview_save_container.visibility = View.GONE
                } else {
                    this.cardview_save_container.visibility = View.VISIBLE
                    if (this.race["illinois"]?.count() ?: 0 > 0) {
                        this.tablerow_illinois.visibility = View.VISIBLE
                        this.illinois_count.text = this.race["illinois"]?.count().toString()
                    } else {
                        this.tablerow_illinois.visibility = View.GONE
                    }

                    if (this.race["medical"]?.count() ?: 0 > 0) {
                        this.tablerow_medical.visibility = View.VISIBLE
                        this.medical_count.text = this.race["medical"]?.count().toString()
                    } else {
                        this.tablerow_medical.visibility = View.GONE
                    }

                    if (this.race["push"]?.count() ?: 0 > 0) {
                        this.tablerow_push.visibility = View.VISIBLE
                        this.push_count.text = this.race["push"]?.count().toString()
                    } else {
                        this.tablerow_push.visibility = View.GONE
                    }

                    if (this.race["run"]?.count() ?: 0 > 0) {
                        this.tablerow_run.visibility = View.VISIBLE
                        this.run_count.text = this.race["run"]?.count().toString()
                    } else {
                        this.tablerow_run.visibility = View.GONE
                    }

                    if (this.race["sit"]?.count() ?: 0 > 0) {
                        this.tablerow_sit.visibility = View.VISIBLE
                        this.sit_count.text = this.race["sit"]?.count().toString()
                    } else {
                        this.tablerow_sit.visibility = View.GONE
                    }

                    if (this.race["throwing"]?.count() ?: 0 > 0) {
                        this.tablerow_throwing.visibility = View.VISIBLE
                        this.throwing_count.text = this.race["throwing"]?.count().toString()
                    } else {
                        this.tablerow_throwing.visibility = View.GONE
                    }

                    if (this.race["vertical"]?.count() ?: 0 > 0) {
                        this.tablerow_vertical.visibility = View.VISIBLE
                        this.vertical_count.text = this.race["vertical"]?.count().toString()
                    } else {
                        this.tablerow_vertical.visibility = View.GONE
                    }
                }
            }).execute()
        }
    }

    interface OnInteractionListener {
        fun getOEvent(): Event.Observable
        fun getDb(): Database
    }

    companion object
    {
        fun newInstance(): Fragment
        {
            return Home()
        }

        const val IDENTIFIER = "Home"
    }

}
