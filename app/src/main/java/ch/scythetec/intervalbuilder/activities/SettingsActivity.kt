package ch.scythetec.intervalbuilder.activities

import android.os.Bundle
import android.widget.TextView
import ch.scythetec.intervalbuilder.R

class SettingsActivity : AbstractBaseActivity() {

    override fun getResizableTextViews(): Array<TextView> {
        return emptyArray()
    }

    override fun getConfiguredLayout(): Int {
        return R.layout.activity_settings;
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}
