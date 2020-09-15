package ch.scythetec.intervalbuilder.helper

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.*
import android.text.style.ForegroundColorSpan
import android.text.style.MetricAffectingSpan
import android.text.style.RelativeSizeSpan
import java.lang.RuntimeException


class ObjectHelper {

    companion object {
        fun <T: Any> nvl(value: T?, valueIfNull: T): T {
            return value ?: valueIfNull
        }
    }
}