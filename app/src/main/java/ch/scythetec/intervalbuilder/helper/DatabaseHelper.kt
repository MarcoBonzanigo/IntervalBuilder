package ch.scythetec.intervalbuilder.helper

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import ch.scythetec.intervalbuilder.const.Constants
import ch.scythetec.intervalbuilder.const.Constants.Companion.SHARED_PREFERENCES
import ch.scythetec.intervalbuilder.const.Constants.Companion.VERSIONING_IDENTIFIER
import ch.scythetec.intervalbuilder.datamodel.*
import ch.scythetec.intervalbuilder.datamodel.database.IntervalBuilderDatabase
import java.io.Serializable
import kotlin.reflect.KClass

class DatabaseHelper private constructor(context: Context) {
    enum class DataMode {
        PREFERENCES, DATABASE
    }

    private var mode: DataMode = DataMode.PREFERENCES
    private var database: IntervalBuilderDatabase? = null
    private var sharedPreferences: SharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE)

    fun disableNewVersioning(): DatabaseHelper {
        if (mode == DataMode.DATABASE && database != null){
            database?.disableNewVersion = true
        }
        return this
    }

    private fun enableNewVersioning(){
        if (mode == DataMode.DATABASE && database != null){
            database?.disableNewVersion = false
        }
    }

    init {
        if (PermissionHelper.check(context, PermissionHelper.Companion.PermissionGroups.STORAGE)) {
            database = IntervalBuilderDatabase.getInstance(context)
            mode = DataMode.DATABASE
        } else {
            mode = DataMode.PREFERENCES
        }
    }

    companion object {
        // Volatile: writes to this field are immediately made visible to other threads.
        @Volatile
        private var instance: DatabaseHelper? = null

        fun getInstance(context: Context): DatabaseHelper {
            return when {
                instance != null -> {
                    if ((instance!!.mode == DataMode.PREFERENCES && PermissionHelper.check(context,
                            PermissionHelper.Companion.PermissionGroups.STORAGE))){
                        instance = DatabaseHelper(context) // If permissions are granted, start over
                    }
                    instance!!
                }
                else -> synchronized(this) {
                    if (instance == null) {
                        instance = DatabaseHelper(context)
                    }
                    instance!!
                }
            }
        }
    }

    fun write(key: String, obj: Any, internalMode: DataMode = mode): Boolean {
        when (internalMode) {
            DataMode.PREFERENCES -> {
                if (obj is Boolean) sharedPreferences.edit().putBoolean(key, obj).apply()
                if (obj is String) sharedPreferences.edit().putString(key, obj).apply()
                if (obj is ByteArray) sharedPreferences.edit().putString(key, Base64.encodeToString(obj, Base64.DEFAULT)).apply()
                if (obj is Short) sharedPreferences.edit().putInt(key, obj.toInt()).apply()
                if (obj is Int) sharedPreferences.edit().putInt(key, obj).apply()
                if (obj is Long) sharedPreferences.edit().putLong(key, obj).apply()
                if (obj is Float) sharedPreferences.edit().putFloat(key, obj).apply()
                if (obj is Double) sharedPreferences.edit().putLong(key, java.lang.Double.doubleToLongBits(obj)).apply()
            }
            DataMode.DATABASE -> {
                database!!.openWritable()
                if (obj is Boolean) database!!.writeBoolean(key, obj)
                if (obj is String) database!!.writeString(key, obj)
                if (obj is ByteArray) database!!.writeByteArray(key, obj)
                if (obj is Short) database!!.writeShort(key, obj)
                if (obj is Int) database!!.writeInt(key, obj)
                if (obj is Long) database!!.writeLong(key, obj)
                if (obj is Float) database!!.writeFloat(key, obj)
                if (obj is Double) database!!.writeDouble(key, obj)
                if (obj is IntervalSequenceList) database!!.writeIntervalSequenceList(obj)
                if (obj is IntervalSequence) database!!.writeIntervalSequence(obj)
                if (obj is Exercise) database!!.writeExercise(obj)
                database!!.close()
            }
        }
        enableNewVersioning()
        return true
    }

    fun <T : Serializable> read(key: String, clz: KClass<T>, valIfNull: T = NullHelper.get(clz), internalMode: DataMode = mode): T {
        return when (internalMode) {
            DataMode.PREFERENCES -> readInternal(key,clz,valIfNull,internalMode)
            DataMode.DATABASE -> {
                database!!.openReadable()
                val readInternal = readInternal(key,clz,valIfNull,internalMode)
                database!!.close()
                readInternal
            }
        }
    }
    @Suppress("UNCHECKED_CAST")
    fun <T : Serializable> readInternal(key: String, clz: KClass<T>, valIfNull: T = NullHelper.get(clz), internalMode: DataMode = mode): T {
        try {
            when (internalMode) {
                DataMode.PREFERENCES -> {
                    if (String::class == clz) return sharedPreferences.getString(key, valIfNull as String) as T
                    if (ByteArray::class == clz) {
                        val byteArrayString: String? = sharedPreferences.getString(key, "")
                        if (StringHelper.hasText(byteArrayString)) {
                            return Base64.decode(byteArrayString, Base64.DEFAULT) as T
                        }
                        return valIfNull
                    }
                    if (Boolean::class == clz) return sharedPreferences.getBoolean(key, valIfNull as Boolean) as T
                    if (Short::class == clz) {
                        val intValue = sharedPreferences.getInt(key, 0)
                        return if (intValue == 0) valIfNull else intValue.toShort() as T
                    }
                    if (Int::class == clz) return sharedPreferences.getInt(key, valIfNull as Int) as T
                    if (Long::class == clz) return sharedPreferences.getLong(key, valIfNull as Long) as T
                    if (Float::class == clz) return sharedPreferences.getFloat(key, valIfNull as Float) as T
                    if (Double::class == clz) {
                        val rawLongBits: Long = sharedPreferences.getLong(key, 0L)
                        return if (rawLongBits == 0L) valIfNull else java.lang.Double.longBitsToDouble(rawLongBits) as T
                    }
                }
                DataMode.DATABASE -> {
                    if (Boolean::class == clz) return database!!.readBoolean(key, valIfNull as Boolean) as T
                    if (String::class == clz) return database!!.readString(key, valIfNull as String) as T
                    if (ByteArray::class == clz) return database!!.readByteArray(key, valIfNull as ByteArray) as T
                    if (Short::class == clz) return database!!.readShort(key, valIfNull as Short) as T
                    if (Int::class == clz) return database!!.readInt(key, valIfNull as Int) as T
                    if (Long::class == clz) return database!!.readLong(key, valIfNull as Long) as T
                    if (Float::class == clz) return database!!.readFloat(key, valIfNull as Float) as T
                    if (Double::class == clz) return database!!.readDouble(key, valIfNull as Double) as T
                    if (IntervalSequenceList::class == clz) return database!!.readIntervalSequenceList(key) as T
                    if (IntervalSequence::class == clz) return  database!!.readIntervalSequence(key) as T
                    if (Exercise::class == clz) return database!!.readExercise(key) as T
                }
            }
        } catch (e: Exception) {
        }
        return valIfNull
    }

    private fun <T : Serializable> readBinary(key: String, clz: KClass<T>, identifier: String, valIfNull: T): T {
        val bytes = read(identifier + key, ByteArray::class, ByteArray(0))
        if (bytes.isNotEmpty()) {
            val project = DataHelper.toObject(bytes, clz)
            return project ?: valIfNull
        }
        return valIfNull
    }

    fun <T : Serializable> readFull(key: String, clz: KClass<T>, internalMode: DataMode = mode): T? {
        return when (internalMode) {
            DataMode.PREFERENCES -> readFullInternal(key,clz,internalMode)
            DataMode.DATABASE -> {
                database!!.openReadable()
                val readFull = readFullInternal(key, clz, internalMode)
                database!!.close()
                readFull
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Serializable> readFullInternal(key: String, clz: KClass<T>, internalMode: DataMode = mode): T? {
        when (internalMode) {
            DataMode.PREFERENCES -> {
            }
            DataMode.DATABASE -> {
            }
        }
        return null
    }

    fun <T : Serializable> readBulk(clz: KClass<T>, key: Any?, fullLoad: Boolean = false, internalMode: DataMode = mode): List<T> {
        return when (internalMode) {
            DataMode.PREFERENCES -> readBulkInternal(clz,key,fullLoad,internalMode)
            DataMode.DATABASE -> {
                database!!.openReadable()
                val readBulkInternal = readBulkInternal(clz, key, fullLoad, internalMode)
                database!!.close()
                readBulkInternal
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Serializable> readBulkInternal(clz: KClass<T>, key: Any?, fullLoad: Boolean = false, internalMode: DataMode = mode): List<T> {
        when (internalMode) {
            DataMode.PREFERENCES -> {
            }
            DataMode.DATABASE -> {
                if (IntervalSequenceList::class == clz){
                    val list = ArrayList<IntervalSequenceList>()
                    val allIntervalSequenceListIds = database!!.readAllIntervalSequenceListIds()
                    for (intervalSequenceListId in allIntervalSequenceListIds){
                        list.add(database!!.readIntervalSequenceList(intervalSequenceListId))
                    }
                    return list as List<T>
                }
                if (Exercise::class == clz){
                    val list = ArrayList<Exercise>()
                    val allExerciseIds = database!!.readAllExerciseIds()
                    for (exerciseId in allExerciseIds){
                        list.add(database!!.readExercise(exerciseId))
                    }
                    return list as List<T>
                }
            }
        }
        return emptyList()
    }

    fun <T : Serializable> readAndMigrate(key: String, clz: KClass<T>, valIfNull: T, deleteInPrefs: Boolean = true): T {
        when (mode) {
            DataMode.PREFERENCES -> {
                return read(key, clz, valIfNull)
            }
            DataMode.DATABASE -> {
                val readPref = read(key, clz, valIfNull, DataMode.PREFERENCES)
                database!!.openReadable()
                val readDb = read(key, clz, valIfNull, DataMode.DATABASE)
                database!!.close()
                if (readPref == valIfNull) {
                    //Val was not saved, return DB value
                    return readDb
                }
                //Delete from Prefs if not specified otherwise
                if (deleteInPrefs) {
                    delete(key, clz, DataMode.PREFERENCES)
                }
                //Value was saved, check DB value
                if (readDb == valIfNull) {
                    //DB value does'nt exist, write to DB, return
                    database!!.openWritable()
                    write(key, readPref)
                    database!!.close()
                    return readPref
                }
                //DB value did exist, return
                return readDb
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Serializable> readBulkInternal(clz: KClass<T>, identifier: String): List<T> {
        val list = ArrayList<Serializable>()
        for (entry in sharedPreferences.all) {
            if (entry.key.contains(identifier)) {
                val bytes = Base64.decode((entry.value as String), Base64.DEFAULT)
                val element = DataHelper.toObject(bytes, clz)
                if (element != null) {
                    list.add(element)
                }
            }
        }
        return list as List<T>
    }

    // Versioning
    fun isNewerVersion(versionItem: IVersionItem): Boolean {
       return when (versionItem){
            else -> false
        }
    }
    fun isNewerVersionBulk(versionItems: List<IVersionItem>): HashMap<KClass<*>,Int> {
        val map = HashMap<KClass<*>,Int>()
        for (item in versionItems){
            val new = when (item){
                else -> false
            }
            if (new){
                map.addOne(item::class)
            }
        }
        return map
    }

    fun addVersioning(id: String, internalMode: DataMode = mode){
        when (internalMode){
            DataMode.PREFERENCES -> sharedPreferences.edit().putLong(VERSIONING_IDENTIFIER.plus(id),System.currentTimeMillis()).apply()
            DataMode.DATABASE -> {
                database!!.openWritable()
                database?.writeLong(VERSIONING_IDENTIFIER.plus(id),System.currentTimeMillis())
                database!!.close()
            }
        }
    }

    fun readVersioning(id: String, internalMode: DataMode = mode, openDatabase: Boolean = true): Long{
        return when (internalMode){
            DataMode.PREFERENCES -> sharedPreferences.getLong(VERSIONING_IDENTIFIER.plus(id),0)
            DataMode.DATABASE -> {
                if (openDatabase){
                    database!!.openReadable()
                }
                val l = NumberHelper.nvl(database?.readLong(VERSIONING_IDENTIFIER.plus(id), 0), 0)
                if (openDatabase){
                    database!!.close()
                }
                l
            }
        }
    }

    fun deleteVersioning(id: String, internalMode: DataMode = mode){
        when (internalMode){
            DataMode.PREFERENCES -> sharedPreferences.edit().remove(Constants.VERSIONING_IDENTIFIER.plus(id)).apply()
            DataMode.DATABASE -> {
                database!!.openWritable()
                database?.deleteNumber(VERSIONING_IDENTIFIER.plus(id))
                database!!.close()
            }
        }
    }


    fun deletePreferenceUids(uidKey: String) {
        for (entry in sharedPreferences.all.entries) {
            if (entry.key.startsWith(uidKey)) {
                sharedPreferences.edit().remove(entry.key).apply()
            }
        }
    }

    fun <T : Serializable> delete(key: String, clz: KClass<T>, internalMode: DataMode = mode) {
        when (internalMode) {
            DataMode.PREFERENCES -> {
                sharedPreferences.edit().remove(key).apply()
            }
            DataMode.DATABASE -> {
                database!!.openWritable()
                if (String::class == clz) {
                    database!!.deleteString(key)
                }
                if (ByteArray::class == clz) {
                    database!!.deleteData(key)
                }
                if (Exercise::class == clz) {
                    database!!.deleteExercise(key)
                }
                if (IntervalSequence::class == clz) {
                    database!!.deleteIntervalSequence(key)
                }
                if (IntervalSequenceList::class == clz) {
                    database!!.deleteIntervalSequenceList(key)
                }
                database!!.close()
            }
        }
    }

    fun recreateTableStatistics(){
        if (mode == DataMode.DATABASE){
            database!!.reCreateIndices()
        }
    }

    fun clear() {
        when (mode) {
            DataMode.PREFERENCES -> {
                sharedPreferences.edit().clear().apply()
            }
            DataMode.DATABASE -> {
                database!!.openWritable()
                database!!.truncateData()
                database!!.truncateNumbers()
                database!!.truncateStrings()
                database!!.truncateIntervalSequence()
                database!!.truncateIntervalSequenceList()
                database!!.truncateIntervalSequenceList()
                database!!.truncateExercise()
                database!!.close()
            }
        }
    }

    fun <T : Serializable> dropAndRecreate(kClass: KClass<T>) {
        database!!.openWritable()
        //TODO Implement
        database!!.close()
    }

    fun dropAndRecreateAll(){
        if (mode == DataMode.DATABASE) {
            database!!.openWritable()
            database!!.tables.forEach {  database!!.dropAndRecreateTable(it) }
            database!!.close()
        }
        for (pref in sharedPreferences.all){
            sharedPreferences.edit().remove(pref.key).apply()
        }
    }
}