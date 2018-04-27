package com.github.syafiqq.fitnesscounter.role.tester

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.github.syafiqq.fitnesscounter.role.tester.controller.service.StopwatchService
import com.google.firebase.database.FirebaseDatabase
import net.danlew.android.joda.JodaTimeAndroid
import org.acra.ACRA
import org.acra.ReportField
import org.acra.ReportingInteractionMode
import org.acra.annotation.ReportsCrashes
import timber.log.Timber

/**
 * This fitness-counter-student project created by :
 * Name         : syafiq
 * Date / Time  : 05 November 2017, 11:40 AM.
 * Email        : id.muhammad.syafiq@gmail.com
 * Github       : Syafiqq
 */
@Suppress("unused")
@ReportsCrashes(mailTo = "syafiq.rezpector@gmail.com",
        customReportContent = [(ReportField.APP_VERSION_CODE), (ReportField.APP_VERSION_NAME), (ReportField.ANDROID_VERSION), (ReportField.PHONE_MODEL), (ReportField.CUSTOM_DATA), (ReportField.STACK_TRACE), (ReportField.LOGCAT)],
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_toast_text)
class App: Application()
{
    val stopwatchService = StopwatchService.Observable()

    override fun onCreate()
    {
        Timber.d("onCreate")

        super.onCreate()
        this.initializeTimber()
        JodaTimeAndroid.init(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            val channelId = getString(R.string.default_notification_channel_id)
            val channelName = getString(R.string.default_notification_channel_name)
            getSystemService(NotificationManager::class.java).run {
                this?.createNotificationChannel(NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW))
            }
        }
        App.instance = this
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        this.bindStopwatchService()
    }

    override fun attachBaseContext(base: Context)
    {
        Timber.d("attachBaseContext")

        super.attachBaseContext(base)
        if (!BuildConfig.DEBUG) ACRA.init(this)
    }

    private fun initializeTimber()
    {
        Timber.d("initializeTimber")

        Timber.plant(
                if (BuildConfig.DEBUG)
                    Timber.DebugTree()
                else
                    ReleaseTree()
        )

        Timber.d("Timber initialized")
    }

    private fun bindStopwatchService()
    {
        Timber.d("bindStopwatchService")

        super.bindService(Intent(this, StopwatchService::class.java), stopwatchServiceConnection, Context.BIND_AUTO_CREATE)
    }

    private val stopwatchServiceConnection = object: ServiceConnection
    {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?)
        {
            Timber.d("onServiceConnected [$name, $service]")

            stopwatchService.service = (service as StopwatchService.StopwatchServiceBinder).service
        }

        override fun onServiceDisconnected(name: ComponentName?)
        {
            Timber.d("onServiceConnected [$name]")

            stopwatchService.service = null
        }
    }

    private class ReleaseTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO || priority == Log.WARN) {
                return
            }

            if (t != null) {
                return log(priority, tag, message, t)
            }
        }
    }

    companion object
    {
        lateinit var instance: App
    }
}