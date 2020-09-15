package ch.scythetec.intervalbuilder.datamodel

import ch.scythetec.intervalbuilder.const.Constants.Companion.EMPTY
import ch.scythetec.intervalbuilder.const.Constants.Companion.SEMI_COLON
import ch.scythetec.intervalbuilder.fragments.IntervalSequenceFragment
import ch.scythetec.intervalbuilder.helper.StringHelper
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

open class IntervalSequenceList private constructor(): Serializable {
    var title: String = EMPTY
    var id: String = EMPTY
    val intervalSequences: ArrayList<IntervalSequence> = ArrayList()

    companion object{
        fun create(title: String, intervalSequences: List<IntervalSequenceFragment>, id: String = UUID.randomUUID().toString()): IntervalSequenceList{
            val sequence = IntervalSequenceList()
            sequence.id = id
            sequence.title = title
            intervalSequences.forEach { sequence.intervalSequences.add(it.intervalSequence) }
            sequence.sort()
            return sequence
        }
        fun create(id: String, title: String, intervalSequences: List<IntervalSequence>): IntervalSequenceList{
            val sequence = IntervalSequenceList()
            sequence.id = id
            sequence.title = title
            sequence.intervalSequences.addAll(intervalSequences)
            sequence.sort()
            return sequence
        }
    }

    /**
     * Sorts elements based on position
     */
    fun sort(){
        val sorted = intervalSequences.sortedWith(compareBy { it.position })
        intervalSequences.clear()
        intervalSequences.addAll(sorted)
    }

    /**
     * Returns a copy of the contained elements
     */
    fun getFragments(): ArrayList<IntervalSequenceFragment> {
        val list: ArrayList<IntervalSequenceFragment> = ArrayList()
        intervalSequences.forEach { list.add(IntervalSequenceFragment.create(it)) }
        return list
    }

    fun getSequenceListAsString(): String{
        val list = ArrayList<String>()
        for (intervalSequence in intervalSequences){
            list.add(intervalSequence.id)
        }
        return StringHelper.concatTokens(SEMI_COLON, *list.toTypedArray())
    }


    class NullIntervalSequenceList private constructor() : IntervalSequenceList() {
        companion object{
            fun get(): IntervalSequenceList{
                return create(EMPTY, listOf<IntervalSequenceFragment>(IntervalSequenceFragment.create(IntervalSequence.NullIntervalSequence.get())))
            }
        }
    }
}