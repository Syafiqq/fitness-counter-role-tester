package com.github.syafiqq.fitnesscounterstudent.controller.auth

import com.github.syafiqq.fitnesscounterstudent.model.firebase.Path
import com.github.syafiqq.fitnesscounterstudent.model.orm.Groups
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

/**
 * This fitness-counter-student project created by :
 * Name         : syafiq
 * Date / Time  : 13 November 2017, 3:41 PM.
 * Email        : id.muhammad.syafiq@gmail.com
 * Github       : Syafiqq
 */
object Auth
{
    fun grantTo(group: Groups, user: FirebaseUser, callback: DatabaseReference.CompletionListener? = null)
    {
        FirebaseDatabase.getInstance().getReference("/${Path.USERS_GROUPS}/${user.uid}").apply {
            updateChildren(mapOf(group.id.toString() to true), callback)
        }
    }
}