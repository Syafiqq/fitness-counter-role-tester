package com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.poko.tester

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/**
 * This fitness-counter-role-tester project created by :
 * Name         : syafiq
 * Date / Time  : 10 April 2018, 6:56 AM.
 * Email        : id.muhammad.syafiq@gmail.com
 * Github       : Syafiqq
 */
@Entity(tableName = "throwing")
data class ThrowingBall(
        // @formatter:off
        @PrimaryKey var uid:Int? = null,
        // Id
        @ColumnInfo(name = "queue")       var queue: Int? = null,
        @ColumnInfo(name = "preset")      var preset: String? = null,

        @ColumnInfo(name = "start")       var start: Long? = null,
        @ColumnInfo(name = "counter")     var counter: Long? = null
        // @formatter:on
)