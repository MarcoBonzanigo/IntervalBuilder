package ch.scythetec.intervalbuilder.helper

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.*
import android.text.style.ForegroundColorSpan
import android.text.style.MetricAffectingSpan
import android.text.style.RelativeSizeSpan
import ch.scythetec.intervalbuilder.const.Constants.Companion.CARRIAGE_RETURN
import ch.scythetec.intervalbuilder.const.Constants.Companion.NEW_LINE
import ch.scythetec.intervalbuilder.const.Constants.Companion.EMPTY
import ch.scythetec.intervalbuilder.const.Constants.Companion.SPACE
import java.lang.RuntimeException
import kotlin.math.roundToInt


class StringHelper {

    companion object {
        fun extractNameFromClassString(className: String): String {
            val split = className.split(".")
            return split[split.size - 1]
        }

        fun hasText(text: Editable?): Boolean {
            if (text == null) return false
            return hasText(text.toString())
        }

        fun hasText(text: String?): Boolean {
            return (text != null && text.isNotBlank())
        }

        fun hasText(text: CharSequence?): Boolean {
            return (text != null && text.isNotBlank())
        }

        fun styleString(id: Int, context: Context): SpannableString {
            return styleStringInternal(
                context.getText(id) as SpannedString,
                Typeface.createFromAsset(context.assets, "FontAwesome900.otf")
            )
        }

        fun stripBlank(text: String): String {
            return text.replace(SPACE, EMPTY).replace(CARRIAGE_RETURN, EMPTY)
                .replace(NEW_LINE, EMPTY)
        }

        fun concatTokens(delimiter: String, vararg obj: String): String {
            if (!obj.isEmpty()) {
                var concat = ""
                for (o in obj) {
                    concat += (o + delimiter)
                }
                return concat.substring(0, concat.length - delimiter.length)
            }
            return EMPTY
        }

        private fun styleStringInternal(
            spannedString: SpannedString,
            typeface: Typeface?
        ): SpannableString {
            val annotations =
                spannedString.getSpans(0, spannedString.length, android.text.Annotation::class.java)
            val spannableString = SpannableString(spannedString)
            for (annotation in annotations) {
                if (annotation.key == "font") {
                    val fontName = annotation.value
                    if (fontName == "font_awesome") {
                        spannableString.setSpan(
                            CustomTypefaceSpan(typeface),
                            spannedString.getSpanStart(annotation),
                            spannedString.getSpanEnd(annotation),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                } else if (annotation.key == "size") {
                    if (!annotation.value.contains(",") || annotation.value.split(",").size != 2) {
                        throw RuntimeException("Wrong Size-Annotation value format. Needs to be sizeFloat,numberOfCharacters")
                    }
                    val sizeConfig = annotation.value.split(",")
                    spannableString.setSpan(
                        RelativeSizeSpan(sizeConfig[0].toFloat()),
                        0,
                        sizeConfig[1].toInt(),
                        0
                    )
                } else if (annotation.key == "color") {
                    if (!annotation.value.contains(",") || annotation.value.split(",").size != 2) {
                        throw RuntimeException("Wrong Color-Annotation value format. Needs to be colorHex,numberOfCharacters")
                    }
                    val colorConfig = annotation.value.split(",")
                    spannableString.setSpan(
                        ForegroundColorSpan(Color.parseColor(colorConfig[0])),
                        0,
                        colorConfig[1].toInt(),
                        0
                    )
                }
            }
            return spannableString
        }

        fun percentageToString(percentage: Double): CharSequence? {
            return ((percentage * 10000).roundToInt() /100).toString() + " %"
        }
    }

    class CustomTypefaceSpan(private val typeface: Typeface?) : MetricAffectingSpan() {

        override fun updateDrawState(drawState: TextPaint) {
            apply(drawState)
        }

        override fun updateMeasureState(paint: TextPaint) {
            apply(paint)
        }

        private fun apply(paint: Paint) {
            val oldTypeface = paint.typeface
            val oldStyle = if (oldTypeface != null) oldTypeface.style else 0
            val fakeStyle = oldStyle and typeface!!.style.inv()

            if (fakeStyle and Typeface.BOLD != 0) {
                paint.isFakeBoldText = true
            }

            if (fakeStyle and Typeface.ITALIC != 0) {
                paint.textSkewX = -0.25f
            }

            paint.typeface = typeface
        }
    }
}