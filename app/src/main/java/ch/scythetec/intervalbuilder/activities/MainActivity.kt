package ch.scythetec.intervalbuilder.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import ch.scythetec.intervalbuilder.R
import ch.scythetec.intervalbuilder.datamodel.Exercise
import ch.scythetec.intervalbuilder.helper.DatabaseHelper
import ch.scythetec.intervalbuilder.helper.PermissionHelper
import kotlinx.android.synthetic.main.activity_main.*
import ch.scythetec.intervalbuilder.helper.StringHelper
import kotlinx.android.synthetic.main.activity_exercise.*

class MainActivity : AbstractBaseActivity() {

    override fun getResizableTextViews(): Array<TextView> {
        return emptyArray()
    }

    override fun getConfiguredLayout(): Int {
        return R.layout.activity_main
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        main_button_builder.text = StringHelper.styleString(R.string.builder_icon,applicationContext)
        main_button_intervals.text = StringHelper.styleString(R.string.intervals_icon,applicationContext)
        main_button_exercises.text = StringHelper.styleString(R.string.workout_icon,applicationContext)
        main_button_settings.text = StringHelper.styleString(R.string.settings_icon,applicationContext)
        PermissionHelper.request(this, PermissionHelper.Companion.PermissionGroups.STORAGE)
    }

    override fun onButtonClicked(v: View) {
        when(v.id){
            R.id.main_button_builder -> {
                startActivity(Intent(this, BuilderActivity::class.java))
            }
            R.id.main_button_exercises -> {
                startActivity(Intent(this, ExerciseActivity::class.java))
            }
            R.id.main_button_intervals -> {
                startActivity(Intent(this, IntervalActivity::class.java))
            }
//            R.id.main_button_settings -> DatabaseHelper.getInstance(applicationContext).dropAndRecreateAll()
        }
    }
}
