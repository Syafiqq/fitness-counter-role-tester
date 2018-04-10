package com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.dao.tester

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.poko.tester.PushUp as PPushUp

/**
 * This fitness-counter-role-tester project created by :
 * Name         : syafiq
 * Date / Time  : 10 April 2018, 7:46 PM.
 * Email        : id.muhammad.syafiq@gmail.com
 * Github       : Syafiqq
 */
@Dao
interface PushUp {
    @Query("SELECT * FROM `push` WHERE `preset`=:preset")
    fun findByPreset(preset: String): List<PPushUp>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg push: PPushUp)

    @Query("DELETE FROM `push` WHERE `queue` = :queue AND `preset` = :preset")
    fun delete(preset: String, queue: Int)
}