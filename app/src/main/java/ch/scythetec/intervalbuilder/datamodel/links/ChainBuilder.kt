package ch.scythetec.intervalbuilder.datamodel.links

import android.graphics.Color
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.AsyncTask
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import ch.scythetec.intervalbuilder.datamodel.Exercise
import ch.scythetec.intervalbuilder.datamodel.IntervalSequence
import ch.scythetec.intervalbuilder.helper.TimeHelper
import ch.scythetec.intervalbuilder.helper.className
import java.lang.RuntimeException

var colorNormal: Int = Color.WHITE
var colorAlert: Int = Color.RED

class ChainBuilder() {
    class AsyncChainTask() : AsyncTask<AbstractLink, Void, AbstractLink>() {
        var currentChainLink: AbstractLink? = null
        var intervalView: TextView? = null
        var restView: TextView? = null
        var repetitionsView: TextView? = null
        var exerciseImage: ImageView? = null
        var startStopButton: Button? = null
        var titleExerciseView: TextView? = null
        var nextExerciseView: TextView? = null
        var switch = false

        public fun setUpdatableViews(intervalView: TextView, restView: TextView, repetitionsView: TextView, titleExerciseView: TextView,nextExerciseView: TextView, exerciseImage: ImageView, startStopButton: Button){
            this.intervalView = intervalView
            this.restView = restView
            this.repetitionsView = repetitionsView
            this.exerciseImage = exerciseImage
            this.startStopButton = startStopButton
            this.titleExerciseView = titleExerciseView
            this.nextExerciseView = nextExerciseView
        }

        override fun doInBackground(vararg params: AbstractLink?): AbstractLink? {
            if (params.isNotEmpty()){
                currentChainLink = params[0]
                while (currentChainLink !is End){
                    publishProgress()
                    val newCurrentChainLink = currentChainLink?.tick()
                    switch = !newCurrentChainLink?.className().equals(currentChainLink?.className())
                    currentChainLink = newCurrentChainLink
                }
                publishProgress()
            }
            return currentChainLink
        }

        override fun onProgressUpdate(vararg values: Void?) {
            super.onProgressUpdate(*values)
            updateViews()
        }

        private fun updateViews() {
            when (currentChainLink) {
                is Begin -> startStopButton?.text = "Stop"
                is Interval -> {
                    if (switch){
                        titleExerciseView?.text = (currentChainLink as Interval).exercise?.title
                        titleExerciseView?.setTextColor(colorNormal)
                        exerciseImage?.setImageBitmap((currentChainLink as Interval).exercise?.image)
                        if (currentChainLink?.next?.next?.next is Interval){
                            nextExerciseView?.text = (currentChainLink?.next?.next?.next  as Interval).exercise?.title
                        }
                        intervalView?.setTextColor(colorAlert)
                        restView?.setTextColor(colorNormal)
                        repetitionsView?.setTextColor(colorNormal)
                        nextExerciseView?.setTextColor(colorNormal)
                    }
                    intervalView?.text = currentChainLink?.getText()
                }
                is Rest -> {
                    if (switch){
                        intervalView?.setTextColor(colorNormal)
                        restView?.setTextColor(colorAlert)
                        if ((currentChainLink?.next as Repetitions).duration <= 1 && currentChainLink?.next?.next is Interval){
                            //only change image if no repetitions remain
                            val exercise = (currentChainLink?.next?.next as Interval).exercise
                            exerciseImage?.setImageBitmap(exercise?.image)
                            titleExerciseView?.text = "Next: " + exercise?.title
                            titleExerciseView?.setTextColor(colorAlert)
                        }
                    }
                    restView?.text = currentChainLink?.getText()
                }
                is Repetitions -> {
                    if (currentChainLink!!.duration >= 1L){
                        restView?.text = (currentChainLink as Repetitions).rest?.getText()
                        intervalView?.text = (currentChainLink as Repetitions).interval?.getText()
                        repetitionsView?.text = currentChainLink?.getText()
                    }else if (currentChainLink?.next?.next?.next  is Repetitions){
                        val nextRepetitions: Repetitions = currentChainLink?.next?.next?.next as Repetitions
                        restView?.text = nextRepetitions.rest?.getText()
                        intervalView?.text = nextRepetitions.interval?.getText()
                        repetitionsView?.text = nextRepetitions.getText()
                    }
                    intervalView?.setTextColor(colorNormal)
                    restView?.setTextColor(colorNormal)
                    repetitionsView?.setTextColor(colorAlert)
                }
                is End -> {
                    repetitionsView?.setTextColor(colorNormal)
                    startStopButton?.text = "Start"
                }
            }
        }

