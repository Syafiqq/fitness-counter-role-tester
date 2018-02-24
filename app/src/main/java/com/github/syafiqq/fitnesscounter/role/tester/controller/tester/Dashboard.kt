package com.github.syafiqq.fitnesscounter.role.tester.controller.tester

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.github.syafiqq.fitnesscounter.role.tester.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import kotlinx.android.synthetic.main.tester_activity_dashboard.*
import timber.log.Timber


class Dashboard: AppCompatActivity()
{
    private lateinit var drawer: Drawer
    private var user: FirebaseUser? = null

    override fun onCreate(state: Bundle?)
    {
        Timber.d("onCreate [$state]")

        super.onCreate(state)
        setContentView(R.layout.tester_activity_dashboard)
        setSupportActionBar(toolbar)

        val drawerHeader = AccountHeaderBuilder()
                .withSavedInstance(state)
                .withActivity(this)
                .withProfileImagesVisible(false)
                .withCompactStyle(true)
                .withOnAccountHeaderListener({ _, profile, isCurrent -> Timber.d("Catch Profile [$profile, $isCurrent]"); false })
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
    }
}
