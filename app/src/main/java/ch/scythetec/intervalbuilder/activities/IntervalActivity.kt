package ch.scythetec.intervalbuilder.activities

import android.os.Bundle
import android.view.View
import android.widget.TextView
import ch.scythetec.intervalbuilder.R
import ch.scythetec.intervalbuilder.datamodel.IntervalSequenceList
import ch.scythetec.intervalbuilder.datamodel.links.ChainBuilder
import ch.scythetec.intervalbuilder.helper.DatabaseHelper
import ch.scythetec.intervalbuilder.helper.TimeHelper
import kotlinx.android.synthetic.main.activity_interval.*

var loadedIntervalSequenceList: IntervalSequenceList? = null
var chain: ChainBuilder.AbstractLink? = null
enum class PLAY_MODE{
    AUTOMATIC, MANUAL
}
var playMode: PLAY_MODE = PLAY_MODE.AUTOMATIC;

class IntervalActivity : AbstractBaseActivity() {
    override fun getConfiguredLayout(): Int {
        return R.layout.activity_interval
    }

    override fun getResizableTextViews(): Array<TextView> {
        return arrayOf()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_interval)
    }

    override fun onButtonClicked(v: View) {
        when (v.id) {
            R.id.interval_button_load -> {
                //TODO implement multi-load
                val intervalSequenceLists = DatabaseHelper.getInstance(this).readBulk(IntervalSequenceList::class, "%")
                if (intervalSequenceLists.isNotEmpty()) {
                    loadedIntervalSequenceList = intervalSequenceLists[0]
                }
                if (loadedIntervalSequenceList != null && loadedIntervalSequenceList!!.intervalSequences.size > 0){
                    val intervalSequencePart = loadedIntervalSequenceList!!.intervalSequences[0]
                    chain = ChainBuilder.build(loadedIntervalSequenceList!!.intervalSequences)
                    interval_image_exercise.setImageBitmap(intervalSequencePart.exercise?.image)
                    interval_text_title.text = intervalSequencePart.exercise?.title
                    interval_text_countdown_interval_value.text = TimeHelper.secondsToMinSecString(intervalSequencePart.timeInterval)
                    interval_text_countdown_rest_value.text = TimeHelper.secondsToMinSecString(intervalSequencePart.timeRest)
                    interval_text_countdown_repetitions_value.text = (intervalSequencePart.repetitions-1).toString()
                    if (loadedIntervalSequenceList != null && loadedIntervalSequenceList!!.intervalSequences.size > 0){
                        val nextIntervalSequenceFragment = loadedIntervalSequenceList!!.intervalSequences[1]
                        interval_text_countdown_next_value.text = nextIntervalSequenceFragment.exercise?.title
                    }
                }
            }
            R.id.interval_button_start -> {
                if (playMode == PLAY_MODE.AUTOMATIC){
                    if (chain != null){
                        if (interval_button_start.text == "Stop"){
                            chain = ChainBuilder.pauseTask()
                            interval_button_start.text = "Start"
                        }else{
                            ChainBuilder.createAndStartTask(chain!!, interval_text_countdown_interval_value, interval_text_countdown_rest_value, interval_text_countdown_repetitions_value, interval_text_title, interval_text_countdown_next_value, interval_image_exercise, interval_button_start)
                            interval_button_start.text = "Stop"
                        }
                    }else{
                        toast("Nothing loaded!")
                    }
                }else{
                    if (chain != null && chain?.next is ChainBuilder.Interval){
                        val interval: ChainBuilder.Interval = chain?.next as ChainBuilder.Interval;
                        val rest: ChainBuilder.Rest = interval.next as ChainBuilder.Rest;
                        val repetitions: ChainBuilder.Repetitions = rest.next as ChainBuilder.Repetitions
                        val nextInterval: ChainBuilder.Interval? = if (repetitions.next is ChainBuilder.Interval) repetitions.next as ChainBuilder.Interval else null
                        interval_text_countdown_interval_value.text = TimeHelper.secondsToMinSecString(interval.duration)
                        interval_text_countdown_rest_value.text = TimeHelper.secondsToMinSecString(rest.duration)
                        interval_text_countdown_repetitions_value.text = repetitions.duration.toString()
                        interval_text_title.text = interval.exercise?.title
                        interval_text_countdown_next_value.text = if (nextInterval == null) "Workout finished!" else nextInterval.exercise?.title
                        interval_image_exercise.setImageBitmap(interval.exercise?.image)
                        interval_button_start.text = "Next Exercise"
                        chain = repetitions
                    }else if (chain!= null && chain?.next is ChainBuilder.End){
                        interval_text_countdown_interval_value.text = ""
                        interval_text_countdown_rest_value.text = ""
                        interval_text_countdown_repetitions_value.text = ""
                        interval_text_title.text = ""
                        interval_text_countdown_next_value.text = ""
                        interval_image_exercise.setImageBitmap(null)
                        interval_button_start.text = "Finish Workout"
                        chain = null
                    }else if (interval_button_start.text == "Finish Workout"){
                        interval_button_start.text = "Start"
                        toast("Workout finished!")
                    }else{
                        toast("Nothing loaded!")
                    }
                }
            }
            R.id.interval_button_play_mode -> {
                if (playMode == PLAY_MODE.AUTOMATIC){
                    playMode = PLAY_MODE.MANUAL;
                    interval_button_play_mode.text = getString(R.string.icon_manual)
                    toast(R.string.manual_mode)
                }else{
                    playMode = PLAY_MODE.AUTOMATIC;
                    interval_button_play_mode.text = getString(R.string.icon_automatic)
                    toast(R.string.automatic_mode)
                }
            }
        }
    }

    override fun onBackPressed() {
        ChainBuilder.pauseTask()
        super.onBackPressed()
    }
}
