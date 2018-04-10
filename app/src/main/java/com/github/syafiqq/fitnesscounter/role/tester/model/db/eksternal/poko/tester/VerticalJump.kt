package com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.poko.tester

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/**
 * This fitness-counter-role-tester project created by :
 * Name         : syafiq
 * Date / Time  : 10 April 2018, 6:57 AM.
 * Email        : id.muhammad.syafiq@gmail.com
 * Github       : Syafiqq
 */
@Entity
data class VerticalJump(
        // @formatter:off
        @PrimaryKey var uid:Int? = null,
        // Id
        @ColumnInfo(name = "queue")       var queue: Int? = null,
        @ColumnInfo(name = "preset")      var preset: String? = null,

        @ColumnInfo(name = "initial")      var initial: Float? = null,
        @ColumnInfo(name = "try1")         var try1: Float? = null,
        @ColumnInfo(name = "try2")         var try2: Float? = null,
        @ColumnInfo(name = "try3")         var try3: Float? = null,
        @ColumnInfo(name = "deviation")    var deviation: Float? = null
        // @formatter:on
)