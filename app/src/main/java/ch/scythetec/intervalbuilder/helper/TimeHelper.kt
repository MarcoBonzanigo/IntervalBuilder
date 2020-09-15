package ch.scythetec.intervalbuilder.helper


class TimeHelper {

    companion object {
        fun secondsToMinSecString(seconds0: Long): String {
            val seconds = if (seconds0 >= 3600) 2599 else seconds0
            val sec = seconds%60
            val min = (seconds - sec)/60
            return "$min Min $sec Sec"
        }
    }
}