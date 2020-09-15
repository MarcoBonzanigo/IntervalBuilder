package ch.scythetec.intervalbuilder.fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import ch.scythetec.intervalbuilder.R
import ch.scythetec.intervalbuilder.datamodel.Exercise

class ExerciseFragment  private  constructor (val exercise: Exercise): Fragment(){

    lateinit var parentLayout: ConstraintLayout
    var lockState: Boolean? = null

    companion object{
        fun create(exercise: Exercise) : ExerciseFragment {
            return ExerciseFragment(exercise)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.exercise_fragment, container, false)
        (view.findViewById(R.id.exercise_fragment_text) as TextView).text = exercise.title
        if (exercise.imageSmall != null){
            (view.findViewById(R.id.exercise_fragment_image) as ImageView).setImageBitmap(exercise.imageSmall)
        }else{
            (view.findViewById(R.id.exercise_fragment_image) as ImageView).setImageBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context?.resources,R.drawable.workout), 128, 128, false))
        }
        view.id = View.generateViewId()
        parentLayout = view as ConstraintLayout
        exercise.viewId = view.id
        if (lockState != null){
            toggleLock(lockState!!)
            lockState = null
        }
        return view
    }

    private fun lock(){
        parentLayout.findViewById<Button>(R.id.exercise_fragment_button_delete).visibility = INVISIBLE
    }

    private fun unlock(){
        parentLayout.findViewById<Button>(R.id.exercise_fragment_button_delete).visibility = VISIBLE
    }

    fun toggleLock(locked: Boolean): ExerciseFragment {
        if (locked) lock() else unlock()
        return this
    }

    fun addLockState(locked: Boolean): ExerciseFragment {
        lockState = locked
        return this
    }
}
