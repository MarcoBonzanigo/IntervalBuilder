package ch.scythetec.intervalbuilder.activities

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import ch.scythetec.intervalbuilder.R
import ch.scythetec.intervalbuilder.const.Constants.Companion.EMPTY
import ch.scythetec.intervalbuilder.datamodel.Exercise
import ch.scythetec.intervalbuilder.datamodel.IntervalSequence
import ch.scythetec.intervalbuilder.datamodel.IntervalSequenceList
import ch.scythetec.intervalbuilder.fragments.IntervalSequenceFragment
import ch.scythetec.intervalbuilder.helper.DatabaseHelper
import ch.scythetec.intervalbuilder.helper.PermissionHelper
import ch.scythetec.intervalbuilder.helper.TimeHelper
import kotlinx.android.synthetic.main.activity_builder.*


private var timeInterval: Long = 0
private var timeRest: Long = 0
private var repetitions: Long = 0
private var intervalSequenceList: IntervalSequenceList? = null
private var intervalSequenceFragments: ArrayList<IntervalSequenceFragment> = ArrayList()
private var notify = true

private var isLocked = true
private var exercise: Exercise? = null
private var popupWindow: PopupWindow? = null
private var loadedExerciseFragments = ArrayList<View>()

class BuilderActivity : AbstractBaseActivity() {
    override fun getConfiguredLayout(): Int {
        return R.layout.activity_builder
    }
    override fun getResizableTextViews(): Array<TextView> {
        return arrayOf(builder_text_stats_rest,
            builder_text_stats_interval,
            builder_text_interval_value,
            builder_text_rest_value,
            builder_text_repetitions_value)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restoreValues()
        builder_layout_active.setOnHierarchyChangeListener(object :
            ViewGroup.OnHierarchyChangeListener {
            override fun onChildViewRemoved(parent: View?, child: View?) {
                if (notify) {
                    refreshMovementButtons()
                }
            }

            override fun onChildViewAdded(parent: View?, child: View?) {
                if (notify) {
                    refreshMovementButtons()
                }
            }
        })
    }

    private fun restoreValues() {
        //TODO restore values
        toggleLock(true)
        timeInterval = 45
        timeRest = 15
        repetitions = 1
        title = EMPTY
        updateValues()
    }

    private fun updateValues() {
        builder_text_interval_value.text = TimeHelper.secondsToMinSecString(timeInterval)
        builder_text_rest_value.text = TimeHelper.secondsToMinSecString(timeRest)
        builder_text_repetitions_value.text = repetitions.toString()

        var timeIntervalTotal: Long = 0
        var timeRestTotal: Long = 0

        for (intervalSequenceFragment in intervalSequenceFragments) {
            timeIntervalTotal += intervalSequenceFragment.intervalSequence.timeInterval
            timeRestTotal += intervalSequenceFragment.intervalSequence.timeRest
        }
        builder_text_stats_interval.text =
            getString(R.string.interval_total, TimeHelper.secondsToMinSecString(timeIntervalTotal))
        builder_text_stats_rest.text =
            getString(R.string.rest_total, TimeHelper.secondsToMinSecString(timeRestTotal))
    }

