package com.github.syafiqq.fitnesscounter.role.tester.controller.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

class StopwatchService: Service()
{

    override fun onBind(intent: Intent): IBinder?
    {
        // TODO: Return the communication channel to the service.
        throw UnsupportedOperationException("Not yet implemented")
    }
}
