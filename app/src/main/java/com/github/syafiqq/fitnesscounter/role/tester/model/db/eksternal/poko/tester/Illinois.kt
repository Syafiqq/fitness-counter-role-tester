package com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.poko.tester

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity

/**
 * This fitness-counter-role-tester project created by :
 * Name         : syafiq
 * Date / Time  : 10 April 2018, 6:47 AM.
 * Email        : id.muhammad.syafiq@gmail.com
 * Github       : Syafiqq
 */
@Entity(tableName = "illinois", primaryKeys = ["queue", "preset"])
data class Illinois(
        // @formatter:off
        //@PrimaryKey var uid:Int? = null,
        // Id
        @ColumnInfo(name = "queue")       var queue: Int = 0,
        @ColumnInfo(name = "preset")      var preset: String = "",
        @ColumnInfo(name = "stamp")       var stamp: String? = null,

        @ColumnInfo(name = "start")       var start: Long? = null,
        @ColumnInfo(name = "end")         var end: Long? = null,
        @ColumnInfo(name = "elapsed")     var elapsed: Long? = null
        // @formatter:on
) {
    fun set(illinois: Illinois) {
        //this.uid =illinois.uid
        this.queue = illinois.queue
        this.preset = illinois.preset
        this.stamp = illinois.stamp
        this.start = illinois.start
        this.end = illinois.end
        this.elapsed = illinois.elapsed
    }

    companion object {
        val EMPTY_DATA = Illinois()
    }
}