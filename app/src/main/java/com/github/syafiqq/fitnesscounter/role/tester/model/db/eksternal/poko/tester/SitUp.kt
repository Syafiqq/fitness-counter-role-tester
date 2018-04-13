package com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.poko.tester

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity

/**
 * This fitness-counter-role-tester project created by :
 * Name         : syafiq
 * Date / Time  : 10 April 2018, 6:55 AM.
 * Email        : id.muhammad.syafiq@gmail.com
 * Github       : Syafiqq
 */
@Entity(tableName = "sit")
data class SitUp(
        // @formatter:off
        //@PrimaryKey var uid:Int? = null,
        // Id
        @ColumnInfo(name = "queue")       var queue: Int? = null,
        @ColumnInfo(name = "preset")      var preset: String? = null,
        @ColumnInfo(name = "stamp")       var stamp: String? = null,

        @ColumnInfo(name = "start")       var start: Long? = null,
        @ColumnInfo(name = "counter")     var counter: Long? = null
        // @formatter:on
) {
    fun set(sit: SitUp) {
        //this.uid =sit.uid
        this.queue = sit.queue
        this.preset = sit.preset
        this.stamp = sit.stamp
        this.start = sit.start
        this.counter = sit.counter
    }

    companion object {
        val EMPTY_DATA = SitUp()
    }
}
