package com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.poko.tester

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity

/**
 * This fitness-counter-role-tester project created by :
 * Name         : syafiq
 * Date / Time  : 10 April 2018, 6:56 AM.
 * Email        : id.muhammad.syafiq@gmail.com
 * Github       : Syafiqq
 */
@Entity(tableName = "throwing", primaryKeys = ["queue", "preset"])
data class ThrowingBall(
        // @formatter:off
        //@PrimaryKey var uid:Int? = null,
        // Id
        @ColumnInfo(name = "queue")       var queue: Int = 0,
        @ColumnInfo(name = "preset")      var preset: String = "",
        @ColumnInfo(name = "stamp")       var stamp: String? = null,

        @ColumnInfo(name = "start")       var start: Long? = null,
        @ColumnInfo(name = "counter")     var counter: Long? = null
        // @formatter:on
) {
    fun set(throwing: ThrowingBall) {
        //this.uid =throwing.uid
        this.queue = throwing.queue
        this.preset = throwing.preset
        this.stamp = throwing.stamp
        this.start = throwing.start
        this.counter = throwing.counter
    }

    companion object {
        val EMPTY_DATA = ThrowingBall()
    }
}