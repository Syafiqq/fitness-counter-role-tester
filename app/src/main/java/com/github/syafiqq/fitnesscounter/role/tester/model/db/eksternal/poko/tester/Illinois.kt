package com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.poko.tester

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/**
 * This fitness-counter-role-tester project created by :
 * Name         : syafiq
 * Date / Time  : 10 April 2018, 6:47 AM.
 * Email        : id.muhammad.syafiq@gmail.com
 * Github       : Syafiqq
 */
@Entity
data class Illinois(
        // @formatter:off
        @PrimaryKey var uid:Int? = null,
        // Id
        @ColumnInfo(name = "queue")       var queue: Int? = null,
        @ColumnInfo(name = "preset")      var preset: String? = null,

        @ColumnInfo(name = "start")       var start: Long? = null,
        @ColumnInfo(name = "end")         var end: Long? = null,
        @ColumnInfo(name = "elapsed")     var elapsed: Long? = null
        // @formatter:on
)