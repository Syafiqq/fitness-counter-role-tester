package com.github.syafiqq.fitnesscounter.role.tester.controller

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.github.syafiqq.fitnesscounter.role.tester.R
import com.github.syafiqq.fitnesscounter.role.tester.controller.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_splash_screen.*
import timber.log.Timber

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class SplashScreen: AppCompatActivity()
{
    private val hideHandler = Handler()
    private val hideOperation = Runnable { hide() }
    private var mContentView: View? = null

    override fun onCreate(bundle: Bundle?)
    {
        Timber.d("onCreate [$bundle]")

        super.onCreate(bundle)
        super.setContentView(R.layout.activity_splash_screen)
        this.mContentView = this.fullscreen_content
    }

    override fun onPostCreate(bundle: Bundle?)
    {
        Timber.d("onPostCreate [$bundle]")

        super.onPostCreate(bundle)
        this.delayedHide(50)
    }

    override fun onPostResume()
    {
        Timber.d("onPostResume")

        super.onPostResume()
        FirebaseAuth.getInstance().signOut()

        this.dispatchOperation(FirebaseAuth.getInstance().currentUser)
    }

    private fun dispatchOperation(user: FirebaseUser?)
    {
        Timber.d("dispatchOperation [$user]")

        Handler(mainLooper).postDelayed({
            if (user != null)
                Timber.d("To Home")
            else
                this.redirectToLoginPage()
        }, 1000)
    }

    private fun redirectToLoginPage()
    {
        Timber.d("redirectToLoginPage")

        val intent = Intent(this, LoginActivity::class.java)
        super.startActivity(intent)
        super.finish()
    }

    override fun onDestroy()
    {
        Timber.d("onDestroy")

        super.onDestroy()
    }

    private fun hide()
    {
        Timber.d("hide")

        val actionBar = supportActionBar
        actionBar?.hide()
    }

    /**
     * Schedules a call to hide() in [300] milliseconds, canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delay: Int)
    {
        Timber.d("hideCompletely [$delay]")

        this.hideHandler.removeCallbacks(this.hideOperation)
        this.hideHandler.postDelayed(this.hideOperation, delay.toLong())
    }
}
