package com.github.syafiqq.fitnesscounterstudent.custom.com.google.firebase.database

import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import timber.log.Timber

/**
 * This fitness-counter-student project created by :
 * Name         : syafiq
 * Date / Time  : 13 November 2017, 1:48 PM.
 * Email        : id.muhammad.syafiq@gmail.com
 * Github       : Syafiqq
 */
interface DefaultErrorValueEventListener: ValueEventListener
{
    override fun onCancelled(error: DatabaseError?)
    {
        Timber.d("onCancelled")
        Timber.d("${error?.message}")
    }
}