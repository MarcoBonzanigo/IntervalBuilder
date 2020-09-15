package ch.scythetec.intervalbuilder.fragments

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import ch.scythetec.intervalbuilder.R
import ch.scythetec.intervalbuilder.datamodel.IntervalSequence
import ch.scythetec.intervalbuilder.helper.TimeHelper

class IntervalSequenceFragment  private  constructor (val intervalSequence: IntervalSequence): Fragment() {

    lateinit var parentLayout: ConstraintLayout
    var lockState: Boolean? = null

    companion object{
        fun create(intervalSequence: IntervalSequence) : IntervalSequenceFragment {
            return IntervalSequenceFragment(intervalSequence)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.interval_sequence_fragment, container, false)
        val timeIntervalStr = TimeHelper.secondsToMinSecString(intervalSequence.timeInterval)
        val timeRestStr = TimeHelper.secondsToMinSecString(intervalSequence.timeRest)
        view.findViewById<TextView>(R.id.interval_fragment_text).text = Html.fromHtml(
            getString(R.string.interval_layout_string,timeIntervalStr,timeRestStr,intervalSequence.repetitions,
            when {
                intervalSequence.exercise == null -> getString(R.string.none)
                else -> intervalSequence.exercise!!.title
            }))
        view.id = View.generateViewId()
        parentLayout = view as ConstraintLayout
        intervalSequence.viewId = view.id
        parentLayout.findViewById<Button>(R.id.interval_fragment_button_up).isEnabled = intervalSequence.position != 0
        parentLayout.findViewById<Button>(R.id.interval_fragment_button_down).isEnabled = false
        if (lockState != null){
            toggleLock(lockState!!)
            lockState = null
        }
        return view
    }

    private fun lock(){
        if (this::parentLayout.isInitialized){
            parentLayout.findViewById<Button>(R.id.interval_fragment_button_up).visibility = INVISIBLE
            parentLayout.findViewById<Button>(R.id.interval_fragment_button_down).visibility = INVISIBLE
            parentLayout.findViewById<Button>(R.id.interval_fragment_button_delete).visibility = INVISIBLE
        }
    }

    private fun unlock(){
        if (this::parentLayout.isInitialized) {
            parentLayout.findViewById<Button>(R.id.interval_fragment_button_up).visibility = VISIBLE
            parentLayout.findViewById<Button>(R.id.interval_fragment_button_down).visibility = VISIBLE
            parentLayout.findViewById<Button>(R.id.interval_fragment_button_delete).visibility = VISIBLE
        }
    }

    fun toggleLock(locked: Boolean): IntervalSequenceFragment {
        if (locked) lock() else unlock()
        return this
    }

    fun addLockState(locked: Boolean): IntervalSequenceFragment {
        lockState = locked
        return this
    }
}
