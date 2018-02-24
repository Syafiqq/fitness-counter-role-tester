package com.github.syafiqq.fitnesscounter.role.tester.controller.tester

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.syafiqq.fitnesscounter.role.tester.R

/**
 * A placeholder fragment containing a simple view.
 */
class DashboardFragment: Fragment()
{

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.tester_fragment_dashboard, container, false)
    }
}
