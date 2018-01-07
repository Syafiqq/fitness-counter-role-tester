package com.github.syafiqq.fitnesscounter.role.student.model

import com.github.syafiqq.fitnesscounter.role.student.model.orm.Groups
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import timber.log.Timber

/**
 * This fitness-counter-student project created by :
 * Name         : syafiq
 * Date / Time  : 12 November 2017, 9:53 PM.
 * Email        : id.muhammad.syafiq@gmail.com
 * Github       : Syafiqq
 */

object Settings
{
    fun defaultGroup(success: (Groups?) -> Unit, error: (DatabaseError?) -> Unit = { Timber.d(it?.message) })
    {
        val ref = FirebaseDatabase.getInstance().getReference("groups")
        ref.addListenerForSingleValueEvent(object: ValueEventListener
        {
            override fun onCancelled(dbError: DatabaseError?)
            {
                Timber.d("onCancelled")

                error(dbError)
            }

            override fun onDataChange(snapshot: DataSnapshot?)
            {
                Timber.d("onDataChange")

                val group = snapshot
                        ?.children
                        ?.map { it.getValue(Groups::class.java) }
                        ?.firstOrNull { it?.name == "student" }
                success(group)
            }
        })
    }

    val GROUP_NAME = "student"
}