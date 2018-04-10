package com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.dao.tester

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.poko.tester.SitUp as PSitUp

/**
 * This fitness-counter-role-tester project created by :
 * Name         : syafiq
 * Date / Time  : 10 April 2018, 7:49 PM.
 * Email        : id.muhammad.syafiq@gmail.com
 * Github       : Syafiqq
 */
@Dao
interface SitUp {
    @Query("SELECT * FROM `sit` WHERE `preset`=:preset")
    fun findByPreset(preset: String): List<PSitUp>

    @Insert
    fun insert(vararg sit: PSitUp)

    @Delete()
    fun delete(sit: PSitUp)
}