package ch.scythetec.intervalbuilder.datamodel

import android.graphics.Bitmap
import ch.scythetec.intervalbuilder.const.Constants.Companion.EMPTY
import java.io.Serializable
import java.util.*

open class Exercise private constructor() : Serializable, Comparable<Exercise> {

    var id: String = EMPTY
    var image: Bitmap? = null
    var imageSmall: Bitmap? = null
    var title: String = EMPTY
    var description: String? = null
    var category: String? = null
    var viewId: Int? = null

    companion object{
        fun create(title: String, description: String?, image: Bitmap?, id: String = UUID.randomUUID().toString()) : Exercise{
            val exercise = Exercise()
            exercise.id = id
            exercise.title = title
            exercise.description = description
            exercise.image = image
            if (image != null){
                exercise.imageSmall = Bitmap.createScaledBitmap(image, 128, 128, false)
            }
            return exercise
        }

        fun create(title: String, description: String?, category: String?, image: Bitmap?, id: String = UUID.randomUUID().toString()) : Exercise{
            val exercise = Exercise()
            exercise.id = id
            exercise.title = title
            exercise.description = description
            exercise.image = image
            if (image != null){
                exercise.imageSmall = Bitmap.createScaledBitmap(image, 128, 128, false)
            }
            exercise.category = category
            return exercise
        }
    }

    override fun compareTo(other: Exercise): Int {
        return title.compareTo(other.title)
    }

    class NullExercise private constructor() : Exercise(){
        companion object{
            fun get(): Exercise{
                return create(EMPTY,null,null)
            }
        }
    }

}