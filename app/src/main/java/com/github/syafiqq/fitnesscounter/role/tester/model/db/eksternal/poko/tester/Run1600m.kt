package com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.poko.tester

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/**
 * This fitness-counter-role-tester project created by :
 * Name         : syafiq
 * Date / Time  : 10 April 2018, 6:50 AM.
 * Email        : id.muhammad.syafiq@gmail.com
 * Github       : Syafiqq
 */
@Entity(tableName = "run")
data class Run1600m(
        // @formatter:off
        @PrimaryKey var uid:Int? = null,
        // Id
        @ColumnInfo(name = "queue")       var queue: Int? = null,
        @ColumnInfo(name = "preset")      var preset: String? = null,
        @ColumnInfo(name = "stamp")       var stamp: String? = null,

        @ColumnInfo(name = "start")      var start: Long? = null,
        @ColumnInfo(name = "lap1")       var lap1: Long? = null,
        @ColumnInfo(name = "lap2")       var lap2: Long? = null,
        @ColumnInfo(name = "lap3")       var lap3: Long? = null,
        @ColumnInfo(name = "end")        var end: Long? = null,
        @ColumnInfo(name = "elapsed")    var elapsed: Long? = null
        // @formatter:on
) {
    fun set(run: Run1600m) {
        this.uid = run.uid
        this.queue = run.queue
        this.preset = run.preset
        this.stamp = run.stamp
        this.start = run.start
        this.lap1 = run.lap1
        this.lap2 = run.lap2
        this.lap3 = run.lap3
        this.end = run.end
        this.elapsed = run.elapsed
    }

    companion object {
        val EMPTY_DATA = Run1600m()
    }
}