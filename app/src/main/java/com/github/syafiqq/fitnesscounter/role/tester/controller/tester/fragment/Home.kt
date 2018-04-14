package com.github.syafiqq.fitnesscounter.role.tester.controller.tester.fragment


import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.github.syafiqq.fitnesscounter.core.db.external.poko.Event
import com.github.syafiqq.fitnesscounter.core.helpers.tester.PresetHelper
import com.github.syafiqq.fitnesscounter.role.tester.R
import com.github.syafiqq.fitnesscounter.role.tester.controller.tester.Dashboard
import com.github.syafiqq.fitnesscounter.role.tester.ext.com.afollestad.materialdialogs.changeAndShow
import com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.Database
import com.google.firebase.database.DatabaseReference
import kotlinx.android.synthetic.main.fragment_tester_home.*
import timber.log.Timber
import java.util.*
import com.github.syafiqq.fitnesscounter.core.db.external.poko.tester.Illinois as MIllinois
import com.github.syafiqq.fitnesscounter.core.db.external.poko.tester.MedicalCheckup as MMedicalCheckup
import com.github.syafiqq.fitnesscounter.core.db.external.poko.tester.PushUp as MPushUp
import com.github.syafiqq.fitnesscounter.core.db.external.poko.tester.Run1600m as MRun1600m
import com.github.syafiqq.fitnesscounter.core.db.external.poko.tester.SitUp as MSitUp
import com.github.syafiqq.fitnesscounter.core.db.external.poko.tester.ThrowingBall as MThrowingBall
import com.github.syafiqq.fitnesscounter.core.db.external.poko.tester.VerticalJump as MVerticalJump
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
        this.initialSendButton()
        super.onViewCreated(view, state)
    }

    private fun initialSendButton() {
        fun callback(event: Event, callback: () -> Unit = {}): DatabaseReference.CompletionListener {
            return DatabaseReference.CompletionListener { error, _ ->
                if (error == null) {
                    Toast.makeText(this.context!!, "Pengiriman Berhasil", Toast.LENGTH_LONG).show()
                    Dashboard.DoAsync({
                        callback()
                    }, {
                        this.displaySave(event)
                    }).execute()
                } else {
                    Toast.makeText(this.context!!, "Error Pengiriman, Silahkan Ulangi Kembali", Toast.LENGTH_LONG).show()
                }
                this.dialog.get()?.dismiss()
            }
        }

        this.illinois_send.setOnClickListener {
            this.listener.getOEvent().event?.let {
                val event = it
                val data = this.race["illinois"] as MutableList<PIllinois>?
                data?.let {
                    this.dialog.changeAndShow(this.dialogs["please-wait"]!!)
                    PresetHelper.Bulk.saveIllinois(event.presetActive!!, data.map { illinois -> Triple(illinois.queue, illinois.stamp!!, illinois.morphToFirebaseData()) }, callback(event, {
                        this.listener.getDb().illinois().delete(*data.toTypedArray())
                    }))
                }
            }
        }
        this.medical_send.setOnClickListener {
            this.listener.getOEvent().event?.let {
                val event = it
                val data = this.race["medical"] as MutableList<PMedicalCheckup>?
                data?.let {
                    this.dialog.changeAndShow(this.dialogs["please-wait"]!!)
                    PresetHelper.Bulk.saveMedicalCheckUp(event.presetActive!!, data.map { medical -> Triple(medical.queue, medical.stamp!!, medical.morphToFirebaseData()) }, callback(event, {
                        this.listener.getDb().medical().delete(*data.toTypedArray())
                    }))
                }
            }
        }
        this.push_send.setOnClickListener {
            this.listener.getOEvent().event?.let {
                val event = it
                val data = this.race["push"] as MutableList<PPushUp>?
                data?.let {
                    this.dialog.changeAndShow(this.dialogs["please-wait"]!!)
                    PresetHelper.Bulk.savePushUp(event.presetActive!!, data.map { push -> Triple(push.queue, push.stamp!!, push.morphToFirebaseData()) }, callback(event, {
                        this.listener.getDb().push().delete(*data.toTypedArray())
                    }))
                }
            }
        }
        this.run_send.setOnClickListener {
            this.listener.getOEvent().event?.let {
                val event = it
                val data = this.race["run"] as MutableList<PRun1600m>?
                data?.let {
                    this.dialog.changeAndShow(this.dialogs["please-wait"]!!)
                    PresetHelper.Bulk.saveRun1600m(event.presetActive!!, data.map { run -> Triple(run.queue, run.stamp!!, run.morphToFirebaseData()) }, callback(event, {
                        this.listener.getDb().run().delete(*data.toTypedArray())
                    }))
                }
            }
        }
        this.sit_send.setOnClickListener {
            this.listener.getOEvent().event?.let {
                val event = it
                val data = this.race["sit"] as MutableList<PSitUp>?
                data?.let {
                    this.dialog.changeAndShow(this.dialogs["please-wait"]!!)
                    PresetHelper.Bulk.saveSitUp(event.presetActive!!, data.map { sit -> Triple(sit.queue, sit.stamp!!, sit.morphToFirebaseData()) }, callback(event, {
                        this.listener.getDb().sit().delete(*data.toTypedArray())
                    }))
                }
            }
        }
        this.throwing_send.setOnClickListener {
            this.listener.getOEvent().event?.let {
                val event = it
                val data = this.race["throwing"] as MutableList<PThrowingBall>?
                data?.let {
                    this.dialog.changeAndShow(this.dialogs["please-wait"]!!)
                    PresetHelper.Bulk.saveThrowingBall(event.presetActive!!, data.map { throwing -> Triple(throwing.queue, throwing.stamp!!, throwing.morphToFirebaseData()) }, callback(event, {
                        this.listener.getDb().throwing().delete(*data.toTypedArray())
                    }))
                }
            }
        }
        this.vertical_send.setOnClickListener {
            this.listener.getOEvent().event?.let {
                val event = it
                val data = (this.race["vertical"] as MutableList<PVerticalJump>?)
                data?.let {
                    this.dialog.changeAndShow(this.dialogs["please-wait"]!!)
                    PresetHelper.Bulk.saveVerticalJump(event.presetActive!!, data.map { vertical -> Triple(vertical.queue, vertical.stamp!!, vertical.morphToFirebaseData()) }, callback(event, {
                        this.listener.getDb().vertical().delete(*data.toTypedArray())
                    }))
                }
            }
        }
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

private fun PIllinois.morphToFirebaseData(): MIllinois {
    return MIllinois(this.start, this.end, this.elapsed)
}

private fun PMedicalCheckup.morphToFirebaseData(): MMedicalCheckup {
    return MMedicalCheckup(this.tbb, this.tbd, this.ratio, this.weight, this.bmi, this.posture, this.gait, this.pulse, this.pressure, this.ictus, this.heart, this.frequency, this.retraction, this.rLocation, this.breath, this.bPipeline, this.vision, this.hearing, this.verbal, this.conclusion)
}

private fun PPushUp.morphToFirebaseData(): MPushUp {
    return MPushUp(this.start, this.counter)
}

private fun PRun1600m.morphToFirebaseData(): MRun1600m {
    return MRun1600m(this.start, this.lap1, this.lap2, this.lap3, this.end, this.elapsed)
}

private fun PSitUp.morphToFirebaseData(): MSitUp {
    return MSitUp(this.start, this.counter)
}

private fun PThrowingBall.morphToFirebaseData(): MThrowingBall {
    return MThrowingBall(this.start, this.counter)
}

private fun PVerticalJump.morphToFirebaseData(): MVerticalJump {
    return MVerticalJump(this.initial, this.try1, this.try2, this.try3, this.deviation)
}
