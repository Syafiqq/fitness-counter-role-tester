package com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.dao.tester

import android.arch.persistence.room.*
import com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.poko.tester.ThrowingBall as PThrowingBall

/**
 * This fitness-counter-role-tester project created by :
 * Name         : syafiq
 * Date / Time  : 10 April 2018, 7:50 PM.
 * Email        : id.muhammad.syafiq@gmail.com
 * Github       : Syafiqq
 */
@Dao
interface ThrowingBall {
    @Query("SELECT * FROM `throwing` WHERE `preset`=:preset")
    fun findByPreset(preset: String): List<PThrowingBall>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg throwing: PThrowingBall)

    @Delete
    fun delete(vararg throwing: PThrowingBall)

    @Query("DELETE FROM `throwing` WHERE `queue` = :queue AND `preset` = :preset")
    fun delete(preset: String, queue: Int)
}