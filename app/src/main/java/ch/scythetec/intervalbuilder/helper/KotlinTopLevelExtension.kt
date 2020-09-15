package ch.scythetec.intervalbuilder.helper

import android.app.Activity
import android.content.Context
import android.database.Cursor
import android.widget.EditText
import android.widget.TextView
import ch.scythetec.intervalbuilder.const.Constants.Companion.FILE_TYPE_TXT
import ch.scythetec.intervalbuilder.const.Constants.Companion.EMPTY
import ch.scythetec.intervalbuilder.const.Constants.Companion.NULL_CLASS
import ch.scythetec.intervalbuilder.const.Constants.Companion.ONE
import ch.scythetec.intervalbuilder.const.Constants.Companion.ONE_I
import ch.scythetec.intervalbuilder.const.Constants.Companion.REFLECTION
import ch.scythetec.intervalbuilder.const.Constants.Companion.REPLACEMENT_TOKEN
import ch.scythetec.intervalbuilder.const.Constants.Companion.SPACE
import ch.scythetec.intervalbuilder.const.Constants.Companion.ZERO
import ch.scythetec.intervalbuilder.const.Constants.Companion.ZERO_I
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.lang.reflect.Field
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random

inline fun <reified INNER> array2d(sizeOuter: Int, sizeInner: Int, noinline innerInit: (Int)->INNER): Array<Array<INNER>> = Array(sizeOuter) { Array<INNER>(sizeInner, innerInit) }
fun array2dOfInt(sizeOuter: Int, sizeInner: Int): Array<IntArray> = Array(sizeOuter) { IntArray(sizeInner) }
fun array2dOfFloat(sizeOuter: Int, sizeInner: Int): Array<FloatArray> = Array(sizeOuter) { FloatArray(sizeInner) }
fun array2dOfDouble(sizeOuter: Int, sizeInner: Int): Array<DoubleArray> = Array(sizeOuter) { DoubleArray(sizeInner) }
fun array2dOfLong(sizeOuter: Int, sizeInner: Int): Array<LongArray> = Array(sizeOuter) { LongArray(sizeInner) }
fun array2dOfByte(sizeOuter: Int, sizeInner: Int): Array<ByteArray> = Array(sizeOuter) { ByteArray(sizeInner) }
fun array2dOfChar(sizeOuter: Int, sizeInner: Int): Array<CharArray> = Array(sizeOuter) { CharArray(sizeInner) }
fun array2dOfBoolean(sizeOuter: Int, sizeInner: Int): Array<BooleanArray> = Array(sizeOuter) { BooleanArray(sizeInner) }
fun floor(value: Double, precision: Int):Double = Math.floor(precision*value)/precision.toDouble()
fun EditText.getStringValue(): String = text.toString()
fun TextView.getStringValue(): String = text.toString()
fun Random.nextSafeInt(range: Int): Int = if (range<=0) 0 else nextInt(range)
fun Any.className(): String {
    val splits = this::class.toString().replace(REFLECTION,EMPTY).split(".")
    val s = splits[splits.size - 1]
    if (s.startsWith(NULL_CLASS) && !this::class.supertypes.isEmpty()){
        return this::class.supertypes[0].className()
    }
    return s
}
fun Any.readableClassName(delimiter: String = SPACE): String {
    val className = className()
    var readableClassName = ""
    for (c in 0 until className.length){
        if (c>0 && className[c].isUpperCase()){
            readableClassName += delimiter
        }
        readableClassName += className[c]
    }
    return readableClassName
}
fun Cursor.getBoolean(columnIndex: Int): Boolean{
    return getInt(columnIndex) == 1
}
fun countNonNull(vararg args: Any?):Int {
    var count = 0
    for (arg in args){
        if (arg != null){
            count++
        }
    }
    return count
}

@Suppress("UNCHECKED_CAST")
fun addToArrayBefore(array: Array<String>, vararg args: String): Array<String>{
    val newArray = arrayOfNulls<String?>(array.size+args.size)
    var i = 0
    for (t in args){
        newArray[i] = t
        i++
    }
    for (t in array){
        newArray[i] = t
        i++
    }
    return newArray as Array<String>
}

@Suppress("UNCHECKED_CAST")
fun <T: Any> addToArrayAfter(array: Array<T>, vararg args: T): Array<T>{
    val newArray: Array<T> = arrayOfNulls<Any>(array.size+args.size) as Array<T>
    var i = 0
    for (t in array){
        newArray[i] = t
        i++
    }
    for (t in args){
        newArray[i] = t
        i++
    }
    return newArray
}

