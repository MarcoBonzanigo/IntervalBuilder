package ch.scythetec.intervalbuilder.helper

import ch.scythetec.intervalbuilder.datamodel.Exercise
import ch.scythetec.intervalbuilder.datamodel.IntervalSequence
import ch.scythetec.intervalbuilder.datamodel.IntervalSequenceList
import java.io.Serializable
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
class NullHelper private constructor(){
    companion object {
        fun <T: Serializable> get(clazz: KClass<T>): T{
            return when(clazz){
                String::class -> "" as T
                Long::class -> 0 as T
                Float::class -> 0 as T
                Double::class -> 0 as T
                Integer::class -> 0 as T
                Short::class -> 0 as T
                Boolean::class -> false as T
                ByteArray::class -> ByteArray(0) as T
                Exercise::class -> Exercise.NullExercise.get() as T
                IntervalSequence::class -> IntervalSequence.NullIntervalSequence.get() as T
                IntervalSequenceList::class -> IntervalSequenceList.NullIntervalSequenceList.get() as T
                else -> throw ClassNotFoundException()
            }
        }
    }
}