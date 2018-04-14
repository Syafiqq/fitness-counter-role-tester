package com.github.syafiqq.fitnesscounter.role.tester.controller.tester

import android.arch.persistence.room.Room
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.github.syafiqq.fitnesscounter.core.custom.com.google.firebase.database.CChildEventListener
import com.github.syafiqq.fitnesscounter.core.custom.com.google.firebase.database.CValueEventListener
import com.github.syafiqq.fitnesscounter.core.db.external.DataMapper
import com.github.syafiqq.fitnesscounter.core.db.external.poko.Event
import com.github.syafiqq.fitnesscounter.core.db.external.poko.EventCategory
import com.github.syafiqq.fitnesscounter.role.tester.App
import com.github.syafiqq.fitnesscounter.role.tester.R
import com.github.syafiqq.fitnesscounter.role.tester.controller.service.StopwatchService
import com.github.syafiqq.fitnesscounter.role.tester.controller.tester.fragment.*
import com.github.syafiqq.fitnesscounter.role.tester.model.Settings
import com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.Database
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import kotlinx.android.synthetic.main.tester_activity_dashboard.*
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import timber.log.Timber
import java.util.*
import kotlin.properties.Delegates


class Dashboard: AppCompatActivity(),
        Home.OnInteractionListener,
        MedicalCheckUp.OnInteractionListener,
        Illinois.OnInteractionListener,
        VerticalJump.OnInteractionListener,
        ThrowingBall.OnInteractionListener,
        PushUp.OnInteractionListener,
        SitUp.OnInteractionListener,
        Run1600m.OnInteractionListener
{
    private lateinit var drawer: Drawer
    private lateinit var drawerHeader: AccountHeader
    private lateinit var db: Database
    private var isPressedTwice: Boolean = false

    private var eventCounter = -1
    private var events: MutableMap<Int, Event> = hashMapOf()
    private var vOEvent = Event.Observable()
    private var activeEvent: Event? by Delegates.observable(null as Event?) { _, old, new ->
        old?.id.let { FirebaseDatabase.getInstance().getReference(DataMapper.event(id = it)["events"]!!).keepSynced(false) }
        new?.id.let { FirebaseDatabase.getInstance().getReference(DataMapper.event(id = it)["events"]!!).keepSynced(true) }
        if (new != old) new?.let {
            this.activateEvent(it)
            vOEvent.set(it)
        }
    }

    private var categoryCounter = -1
    private var categories: MutableMap<Int, EventCategory> = hashMapOf()
    private var activeCategory: EventCategory? = null

    private var user: FirebaseUser? = null
    private var savedState: MutableMap<String, Any?> = hashMapOf()

    private var stopwatchService: StopwatchService? = null
    private val stopwatchO = Observer { o, arg ->
        if (o is StopwatchService.Observable)
        {
            Timber.d("Stopwatch Initialized")
            stopwatchService = arg as StopwatchService
        }
    }

    override fun onCreate(state: Bundle?)
    {
        Timber.d("onCreate [$state]")

        super.onCreate(state)
        setContentView(R.layout.tester_activity_dashboard)
        setSupportActionBar(toolbar)

        this.drawerHeader = AccountHeaderBuilder()
                .withSavedInstance(state)
                .withActivity(this)
                .withProfileImagesVisible(false)
                .withCompactStyle(true)
                .withHeaderBackground(R.drawable.blank_primary_dark)
                .withOnAccountHeaderListener({ _, event, _ -> this.activeEvent = this.events[event.identifier.toInt()]!!; false })
                .build()

        this.drawer = DrawerBuilder()
                .withActivity(this)
                .withSavedInstance(state)
                .withToolbar(toolbar)
                .withAccountHeader(drawerHeader)
                .withTranslucentStatusBar(false)
                .withActionBarDrawerToggle(true)
                .withOnDrawerItemClickListener({ _, _, category -> this.activateCategory(this.categories[category.identifier.toInt()]!!); false })
                .build()

        if (state == null)
        {
            supportFragmentManager
                    .beginTransaction()
                    .add(R.id.fragment, Home.newInstance(), Home.IDENTIFIER)
                    .commit()
        }

        with(App.instance.stopwatchService)
        {
            this.addObserver(stopwatchO)
            stopwatchService = this.service
        }

        this.user = FirebaseAuth.getInstance().currentUser
        this.listRegisteredEvent()

        DoAsync({
            this.db = Room.databaseBuilder(this, Database::class.java, "counter").build()
        }).execute()
    }

    override fun onDestroy()
    {
        Timber.d("onDestroy")

        App.instance.stopwatchService.deleteObserver(stopwatchO)
        super.onDestroy()
    }

    override fun onSaveInstanceState(state: Bundle?)
    {
        Timber.d("onSaveInstanceState [$state]")

        this.savedState.clear()
        var mState = state ?: Bundle()

        mState = drawerHeader.saveInstanceState(mState)
        mState = drawer.saveInstanceState(mState)
        this.activeEvent?.let { mState.putSerializable(M_ACTIVE_EVENT, it) }
        this.activeCategory?.let { mState.putSerializable(M_ACTIVE_CATEGORY, it) }
        if (supportFragmentManager.findFragmentById(R.id.fragment) is IdentifiableFragment) mState.putString(M_CURRENT_FRAGMENT, (supportFragmentManager.findFragmentById(R.id.fragment) as IdentifiableFragment).identifier)
        super.onSaveInstanceState(mState)
    }

    override fun onRestoreInstanceState(state: Bundle?)
    {
        Timber.d("onRestoreInstanceState [$state]")
        super.onRestoreInstanceState(state)

        state?.let {
            it.getSerializable(M_ACTIVE_EVENT)?.let { this.savedState[M_ACTIVE_EVENT] = it }
            it.getSerializable(M_ACTIVE_CATEGORY)?.let { this.savedState[M_ACTIVE_CATEGORY] = it }
            it.getString(M_CURRENT_FRAGMENT)?.let { this.savedState[M_CURRENT_FRAGMENT] = it }
        }
    }

    override fun onBackPressed()
    {
        Timber.d("onBackPressed")

        if (drawer.isDrawerOpen)
        {
            drawer.closeDrawer()
        }
        else
        {
            if (this.activeCategory?.category != Home.IDENTIFIER) {
                this.activateCategory(EventCategory(Home.IDENTIFIER))
            } else {
                if (isPressedTwice) {
                    super.onBackPressed()
                    return
                }

                this.isPressedTwice = true
                Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()

                Handler().postDelayed({ isPressedTwice = false }, 2000)
            }
        }
    }

    private fun listRegisteredEvent()
    {
        Timber.d("listRegisteredEvent")
        FirebaseDatabase.getInstance().getReference(DataMapper.event(user?.uid, Settings.GROUP_NAME)["users"]).addChildEventListener(object:
                CChildEventListener
        {
            override fun onChildAdded(snapshot: DataSnapshot?, previousChildName: String?)
            {
                snapshot?.run {
                    FirebaseDatabase.getInstance().getReference(DataMapper.event(null, null, this.key)["events"]).addListenerForSingleValueEvent(object:
                            CValueEventListener
                    {
                        override fun onDataChange(snapshot: DataSnapshot?)
                        {
                            snapshot?.run { this@Dashboard.addNewEvent(this.getValue(Event::class.java).apply { if (this != null) this.id = snapshot.key }) }
                        }
                    })
                }
            }
        })
    }

    private fun addNewEvent(event: Event?)
    {
        Timber.d("addNewEvent [$event]")

        event?.let {
            this.events[++eventCounter] = it
            this.drawerHeader.addProfile(ProfileDrawerItem().withName(it.event).withIdentifier(eventCounter.toLong()), eventCounter)

            if (this.activeEvent == null)
            {
                this.activeEvent = it
            }
            else if (this.savedState[M_ACTIVE_EVENT] != null && this.savedState[M_ACTIVE_EVENT] as Event == it)
            {
                this.savedState.remove(M_ACTIVE_EVENT)
                this.activeEvent = it
            }
        }
    }

    private fun activateEvent(event: Event)
    {
        Timber.d("activateEvent [$event]")
        this.drawer.deselect()
        this.drawer.removeAllItems()
        this.categories.clear()
        this.activateCategory(null)
        this.categoryCounter = -1
        arrayOf(
                MedicalCheckUp.IDENTIFIER,
                Illinois.IDENTIFIER,
                VerticalJump.IDENTIFIER,
                ThrowingBall.IDENTIFIER,
                PushUp.IDENTIFIER,
                SitUp.IDENTIFIER,
                Run1600m.IDENTIFIER).forEach { addNewCategory(EventCategory(it)) }
    }

    private fun addNewCategory(category: EventCategory?)
    {
        Timber.d("addNewCategory [$category]")

        category?.let {
            this.categories[++categoryCounter] = it
            this.drawer.addItem(PrimaryDrawerItem().withName(it.category).withIdentifier(categoryCounter.toLong()))

            if (this.savedState[M_ACTIVE_CATEGORY] != null && this.savedState[M_CURRENT_FRAGMENT] != null && this.savedState[M_ACTIVE_CATEGORY] == it)
            {
                val active = this.savedState.remove(M_ACTIVE_CATEGORY)
                if ((supportFragmentManager.findFragmentByTag(this.savedState[M_CURRENT_FRAGMENT] as String) as IdentifiableFragment).identifier != (active as EventCategory).category)
                {
                    this.activateCategory(it)
                }
                else
                {
                    supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.fragment, supportFragmentManager.findFragmentByTag(this.savedState[M_CURRENT_FRAGMENT] as String))
                            .commit()
                }
            }
        }
    }

    private fun activateCategory(category: EventCategory?)
    {
        fun checkAvailability(category: EventCategory): Fragment?
        {
            return this.supportFragmentManager.findFragmentByTag(category.category)
        }

        Timber.d("activateCategory [$category]")
        if (category != this.activeCategory)
        {
            val fragment: Fragment = when (category?.category)
            { // @formatter:off
                MedicalCheckUp.IDENTIFIER -> checkAvailability(category) ?: MedicalCheckUp.newInstance()
                Illinois.IDENTIFIER       -> checkAvailability(category) ?: Illinois.newInstance()
                VerticalJump.IDENTIFIER   -> checkAvailability(category) ?: VerticalJump.newInstance()
                ThrowingBall.IDENTIFIER   -> checkAvailability(category) ?: ThrowingBall.newInstance()
                PushUp.IDENTIFIER         -> checkAvailability(category) ?: PushUp.newInstance()
                SitUp.IDENTIFIER          -> checkAvailability(category) ?: SitUp.newInstance()
                Run1600m.IDENTIFIER       -> checkAvailability(category) ?: Run1600m.newInstance()
                Home.IDENTIFIER           -> checkAvailability(category) ?: Home.newInstance()
                else                      -> Home.newInstance()
            }// @formatter:on

            val transaction = supportFragmentManager.beginTransaction()
            if (category == null)
            {
                this.toolbar.setTitle(R.string.title_tester_activity_dashboard)
                transaction.replace(R.id.fragment, fragment)
            }
            else
            {
                transaction.replace(R.id.fragment, fragment, (fragment as IdentifiableFragment).identifier)
                category.category?.let {
                    this.toolbar.title = it
                }
            }
            transaction.commit()

            this.activeCategory = category
        }
    }

    override fun getEvent(): Event
    {
        if (this.activeEvent == null)
        {
            throw RuntimeException("Error")
        }
        else
        {
            return this.activeEvent!!
        }
    }

    override fun getOEvent(): Event.Observable {
        return this.vOEvent
    }

    override fun getStamp(): String {
        return DateTime.now(DateTimeZone.forID("Asia/Jakarta")).toString(DateTimeFormat.forPattern("yyyyMMdd"))
    }

    override fun getDb(): Database {
        return this.db
    }

    override fun getOService(): StopwatchService.Observable
    {
        return App.instance.stopwatchService
    }

    companion object
    {
        const val M_ACTIVE_EVENT = "m_active_event"
        const val M_ACTIVE_CATEGORY = "m_active_category"
        const val M_CURRENT_FRAGMENT = "m_current_fragment"
    }

    class DoAsync(val handler: () -> Unit, val next: (Void?) -> Unit = {}) : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            handler()
            return null
        }

        override fun onPostExecute(result: Void?) {
            next(result)
            super.onPostExecute(result)
        }
    }
}
