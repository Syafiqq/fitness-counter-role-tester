package com.github.syafiqq.fitnesscounter.role.tester.controller.tester

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import com.github.syafiqq.fitnesscounter.role.tester.R
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import kotlinx.android.synthetic.main.tester_activity_dashboard.*

class Dashboard: AppCompatActivity()
{

    private lateinit var drawer: Drawer

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tester_activity_dashboard)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        this.drawer = DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withTranslucentStatusBar(false)
                .withActionBarDrawerToggle(true)
                .build()
    }

}
