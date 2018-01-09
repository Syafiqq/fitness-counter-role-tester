package com.github.syafiqq.fitnesscounter.role.student

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
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
@ReportsCrashes(mailTo = "syafiq.rezpector@gmail.com",
        customReportContent = [(ReportField.APP_VERSION_CODE), (ReportField.APP_VERSION_NAME), (ReportField.ANDROID_VERSION), (ReportField.PHONE_MODEL), (ReportField.CUSTOM_DATA), (ReportField.STACK_TRACE), (ReportField.LOGCAT)],
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_toast_text)
class App: Application()
{
    override fun onCreate()
    {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            val channelId = getString(R.string.default_notification_channel_id)
            val channelName = getString(R.string.default_notification_channel_name)
            getSystemService(NotificationManager::class.java).apply {
                this?.createNotificationChannel(NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW))
            }
        }
    }

    override fun attachBaseContext(base: Context)
    {
        super.attachBaseContext(base)
        this.initializeTimber()
        if (!BuildConfig.DEBUG) ACRA.init(this)
    }

    private fun initializeTimber()
    {
        Timber.plant(
                if (BuildConfig.DEBUG)
                    Timber.DebugTree()
                else
                    Timber.DebugTree()
        )

        Timber.d("Timber initialized")
    }
}