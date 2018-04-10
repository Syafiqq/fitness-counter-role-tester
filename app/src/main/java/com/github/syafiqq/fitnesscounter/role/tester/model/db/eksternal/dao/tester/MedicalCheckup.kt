package com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.dao.tester

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
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

    @Insert
    fun insert(vararg medical: PMedicalCheckup)

    @Delete()
    fun delete(medical: PMedicalCheckup)
}