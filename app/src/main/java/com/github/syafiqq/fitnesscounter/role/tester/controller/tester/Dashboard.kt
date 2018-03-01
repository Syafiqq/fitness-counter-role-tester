package com.github.syafiqq.fitnesscounter.role.tester.controller.tester

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.github.syafiqq.fitnesscounter.core.custom.com.google.firebase.database.CChildEventListener
import com.github.syafiqq.fitnesscounter.core.custom.com.google.firebase.database.CValueEventListener
import com.github.syafiqq.fitnesscounter.core.db.external.DataMapper
import com.github.syafiqq.fitnesscounter.core.db.external.poko.Event
import com.github.syafiqq.fitnesscounter.core.db.external.poko.EventCategory
import com.github.syafiqq.fitnesscounter.role.tester.App
import com.github.syafiqq.fitnesscounter.role.tester.R
import com.github.syafiqq.fitnesscounter.role.tester.controller.service.StopwatchService
import com.github.syafiqq.fitnesscounter.role.tester.controller.tester.fragment.Home
import com.github.syafiqq.fitnesscounter.role.tester.controller.tester.fragment.Illinois
import com.github.syafiqq.fitnesscounter.role.tester.controller.tester.fragment.MedicalCheckUp
import com.github.syafiqq.fitnesscounter.role.tester.controller.tester.fragment.PushUp
import com.github.syafiqq.fitnesscounter.role.tester.controller.tester.fragment.Run1600m
import com.github.syafiqq.fitnesscounter.role.tester.controller.tester.fragment.SitUp
import com.github.syafiqq.fitnesscounter.role.tester.controller.tester.fragment.ThrowingBall
import com.github.syafiqq.fitnesscounter.role.tester.controller.tester.fragment.VerticalJump
import com.github.syafiqq.fitnesscounter.role.tester.model.Settings
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
import timber.log.Timber
import java.util.Observer


class Dashboard: AppCompatActivity(),
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

    private var eventCounter = -1
    private var events: MutableMap<Int, Event> = hashMapOf()
    private var activeEvent: Event? = null

    private var categoryCounter = -1
    private var categories: MutableMap<Int, EventCategory> = hashMapOf()
    private var activeCategory: EventCategory? = null

    private var user: FirebaseUser? = null

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
                .withOnAccountHeaderListener({ _, event, _ -> this.activateEvent(this.events[event.identifier.toInt()]!!); false })
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

        this.user = FirebaseAuth.getInstance().currentUser

        Timber.d("Current user [${user?.uid}]")
        Timber.d("Current Selected [${drawerHeader.activeProfile?.name}]")

        this.listRegisteredEvent()
        with(App.instance.stopwatchService)
        {
            this.addObserver(stopwatchO)
            stopwatchService = this.service
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
                            snapshot?.run { this@Dashboard.addNewEvent(this.getValue(Event::class.java)) }
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
                this.activateEvent(it)
            }
        }
    }

    private fun activateEvent(event: Event)
    {
        Timber.d("activateEvent [$event]")

        if (this.activeEvent != event)
        {
            this.drawer.deselect()
            this.drawer.removeAllItems()
            this.categories.clear()
            this.activateCategory(null)
            this.categoryCounter = -1
            arrayOf(
                    "Medical Check",
                    "Illinois",
                    "Vertical Jump",
                    "Throwing Ball",
                    "Push Up",
                    "Sit Up",
                    "Run 1600 m").forEach { addNewCategory(EventCategory(it)) }
            this.activeEvent = event
        }
    }

    private fun addNewCategory(category: EventCategory?)
    {
        Timber.d("addNewCategory [$category]")

        category?.let {
            this.categories[++categoryCounter] = it
            this.drawer.addItem(PrimaryDrawerItem().withName(it.category).withIdentifier(categoryCounter.toLong()))
        }
    }

    private fun activateCategory(category: EventCategory?)
    {
        Timber.d("activateCategory [$category]")
        if (category != this.activeCategory)
        {
            val fragment: Fragment = when (category?.category)
            {
                "Medical Check" -> MedicalCheckUp.newInstance()
                "Illinois"      -> Illinois.newInstance()
                "Vertical Jump" -> VerticalJump.newInstance()
                "Throwing Ball" -> ThrowingBall.newInstance()
                "Push Up"       -> PushUp.newInstance()
                "Sit Up"        -> SitUp.newInstance()
                "Run 1600 m"    -> Run1600m.newInstance()
                else            -> Home.newInstance()
            }

            if (category == null)
            {
                this.toolbar.setTitle(R.string.title_tester_activity_dashboard)
            }
            else
            {
                category.category?.let {
                    this.toolbar.title = it
                }
            }

            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment, fragment)
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

    override fun onSaveInstanceState(state: Bundle?)
    {
        Timber.d("onSaveInstanceState [$state]")

        var mState = state ?: Bundle()

        mState = drawerHeader.saveInstanceState(mState)
        mState = drawer.saveInstanceState(mState)
        super.onSaveInstanceState(mState)
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
            super.onBackPressed()
        }
    }

    override fun onDestroy()
    {
        Timber.d("onDestroy")

        App.instance.stopwatchService.deleteObserver(stopwatchO)
        super.onDestroy()
    }

    companion object

}
