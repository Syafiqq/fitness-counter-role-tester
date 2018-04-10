package com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.dao.tester

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.poko.tester.MedicalCheckup as PMedicalCheckup

/**
 * This fitness-counter-role-tester project created by :
 * Name         : syafiq
 * Date / Time  : 10 April 2018, 7:42 PM.
 * Email        : id.muhammad.syafiq@gmail.com
 * Github       : Syafiqq
 */
@Dao
interface MedicalCheckup {
    @Query("SELECT * FROM `medical` WHERE `preset`=:preset")
    fun findByPreset(preset: String): List<PMedicalCheckup>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg medical: PMedicalCheckup)

    @Query("DELETE FROM `medical` WHERE `queue` = :queue AND `preset` = :preset")
    fun delete(preset: String, queue: Int)
}