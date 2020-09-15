package ch.scythetec.intervalbuilder.activities

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import ch.scythetec.intervalbuilder.R
import ch.scythetec.intervalbuilder.const.Constants.Companion.EMPTY
import ch.scythetec.intervalbuilder.const.Constants.Companion.SELECT_PICTURE
import ch.scythetec.intervalbuilder.datamodel.Exercise
import ch.scythetec.intervalbuilder.fragments.ExerciseFragment
import ch.scythetec.intervalbuilder.helper.DatabaseHelper
import ch.scythetec.intervalbuilder.helper.ObjectHelper
import ch.scythetec.intervalbuilder.helper.StringHelper
import kotlinx.android.synthetic.main.activity_exercise.*


private var notify = true
private var isLocked = true
private var silenceListener = false
private var exerciseImage: Bitmap? = null
private var exerciseDescription: String = EMPTY
private var exerciseTitle: String = EMPTY
private var exerciseFragments: ArrayList<ExerciseFragment> = ArrayList()
private var oldHintColor: ColorStateList? = null

class ExerciseActivity : AbstractBaseActivity() {

    override fun getResizableTextViews(): Array<TextView> {
        return arrayOf()
    }

    override fun getConfiguredLayout(): Int {
        return R.layout.activity_exercise
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addWatchers()
        loadData()
    }

    private fun addWatchers() {
        exercise_edit_text_title.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                exerciseTitle = s?.toString() ?: EMPTY
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (oldHintColor != null){
                    exercise_edit_text_title.setHintTextColor(oldHintColor)
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {  /*NOP*/ }
        })
        exercise_edit_text_description.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!silenceListener) {
                    var counter = 0
                    val str = ObjectHelper.nvl(s,"").toString()
                    for (character in str.toCharArray()) {
                        if (character == '\n') counter++
                    }
                    if (counter >= 5) {
                        exercise_button_add.requestFocus()
                        hideKeyboard()
                        silenceListener = true
                        exercise_edit_text_description.setText(exerciseDescription)
                    } else {
                        exerciseDescription = str
                    }
                } else {
                    silenceListener = false
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                exerciseDescription = ObjectHelper.nvl(s,"").toString()
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {  /*NOP*/ }
        })
        val proposals =  arrayOf<String>("Legs", "Arms", "Shoulders","Abs","Chest")
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(this, android.R.layout.select_dialog_item, proposals)
        exercise_edit_text_category.threshold = 1 //will start working from first character
        exercise_edit_text_category.setAdapter(adapter)
    }

    private fun loadData(){
        val exercises = DatabaseHelper.getInstance(this).readBulk(Exercise::class, "%")
        for (exercise in exercises) {
            addExerciseToList(exercise,false)
        }
        reset()
    }

    override fun onButtonClicked(v: View) {
        when(v.id){
            R.id.exercise_button_image -> {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(
                    Intent.createChooser(
                        intent,
                        "Select Picture"
                    ), SELECT_PICTURE
                )
            }
            R.id.exercise_button_image_state -> {
                if (exerciseImage != null){
                    updateImage()
                }
            }
            R.id.exercise_button_add -> {
                if (StringHelper.hasText(exerciseTitle)){
                    val exercise = Exercise.create(exerciseTitle, exerciseDescription, exerciseImage)
                    addExerciseToList(exercise, true)
                    reset()
                }else{
                    oldHintColor = if (oldHintColor == null) exercise_edit_text_title.hintTextColors else oldHintColor
                    exercise_edit_text_title.setHintTextColor(Color.RED)
                    toast("Title cannot be empty!")
                }
            }
            R.id.exercise_button_toggle_lock -> toggleLock()
        }
    }

    private fun addExerciseToList(exercise: Exercise, write: Boolean) {
        val exerciseFragment = ExerciseFragment.create(exercise).addLockState(isLocked)
        supportFragmentManager.beginTransaction().add(
            exercise_layout_active.id,
            exerciseFragment
        ).commit()
        exerciseFragments.add(exerciseFragment)
        if (write){
            DatabaseHelper.getInstance(this).write(exercise.id, exercise)
        }
    }

    override fun onFragmentButtonClicked(view: View, parentId: Int) {
        when (view.id) {
            R.id.exercise_fragment_button_delete -> {
                deleteFragment(parentId)
            }
        }
    }

    private fun toggleLock() {
        isLocked = !isLocked
        exercise_button_toggle_lock.text = getText(if (isLocked) R.string.icon_lock else R.string.icon_unlock)
        for (fragment in exerciseFragments){
            fragment.toggleLock(isLocked)
        }
    }

    private fun deleteFragment(parentId: Int) {
        for (exerciseFragment in exerciseFragments) {
            if (exerciseFragment.parentLayout.id == parentId) {
                supportFragmentManager.beginTransaction().remove(
                    exerciseFragment
                ).commit()
                exerciseFragments.remove(exerciseFragment)
                DatabaseHelper.getInstance(this).delete(exerciseFragment.exercise.id,Exercise::class)
                return
            }
        }
    }

    private fun reset(complete: Boolean = false){
        updateImage()
        exerciseDescription = EMPTY
        exerciseTitle = EMPTY
        exercise_edit_text_title.setText(EMPTY)
        exercise_edit_text_description.setText(EMPTY)
        if (complete){
            if (oldHintColor != null){
                exercise_edit_text_title.setHintTextColor(oldHintColor)
            }
            notify = true
            isLocked = true
            silenceListener = false
            exerciseImage = null
            exerciseDescription = EMPTY
            exerciseTitle = EMPTY
            exerciseFragments.clear()
            oldHintColor = null
            exercise_layout_active.removeAllViews()
        }
        exercise_focus_sink.clearFocus()
        hideKeyboard(this)
    }

    private fun updateImage(bitmap: Bitmap? = null){
        if (bitmap == null){
            exerciseImage = null
            exercise_image_view_exercise.setImageBitmap(BitmapFactory.decodeResource(applicationContext.resources,R.drawable.workout))
            exercise_button_image_state.text = "No Image Selected"
        }else{
            exerciseImage = bitmap
            exercise_image_view_exercise.setImageBitmap(bitmap)
            exercise_button_image_state.text = "Tap To Remove"
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                val selectedImageUri = data!!.data
                if (selectedImageUri != null){
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImageUri)
                    updateImage(bitmap)
                }else{
                    updateImage()
                }
            }
        }
    }

    override fun onBackPressed() {
        reset(true);
        super.onBackPressed()
    }
}
