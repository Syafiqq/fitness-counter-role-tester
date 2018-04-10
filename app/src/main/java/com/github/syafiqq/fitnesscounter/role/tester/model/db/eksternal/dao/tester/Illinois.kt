package com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.dao.tester

import android.arch.persistence.room.*
import com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.poko.tester.Illinois as PIllinois

/**
 * This fitness-counter-role-tester project created by :
 * Name         : syafiq
 * Date / Time  : 10 April 2018, 7:02 AM.
 * Email        : id.muhammad.syafiq@gmail.com
 * Github       : Syafiqq
 */
@Dao
interface Illinois {
    @Query("SELECT * FROM `illinois` WHERE `preset`=:preset")
    fun findByPreset(preset: String): List<PIllinois>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg illinois: PIllinois)

    @Delete()
    fun delete(illinois: PIllinois)
}