fun ArrayList<*>.toStringArray(): Array<String>{
    val list = ArrayList<String>()
    for (i in 0 until size){
        list.add(get(i).toString())
    }
    return list.toTypedArray()
}

fun List<*>.toStringArray(): Array<String>{
    val list = ArrayList<String>()
    for (i in 0 until size){
        list.add(get(i).toString())
    }
    return list.toTypedArray()
}

fun String.isContainedIn(str: String?):Boolean{
    if (str == null) return false
    return str.contains(this)
}

fun File.isFileType(type: String): Boolean{
    if (isDirectory) return false
    return name.endsWith(type)
}

fun Activity.getIdByName(str: String, type: String = "string") = resources.getIdentifier(str, type, packageName)

fun Activity.getStringByName(str: String, vararg formatArgs: String):String {
    var txt = getString(getIdByName(str))
    for (id in 0 until formatArgs.size){
        txt = txt.replace("%${id+1}\$s",formatArgs[id])
    }
    return txt
}

fun getDrawableIdByName(context: Context, drawableName: String): Int {
    return context.resources.getIdentifier(drawableName, "drawable", context.packageName)
}


fun Activity.getGenericStringWithIdAndTemplate(id: Int, templateId: Int, vararg formatArgs: String):String {
    return getStringByName(getString(templateId,id),*formatArgs)
}

fun ArrayList<*>.removeFirst(){
    if (!isNullOrEmpty()){
        removeAt(0)
    }
}

fun <T: Any?> T?.notNull(f: ()-> Unit): T?{
    if (this != null){
        f()
    }
    return this
}

fun <T: Any> HashMap<T,Int>.addOne(key: T){
    this[key] = NumberHelper.nvl(this[key], ZERO_I).plus(ONE_I)
}

fun <K,V: Comparable<V>> Map<K,V>.sortByValue(): TreeMap<K, V> {
    val comparator = Comparator<K> { k1, k2 ->
        val compare = get(k1)!!.compareTo(get(k2)!!)
        if (compare == 0) 1 else compare
    }
    val sorted = TreeMap<K,V>(comparator)
    sorted.putAll(this)
    return sorted
}

fun <K,V: Comparable<V>> Map<K,V>.sort(): TreeMap<K, V> {
    val sorted = TreeMap<K,V>()
    sorted.putAll(this)
    return sorted
}

fun readTextFileFromAsset(context: Context, fileName: String): String{
    var reader: BufferedReader? = null
    try {
        reader = BufferedReader(InputStreamReader(context.assets.open(fileName+ FILE_TYPE_TXT)));

        val builder = StringBuilder()
        var line: String?
        do {
            line = reader.readLine()
            if (line != null){
                builder.append(line)
            }
        }while (line != null)
        return builder.toString()
    } catch (e: Exception) {
    } finally {
        if (reader != null) {
            try {
                reader.close()
            } catch (e: Exception) {
            }
        }
    }
    return EMPTY
}

fun String.replaceAllIgnoreCase(str: String, replaceWith: String): String{
    if (str == replaceWith){
        return this
    }
    var returnString = this
    val strLowerCase = str.lc()
    var thisLowerCase = this.lc()
    var replacement = replaceWith
    var identity = false
    if (replaceWith.lc().contains(strLowerCase)){
        replacement = REPLACEMENT_TOKEN
        identity = true
    }
    var index = thisLowerCase.indexOf(strLowerCase)
    while (index >= 0){
        returnString = returnString.replaceRange(index,index+str.length,replacement)
        thisLowerCase = thisLowerCase.replaceRange(index,index+str.length,replacement)
        index = thisLowerCase.indexOf(strLowerCase)
    }
    return if (identity) returnString.replace(REPLACEMENT_TOKEN, replaceWith) else returnString
}

fun String.lc(): String{
    return this.lc()
}

fun String.uc(): String{
    return this.uc()
}

fun Any.getAllDeclaredFields(): ArrayList<Field>{
    val fields = ArrayList<Field>()
    var currentClass: Class<in Any> = javaClass
    while(currentClass.superclass !=null){
        fields.addAll(currentClass.declaredFields)
        currentClass = currentClass.superclass as Class<in Any>
    }
    return fields
}