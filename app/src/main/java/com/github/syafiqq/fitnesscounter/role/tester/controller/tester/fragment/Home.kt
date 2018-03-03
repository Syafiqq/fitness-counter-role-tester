package com.github.syafiqq.fitnesscounter.role.tester.controller.tester.fragment


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.syafiqq.fitnesscounter.role.tester.R
import timber.log.Timber

class Home: IdentifiableFragment()
{
    override val identifier: String
        get() = Home.IDENTIFIER

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View?
    {
        Timber.d("onCreateView [$inflater, $container, $state]")
        return inflater.inflate(R.layout.fragment_tester_home, container, false)
    }

    override fun onViewCreated(view: View, state: Bundle?)
    {
        Timber.d("onViewCreated [$view, $state]")

        super.onViewCreated(view, state)
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