    override fun onButtonClicked(v: View) {
        when (v.id) {
            R.id.builder_button_toggle_lock -> toggleLock()
            R.id.builder_button_load -> {
                //TODO implement
                val readBulk = DatabaseHelper.getInstance(this).readBulk(IntervalSequenceList::class, "%")
                if (readBulk.isNotEmpty()){
                    val list = readBulk[0]
                    intervalSequenceFragments.clear()
                    intervalSequenceFragments.addAll(list.getFragments())
                    for (intervalSequenceFragment in intervalSequenceFragments) {
                        supportFragmentManager.beginTransaction().add(
                            builder_layout_active.id,
                            intervalSequenceFragment
                        ).commit()
                    }
                }
                toggleLock(true)
            }
            R.id.builder_button_save -> {
                if (intervalSequenceFragments.isEmpty()){
                    toast("Nothing to save!")
                    return
                }
                //Open input
                val name = "INTERVAL "+System.nanoTime() //TODO: replace with popup
                intervalSequenceList = IntervalSequenceList.create(name, intervalSequenceFragments)
                if (PermissionHelper.check(applicationContext, PermissionHelper.Companion.PermissionGroups.STORAGE)){
                    storeAndReset()
                }else{
                    PermissionHelper.request(this, PermissionHelper.Companion.PermissionGroups.STORAGE)
                }
            }
            R.id.builder_button_exercise -> {
                if (popupWindow != null){
                    return
                }
                val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val popupView = inflater.inflate(R.layout.popup_window_fragment, null)

                val width = LinearLayout.LayoutParams.MATCH_PARENT
                val height = LinearLayout.LayoutParams.MATCH_PARENT
                popupWindow = PopupWindow(popupView, width, height, false)
                popupWindow!!.showAtLocation(builder_root, Gravity.CENTER, 0, 0)

                val contentView = popupWindow!!.contentView.findViewById<LinearLayout>(R.id.popup_scroll_content)
                val exercises = DatabaseHelper.getInstance(this).readBulk(Exercise::class, "%")
                for (exercise in exercises){
                    val exerciseFragment = LayoutInflater.from(this).inflate(R.layout.exercise_fragment, null)
                    exerciseFragment.findViewById<TextView>(R.id.exercise_fragment_text).text = exercise.title
                    exerciseFragment.findViewById<ImageView>(R.id.exercise_fragment_image).setImageBitmap(exercise.imageSmall)
                    exerciseFragment.setOnClickListener {
                        selectExercise(exercise,exerciseFragment)
                    }
                    exerciseFragment.id = View.generateViewId()
                    loadedExerciseFragments.add(exerciseFragment)
                    contentView.addView(exerciseFragment)
                }
            }
            R.id.builder_button_exercise_remove -> {
                removeExercise()
            }
            R.id.builder_button_interval_minus -> timeInterval -= if (timeInterval > 5) 5 else 0
            R.id.builder_button_interval_plus -> timeInterval += 5
            R.id.builder_button_rest_minus -> timeRest -= if (timeRest > 5) 5 else 0
            R.id.builder_button_rest_plus -> timeRest += 5
            R.id.builder_button_repetitions_minus -> repetitions -= if (repetitions > 1) 1 else 0
            R.id.builder_button_repetitions_plus -> repetitions += 1
            R.id.builder_button_add -> {
                val intervalSequence =
                    IntervalSequence.create(timeInterval, timeRest, repetitions, exercise, intervalSequenceFragments.size)
                val intervalSequenceFragment = IntervalSequenceFragment.create(intervalSequence)
                supportFragmentManager.beginTransaction().add(
                    builder_layout_active.id,
                    intervalSequenceFragment
                ).commit()
                intervalSequenceFragments.add(intervalSequenceFragment.addLockState(isLocked))
                removeExercise()
            }
        }
        updateValues()
    }

    private fun removeExercise() {
        exercise = null
        builder_button_exercise_remove.visibility = GONE
        builder_button_exercise.text = getString(R.string.select_exercise)
    }

    override fun onFragmentButtonClicked(view: View, parentId: Int) {
        when (view.id) {
            R.id.interval_fragment_button_down -> {
                moveFragmentDown(parentId)
            }
            R.id.interval_fragment_button_up -> {
                moveFragmentUp(parentId)
            }
            R.id.interval_fragment_button_delete -> {
                deleteFragment(parentId)
                reorganizePositions()
            }
        }
    }

    private fun reorganizePositions() {
        for ((pos, intervalFragmentSequence) in intervalSequenceFragments.withIndex()) {
            intervalFragmentSequence.intervalSequence.position = pos
        }
    }

    override fun onPopupWindowClicked(view: View) {
        when (view.id) {
            R.id.popup_button_add -> {
                if (exercise == null){
                    toast("No Exercise selected!")
                }else{
                    popupWindow!!.dismiss()
                    popupWindow = null
                    builder_button_exercise_remove.visibility = VISIBLE
                    builder_button_exercise.text = exercise!!.title
                }
            }
            R.id.popup_button_cancel -> {
                cancelPopup()
            }
        }
    }

    private fun cancelPopup() {
        popupWindow!!.dismiss()
        popupWindow = null
        exercise = null
        loadedExerciseFragments.clear()
    }

    private fun selectExercise(ex: Exercise, exerciseFragment: View) {
        exercise = ex
        for (view in loadedExerciseFragments){
            if (view.id == exerciseFragment.id){
                view.background = ContextCompat.getDrawable(applicationContext,R.drawable.background_selected)
            }else{
                view.background = ContextCompat.getDrawable(applicationContext,R.drawable.background)
            }
        }
    }

