package com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.poko.tester

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity

/**
 * This fitness-counter-role-tester project created by :
 * Name         : syafiq
 * Date / Time  : 10 April 2018, 6:57 AM.
 * Email        : id.muhammad.syafiq@gmail.com
 * Github       : Syafiqq
 */
@Entity(tableName = "vertical", primaryKeys = ["queue", "preset"])
data class VerticalJump(
        // @formatter:off
        //@PrimaryKey var uid:Int? = null,
        // Id
        @ColumnInfo(name = "queue")       var queue: Int = 0,
        @ColumnInfo(name = "preset")      var preset: String = "",
        @ColumnInfo(name = "stamp")       var stamp: String? = null,

        @ColumnInfo(name = "initial")      var initial: Float? = null,
        @ColumnInfo(name = "try1")         var try1: Float? = null,
        @ColumnInfo(name = "try2")         var try2: Float? = null,
        @ColumnInfo(name = "try3")         var try3: Float? = null,
        @ColumnInfo(name = "deviation")    var deviation: Float? = null
        // @formatter:on
) {
    fun set(vertical: VerticalJump) {
        //this.uid =vertical.uid
        this.queue = vertical.queue
        this.preset = vertical.preset
        this.stamp = vertical.stamp
        this.initial = vertical.initial
        this.try1 = vertical.try1
        this.try2 = vertical.try2
        this.try3 = vertical.try3
        this.deviation = vertical.deviation
    }

    companion object {
        val EMPTY_DATA = VerticalJump()
    }
}