package ch.scythetec.intervalbuilder.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannedString
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import android.app.Activity
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import ch.scythetec.intervalbuilder.helper.DatabaseHelper


abstract class AbstractBaseActivity : AppCompatActivity() {

    abstract fun getConfiguredLayout(): Int

    abstract fun getResizableTextViews(): Array<TextView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)//will hide the intervalTitle
        supportActionBar?.hide() //hide the intervalTitle bar
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN) //show the activity in full screen
        setContentView(getConfiguredLayout())
        resizeText()
    }

    protected fun getSpannedStringFromId(id: Int): SpannedString {
        return getText(id) as SpannedString
    }

    protected fun resizeText() {
        val editTextSize = 3.5f * resources.displayMetrics.density
        for (textView in getResizableTextViews()){
//            textView.textSize = editTextSize
        }
    }

    fun hideKeyboard(activity: Activity = this) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = activity.currentFocus
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun toast(id: Int){
        toast(getString(id))
    }

    fun toast(text: String){
        Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT).show()
    }

    fun onFragmentButtonClicked(v: View) {
        var parentId: Int
        if (v.parent is ConstraintLayout) {
            parentId = (v.parent as ConstraintLayout).id
            onFragmentButtonClicked(v,parentId)
        }
    }

    open fun onFragmentButtonClicked(view: View, parentId: Int){}

    open fun onButtonClicked(view: View){}

    open fun onPopupWindowClicked(view: View) {}
}
