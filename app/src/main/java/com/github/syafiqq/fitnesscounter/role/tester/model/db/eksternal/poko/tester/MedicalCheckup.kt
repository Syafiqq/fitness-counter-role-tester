package com.github.syafiqq.fitnesscounter.role.tester.model.db.eksternal.poko.tester

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/**
 * This fitness-counter-role-tester project created by :
 * Name         : syafiq
 * Date / Time  : 10 April 2018, 5:15 AM.
 * Email        : id.muhammad.syafiq@gmail.com
 * Github       : Syafiqq
 */
@Entity(tableName = "medical")
data class MedicalCheckup(
        // @formatter:off
        @PrimaryKey var uid:Int? = null,
        // Id
        @ColumnInfo(name = "queue")       var queue: Int? = null,
        @ColumnInfo(name = "preset")      var preset: String? = null,
        @ColumnInfo(name = "stamp")       var stamp: String? = null,

        // Anthropometric
        @ColumnInfo(name = "tbb")         var tbb: Float? = null,
        @ColumnInfo(name = "tbd")         var tbd: Float? = null,
        @ColumnInfo(name = "ratio")       var ratio: Float? = null,
        @ColumnInfo(name = "weight")      var weight: Float? = null,
        @ColumnInfo(name = "bmi")         var bmi: Float? = null,
        // Posture and Gait
        @ColumnInfo(name = "posture")     var posture: String? = null,
        @ColumnInfo(name = "gait")        var gait: String? = null,
        // Cardiovascular
        @ColumnInfo(name = "pulse")       var pulse: Float? = null,
        @ColumnInfo(name = "pressure")    var pressure: String? = null,
        @ColumnInfo(name = "ictus")       var ictus: String? = null,
        @ColumnInfo(name = "heart")       var heart: String? = null,
        // Respiratory
        @ColumnInfo(name = "frequency")   var frequency: Float? = null,
        @ColumnInfo(name = "retraction")  var retraction: String? = null,
        @ColumnInfo(name = "r_location")  var rLocation: String? = null,
        @ColumnInfo(name = "breath")      var breath: String? = null,
        @ColumnInfo(name = "b_pipeline")  var bPipeline: String? = null,
        // Verbal
        @ColumnInfo(name = "vision")      var vision: String? = null,
        @ColumnInfo(name = "hearing")     var hearing: String? = null,
        @ColumnInfo(name = "verbal")      var verbal: String? = null,
        // Conclusion
        @ColumnInfo(name = "conclusion")  var conclusion: Boolean = false
        // @formatter:on
) {
    fun set(medical: MedicalCheckup) {
        this.uid = medical.uid
        this.queue = medical.queue
        this.preset = medical.preset
        this.stamp = medical.stamp
        this.tbb = medical.tbb
        this.tbd = medical.tbd
        this.ratio = medical.ratio
        this.weight = medical.weight
        this.bmi = medical.bmi
        this.posture = medical.posture
        this.gait = medical.gait
        this.pulse = medical.pulse
        this.pressure = medical.pressure
        this.ictus = medical.ictus
        this.heart = medical.heart
        this.frequency = medical.frequency
        this.retraction = medical.retraction
        this.rLocation = medical.rLocation
        this.breath = medical.breath
        this.bPipeline = medical.bPipeline
        this.vision = medical.vision
        this.hearing = medical.hearing
        this.verbal = medical.verbal
        this.conclusion = medical.conclusion
    }

    companion object {
        val EMPTY_DATA = MedicalCheckup()
    }
}