    private fun moveFragmentDown(parentId: Int) {
        for (i in 0 until intervalSequenceFragments.size) {
            if (intervalSequenceFragments[i].parentLayout.id == parentId) {
                intervalSequenceFragments[i].intervalSequence.position++
                intervalSequenceFragments[i + 1].intervalSequence.position--
            }
        }
        sortAndRefresh()
    }

    private fun moveFragmentUp(parentId: Int) {
        for (i in intervalSequenceFragments.indices) {
            if (intervalSequenceFragments[i].parentLayout.id == parentId) {
                intervalSequenceFragments[i].intervalSequence.position--
                intervalSequenceFragments[i - 1].intervalSequence.position++
            }
        }
        sortAndRefresh()
    }

    private fun toggleLock(forceClose: Boolean = false) {
        isLocked = if (forceClose) true else !isLocked
        builder_button_toggle_lock.text = getText(if (isLocked) R.string.icon_lock else R.string.icon_unlock)
        for (fragment in intervalSequenceFragments){
            fragment.toggleLock(isLocked)
        }
    }

    private fun sortAndRefresh() {
        val sorted = intervalSequenceFragments.sortedWith(compareBy { it.intervalSequence.position })
        intervalSequenceFragments.clear()
        notify = false
        builder_layout_active.removeAllViews()
        for (l in sorted.indices) {
            builder_layout_active.addView(sorted[l].parentLayout)
            intervalSequenceFragments.add(sorted[l])
        }
        notify = true
        refreshMovementButtons()
    }

    /**
     * Updates movement buttons based on their position
     */
    private fun refreshMovementButtons() {
        for (l in 0 until builder_layout_active.childCount) {
            builder_layout_active.getChildAt(l)
                .findViewById<Button>(R.id.interval_fragment_button_up).isEnabled = (l != 0)
            builder_layout_active.getChildAt(l)
                .findViewById<Button>(R.id.interval_fragment_button_up).setTextColor(
                    if (l != 0) ContextCompat.getColor(
                        this,
                        R.color.ibPrimaryPastel
                    ) else ContextCompat.getColor(this, R.color.ibGray)
                )
            builder_layout_active.getChildAt(l)
                .findViewById<Button>(R.id.interval_fragment_button_down).isEnabled =
                (l != builder_layout_active.childCount - 1)
            builder_layout_active.getChildAt(l)
                .findViewById<Button>(R.id.interval_fragment_button_down).setTextColor(
                    if (l != builder_layout_active.childCount - 1) ContextCompat.getColor(
                        this,
                        R.color.ibPrimaryPastel
                    ) else ContextCompat.getColor(this, R.color.ibGray)
                )
        }
    }

    private fun deleteFragment(parentId: Int) {
        var found = false
        for (intervalFragmentSequence in intervalSequenceFragments) {
            if (intervalFragmentSequence.parentLayout.id == parentId) {
                found = true
                intervalSequenceFragments.remove(intervalFragmentSequence)
                for (i in 0 until builder_layout_active.childCount) {
                    if (builder_layout_active.getChildAt(i).id == parentId) {
                        supportFragmentManager.beginTransaction().remove(intervalFragmentSequence).commit()
                        builder_layout_active.removeViewAt(i)
                        updateValues()
                        return
                    }
                }
            }
        }
    }

    private fun storeAndReset() {
        DatabaseHelper.getInstance(this).delete("%",IntervalSequenceList::class)
        DatabaseHelper.getInstance(this).write(intervalSequenceList!!.id, intervalSequenceList!!)
        reset()
    }

    private fun reset(){
        exercise = null
        intervalSequenceList = null
        intervalSequenceFragments.clear()
        notify = true
        toggleLock(true)
        loadedExerciseFragments.clear()
        builder_layout_active.removeAllViews()
        builder_button_exercise_remove.visibility = GONE
        builder_button_exercise.text = getString(R.string.select_exercise)
        updateValues()
    }

    override fun onRequestPermissionsResult(requestCode: Int,permissions: Array<out String>,grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (PermissionHelper.check(applicationContext, PermissionHelper.Companion.PermissionGroups.STORAGE)){
            if (intervalSequenceList != null){
                storeAndReset()
            }
        }
    }

    override fun onBackPressed() {
        if (popupWindow != null){
            cancelPopup()
        }else{
            super.onBackPressed()
        }
    }
}
