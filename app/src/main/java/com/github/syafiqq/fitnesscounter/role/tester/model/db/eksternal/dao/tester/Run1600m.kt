package com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.dao.tester

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.poko.tester.Run1600m as PRun1600m

/**
 * This fitness-counter-role-tester project created by :
 * Name         : syafiq
 * Date / Time  : 10 April 2018, 7:47 PM.
 * Email        : id.muhammad.syafiq@gmail.com
 * Github       : Syafiqq
 */
@Dao
interface Run1600m {
    @Query("SELECT * FROM `run` WHERE `preset`=:preset")
    fun findByPreset(preset: String): List<PRun1600m>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg run: PRun1600m)

    @Query("DELETE FROM `run` WHERE `queue` = :queue AND `preset` = :preset")
    fun delete(preset: String, queue: Int)
}