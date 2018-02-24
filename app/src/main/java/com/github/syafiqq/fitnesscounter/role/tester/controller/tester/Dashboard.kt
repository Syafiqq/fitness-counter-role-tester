package com.github.syafiqq.fitnesscounter.role.tester.controller.tester

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.github.syafiqq.fitnesscounter.core.custom.com.google.firebase.database.CChildEventListener
import com.github.syafiqq.fitnesscounter.core.custom.com.google.firebase.database.CValueEventListener
import com.github.syafiqq.fitnesscounter.core.db.external.DataMapper
import com.github.syafiqq.fitnesscounter.core.db.external.poko.Event
import com.github.syafiqq.fitnesscounter.core.db.external.poko.EventCategory
import com.github.syafiqq.fitnesscounter.role.tester.R
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


class Dashboard: AppCompatActivity()
{
    private lateinit var drawer: Drawer
    private lateinit var drawerHeader: AccountHeader

    private var eventCounter = -1
    private var events: MutableMap<Int, Event> = hashMapOf()
    private var activeEvent: Event? = null

    private var categoryCounter = -1
    private var categories: MutableMap<Int, EventCategory> = hashMapOf()
    private var activeCategory: Event? = null

    private var user: FirebaseUser? = null

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
                .withOnAccountHeaderListener({ _, profile, _ -> this.activateEvent(this.events[profile.identifier.toInt()]!!); false })
                .build()

        this.drawer = DrawerBuilder()
                .withActivity(this)
                .withSavedInstance(state)
                .withToolbar(toolbar)
                .withAccountHeader(drawerHeader)
                .withTranslucentStatusBar(false)
                .withActionBarDrawerToggle(true)
                .withOnDrawerItemClickListener({ view, index, menu -> Timber.d("Catch Item [$view, $index, $menu"); false })
                .build()

        this.user = FirebaseAuth.getInstance().currentUser

        Timber.d("Current user [${user?.uid}]")
        Timber.d("Current Selected [${drawerHeader.activeProfile?.name}]")

        this.listRegisteredEvent()
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
            this.categoryCounter = -1
            arrayOf(
                    "Medical Check",
                    "Ulinois",
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

    companion object

}
