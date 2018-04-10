package com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.dao.tester

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.poko.tester.VerticalJump as PVerticalJump

/**
 * This fitness-counter-role-tester project created by :
 * Name         : syafiq
 * Date / Time  : 10 April 2018, 7:52 PM.
 * Email        : id.muhammad.syafiq@gmail.com
 * Github       : Syafiqq
 */
@Dao
interface VerticalJump {
    @Query("SELECT * FROM `vertical` WHERE `preset`=:preset")
    fun findByPreset(preset: String): List<PVerticalJump>

    @Insert
    fun insert(vararg vertical: PVerticalJump)

    @Delete()
    fun delete(vertical: PVerticalJump)
}