package com.github.syafiqq.fitnesscounter.role.tester.ext.com.afollestad.materialdialogs.org.joda.time

import org.joda.time.Duration
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * This fitness-counter-role-tester project created by :
 * Name         : syafiq
 * Date / Time  : 03 March 2018, 10:02 PM.
 * Email        : id.muhammad.syafiq@gmail.com
 * Github       : Syafiqq
 */
fun Duration.toFormattedStopwatch(): String
{
    var time = this.millis
    val minutes = TimeUnit.MILLISECONDS.toMinutes(time)
    time -= TimeUnit.MINUTES.toMillis(minutes)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(time)
    time -= TimeUnit.SECONDS.toMillis(seconds)
    return String.format(Locale.getDefault(), "%02d:%02d.%03d", minutes, seconds, time)
}