package ch.scythetec.intervalbuilder.helper

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import ch.scythetec.intervalbuilder.const.Constants.Companion.BOOLEAN
import ch.scythetec.intervalbuilder.const.Constants.Companion.DOUBLE
import ch.scythetec.intervalbuilder.const.Constants.Companion.EQUALS
import ch.scythetec.intervalbuilder.const.Constants.Companion.FLOAT
import ch.scythetec.intervalbuilder.const.Constants.Companion.HASH_MAP_ENTRY
import ch.scythetec.intervalbuilder.const.Constants.Companion.INT
import ch.scythetec.intervalbuilder.const.Constants.Companion.LONG
import ch.scythetec.intervalbuilder.const.Constants.Companion.STRING
import java.io.*
import kotlin.reflect.KClass
import kotlin.reflect.full.cast

class DataHelper {
    companion object {
        fun imageToByteArray(serializable: Serializable): ByteArray {
            val bos = ByteArrayOutputStream()
            return try {
                val oos = ObjectOutputStream(bos)
                oos.writeObject(serializable)
                oos.flush()
                val objectBytes: ByteArray = bos.toByteArray()
                bos.close()
                objectBytes
            } catch (e: Exception) {
                try {
                    bos.close()
                } catch (e: IOException) {
                    //NOP
                }
                ByteArray(0)
            }
        }

        fun imageToByteArray(bitmap: Bitmap): ByteArray{
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            return stream.toByteArray()
        }

        fun imageFromByteArray(byteArray: ByteArray?): Bitmap? {
            if (byteArray == null){
                return null
            }
            return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        }


        @Suppress("UNCHECKED_CAST")
        fun <T : Any> toObject(byteArray: ByteArray, clz: KClass<T>): T? {
            val bis = ByteArrayInputStream(byteArray)
            return try {
                val ois = ObjectInputStream(bis)
                val obj = ois.readObject()
                clz.cast(obj)
            } catch (e: Exception) {
                try {
                    bis.close()
                } catch (e: IOException) {
                    //NOP
                }
                return null
            }
        }

        fun parseString(stringValue: String, clazz: String): Any? {
            if (StringHelper.hasText(stringValue) && StringHelper.hasText(clazz)) {
                when (clazz) {
                    STRING -> return stringValue
                    INT -> return stringValue.toInt()
                    FLOAT -> return stringValue.toFloat()
                    DOUBLE -> return stringValue.toDouble()
                    BOOLEAN -> return stringValue.toBoolean()
                    LONG -> return stringValue.toLong()
                    HASH_MAP_ENTRY -> return stringValue.split(EQUALS)[0]
                }
            }
            return null
        }
    }
}