package com.github.syafiqq.fitnesscounter.role.tester.controller.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import com.danielbostwick.stopwatch.core.model.Stopwatch
import com.danielbostwick.stopwatch.core.service.DefaultStopwatchService
import com.github.syafiqq.fitnesscounter.role.tester.R
import com.github.syafiqq.fitnesscounter.role.tester.controller.tester.Dashboard
import org.joda.time.DateTime
import org.joda.time.Duration
import timber.log.Timber
import java.util.concurrent.atomic.AtomicReferenceArray
import kotlin.properties.Delegates

class StopwatchService: Service()
{
    private val notificationID = 100
    private val stopwatches: AtomicReferenceArray<Stopwatch> = AtomicReferenceArray(4)
    private var notification: Notification? = null
    private val stopwatchService = DefaultStopwatchService()
    private val binder = StopwatchServiceBinder()

    override fun onCreate()
    {
        Timber.d("onCreate")

        super.onCreate()
        (0..3).forEach { stopwatches.set(it, create()) }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
    {
        Timber.d("onStartCommand [$intent, $flags, $startId]")

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder
    {
        Timber.d("onBind [$intent]")

        return binder
    }

    fun getStopwatch(index: Int = 0): Stopwatch
    {
        //Timber.d("getStopwatch [$index]")

        return stopwatches.get(index)
    }

    fun create(): Stopwatch
    {
        Timber.d("create")

        return stopwatchService.create()
    }

    fun start(stopwatch: Stopwatch, startedAt: DateTime, index: Int = 0): Stopwatch
    {
        Timber.d("start [$stopwatch, $startedAt, $index]")

        val stopwatch = stopwatchService.start(stopwatch, startedAt)
        stopwatches.set(index, stopwatch)

        notification = notification ?: createNotification()
        startForeground(notificationID, notification)
        return stopwatch
    }

    fun pause(stopwatch: Stopwatch, pausedAt: DateTime, index: Int = 0): Stopwatch
    {
        Timber.d("pause [$stopwatch, $pausedAt, $index]")

        val stopwatch = stopwatchService.pause(stopwatch, pausedAt)

        stopwatches.set(index, stopwatch)
        return stopwatch
    }

    fun reset(stopwatch: Stopwatch, index: Int = 0): Stopwatch
    {
        Timber.d("reset [$stopwatch, $index]")

        val stopwatch = stopwatchService.reset(stopwatch)

        stopwatches.set(index, stopwatch)
        stopForeground(true)
        return stopwatch
    }

    fun timeElapsed(stopwatch: Stopwatch, now: DateTime, index: Int = 0): Duration
    {
        //Timber.d("timeElapsed [$stopwatch, $now, $index]")

        return stopwatchService.timeElapsed(stopwatch, now)
    }

    private fun createNotification(): Notification
    {
        Timber.d("createNotification")

        val notificationIntent = Intent(this, Dashboard::class.java)
        val stackBuilder = TaskStackBuilder.create(this).apply {
            this.addNextIntent(notificationIntent)
        }
        val pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

        return NotificationCompat.Builder(this, "stopwatch")
                .setSmallIcon(R.drawable.notification_icon_background)
                .setContentTitle("Stopwatch")
                .setContentText("Aplikasi Sedang Berjalan")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(pendingIntent)
                .build()
    }

    /**
     * Class for clients to access.  Because we know this service always runs in the same process
     * as its clients, we don't need to deal with IPC.
     */
    inner class StopwatchServiceBinder: Binder()
    {
        internal val service: StopwatchService
            get() = this@StopwatchService
    }

    class Observable: java.util.Observable()
    {
        var service: StopwatchService? by Delegates.observable(null as StopwatchService?, { _, _, service ->
            run {
                setChanged()
                this.notifyObservers(service)
            }
        })
    }
}