        private fun playSound(){
            val toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
            toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
        }
    }

    companion object {
        var task: AsyncChainTask? = null

        public fun build(intervalSequences: ArrayList<IntervalSequence>): AbstractLink{
            var chain: AbstractLink = End()
            if (intervalSequences.isEmpty()){
                return chain
            }
            for (sequence in 1 .. intervalSequences.size){
                chain = addLinks(chain,intervalSequences[intervalSequences.size - sequence])
            }
            chain = Begin(chain)
            chain.print()
            return chain
        }

        private fun addLinks(link: AbstractLink, intervalSequence: IntervalSequence): AbstractLink{
            val repetitions = Repetitions(intervalSequence.repetitions, intervalSequence.position, link)
            val rest = Rest(intervalSequence.timeRest, intervalSequence.position, repetitions)
            val interval = Interval(intervalSequence.exercise, intervalSequence.timeInterval, intervalSequence.position, rest)
            repetitions.setIntervalAndRest(interval,rest)
            return interval
        }

        public fun createAndStartTask(chain: AbstractLink, intervalView: TextView, restView: TextView, repetitionsView: TextView, titleExerciseView: TextView, nextExerciseView: TextView, exerciseImage: ImageView, startStopButton: Button){
            task = AsyncChainTask()
            task?.setUpdatableViews(intervalView,restView,repetitionsView,titleExerciseView,nextExerciseView,exerciseImage,startStopButton)
            task?.execute(chain)
        }

        public fun pauseTask(): AbstractLink?{
            task?.cancel(true)
            val currentChainLink = task?.currentChainLink
            task = null
            return currentChainLink?.pause()
        }
    }


    abstract class AbstractLink(var duration: Long, val position: Int, val next: AbstractLink?) {
        private val originalDuration = duration

        abstract fun print()

        public fun tick(suppressWait: Boolean = false): AbstractLink?{
            if (this is Repetitions){
                return if (duration > 1){
                    duration--
                    interval?.reset()
                    rest?.reset()
                    if (!suppressWait){
                        Thread.sleep(1000)
                    }
                    interval
                }else{
                    duration--
                    if (!suppressWait){
                        Thread.sleep(1000)
                    }
                    next
                }
            }else{
                duration--
                if (duration<=0){
                    try{
                        val toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
                        toneGen1.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 150)
                    }catch(e: RuntimeException){
                        //NOP
                    }
                    return next
                }else if (duration <= 3){
                    try{
                    val toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
                    toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
                    }catch(e: RuntimeException){
                        //NOP
                    }
                }
                if (!suppressWait){
                    Thread.sleep(1000)
                }
                return this
            }
        }

        public fun reset(){
            duration = originalDuration
        }

        public fun pause(): AbstractLink {
            if (!(this is Repetitions)){
                duration++
            }
            return this
        }

        abstract fun getText(): String
    }

    class Begin(next: AbstractLink?) : AbstractLink(0, 0, checkNotNull(next)){
        override fun print() {
            Log.i("Interval-Chain: ","Begin")
            next?.print()
        }

        override fun getText(): String {
            return "Workout Finished!"
        }
    }

    class Interval(val exercise: Exercise?, duration: Long,  position: Int, next: AbstractLink?) : AbstractLink(duration,  position, checkNotNull(next)){
        override fun print() {
            val exerciseText = if (exercise==null) "None" else exercise.title
            Log.i("Interval-Chain: ","Interval with $duration seconds duration and the following fix exercise: $exerciseText")
            next?.print()
        }

        override fun getText(): String {
            return TimeHelper.secondsToMinSecString(duration)
        }
    }

    class Rest(duration: Long, position: Int, next: AbstractLink?) : AbstractLink(duration,  position, checkNotNull(next)){
        override fun print() {
            Log.i("Interval-Chain: ","Rest with $duration seconds duration")
            next?.print()
        }

        override fun getText(): String {
            return TimeHelper.secondsToMinSecString(duration)
        }
    }

    class Repetitions(count: Long, position: Int, next: AbstractLink?) : AbstractLink(count,  position, checkNotNull(next)){
        var interval: Interval? = null
        var rest: Rest? = null
        fun setIntervalAndRest(interval: Interval,rest: Rest){
            this.interval = interval
            this.rest = rest
        }

        override fun print() {
            Log.i("Interval-Chain: ","$duration Repetitions")
            next?.print()
        }

        override fun getText(): String {
            return (duration-1).toString()
        }
    }

    class End() : AbstractLink(0, -1,null){
        override fun print() {
            Log.i("Interval-Chain: ","End")
        }

        override fun getText(): String {
            return "Workout Finished!"
        }
    }
}