package com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.dao.tester.Illinois as DIllinois
import com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.dao.tester.MedicalCheckup as DMedicalCheckup
import com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.dao.tester.PushUp as DPushUp
import com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.dao.tester.Run1600m as DRun1600m
import com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.dao.tester.SitUp as DSitUp
import com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.dao.tester.ThrowingBall as DThrowingBall
import com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.dao.tester.VerticalJump as DVerticalJump
import com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.poko.tester.Illinois as PIllinois
import com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.poko.tester.MedicalCheckup as PMedicalCheckup
import com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.poko.tester.PushUp as PPushUp
import com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.poko.tester.Run1600m as PRun1600m
import com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.poko.tester.SitUp as PSitUp
import com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.poko.tester.ThrowingBall as PThrowingBall
import com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.poko.tester.VerticalJump as PVerticalJump

/**
 * This fitness-counter-role-tester project created by :
 * Name         : syafiq
 * Date / Time  : 10 April 2018, 8:01 PM.
 * Email        : id.muhammad.syafiq@gmail.com
 * Github       : Syafiqq
 */

@Database(entities = [PIllinois::class, PMedicalCheckup::class, PPushUp::class, PRun1600m::class, PSitUp::class, PThrowingBall::class, PVerticalJump::class], version = 1)
abstract class Database : RoomDatabase() {
    abstract fun illinois(): DIllinois
    abstract fun medical(): DMedicalCheckup
    abstract fun push(): DPushUp
    abstract fun run(): DRun1600m
    abstract fun sit(): DSitUp
    abstract fun throwing(): DThrowingBall
    abstract fun vertical(): DVerticalJump
}