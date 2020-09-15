package ch.scythetec.intervalbuilder.datamodel

import ch.scythetec.intervalbuilder.const.Constants.Companion.EMPTY
import java.util.*

open class IntervalSequence private constructor(){
    var id: String = EMPTY
    var timeInterval: Long = 0
    var timeRest: Long = 0
    var repetitions: Long = 0
    var exercise: Exercise? = null
    var position: Int = 0
    var viewId: Int = 0

    companion object {
        fun create(timeInterval: Long, timeRest: Long, repetitions: Long, exercise: Exercise?, position: Int, id: String = UUID.randomUUID().toString()) : IntervalSequence {
            val intervalSequence = IntervalSequence()
            intervalSequence.id = id
            intervalSequence.timeInterval = timeInterval
            intervalSequence.timeRest = timeRest
            intervalSequence.repetitions = repetitions
            intervalSequence.exercise = exercise
            intervalSequence.position = position
            return intervalSequence
        }
    }

    class NullIntervalSequence private constructor() : IntervalSequence(){
        companion object{
            fun get(): IntervalSequence{
                return create(0,0,0,null,0)
            }
        }
    }
}