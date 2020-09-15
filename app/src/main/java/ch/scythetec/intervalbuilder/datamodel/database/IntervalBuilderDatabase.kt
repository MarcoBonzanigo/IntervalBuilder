package ch.scythetec.intervalbuilder.datamodel.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.core.database.getStringOrNull
import ch.scythetec.intervalbuilder.const.Constants
import ch.scythetec.intervalbuilder.const.Constants.Companion.COMMA
import ch.scythetec.intervalbuilder.const.Constants.Companion.EMPTY
import ch.scythetec.intervalbuilder.const.Constants.Companion.GROUND_DASH
import ch.scythetec.intervalbuilder.const.Constants.Companion.LIKE
import ch.scythetec.intervalbuilder.const.Constants.Companion.SEMI_COLON
import ch.scythetec.intervalbuilder.const.Constants.Companion.SINGLE_QUOTE
import ch.scythetec.intervalbuilder.const.Constants.Companion.VERSIONING_IDENTIFIER
import ch.scythetec.intervalbuilder.const.Constants.Companion.WILDCARD
import ch.scythetec.intervalbuilder.datamodel.Exercise
import ch.scythetec.intervalbuilder.datamodel.IntervalSequence
import ch.scythetec.intervalbuilder.datamodel.IntervalSequenceList
import ch.scythetec.intervalbuilder.helper.*
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class IntervalBuilderDatabase private constructor(val context: Context) : AbstractDatabase() {
    private var dbHelper: DbHelper
    var disableNewVersion = false
    private var db: SQLiteDatabase? = null
    private val activeCursors = ArrayList<Cursor>()

    init {
         val databaseVersion = 1
         val databaseName = "IntervalBuilderDatabase"
         val databaseEnding = ".db"
         val fileDir: String = Constants.FOLDER_ROOT.plus(
            Constants.FOLDER_DATABASE
        )
        val path = context.getExternalFilesDir(null).toString() + fileDir + File.separator + databaseName + databaseEnding
        val file = File(path)
        if (!file.exists() && !file.parentFile.exists()){
            file.parentFile.mkdirs()
        }
        dbHelper = DbHelper(context,path,databaseVersion)
    }

    fun openReadable(){
        if (db == null){
            db = dbHelper.readableDatabase
        }
    }

    fun openWritable(){
        if (db == null){
        db = dbHelper.writableDatabase
        }
    }

    fun close(){
        if (db != null) {
            if (db!!.inTransaction()){
                db?.endTransaction()
            }
            for (cursor in activeCursors){
                if (!cursor.isClosed){
                    cursor.close()
                }
            }
            activeCursors.clear()
            db?.close()
            db = null
        }
    }

    private fun getDb(): SQLiteDatabase{
        return db!!
    }

    val map = TreeMap<Long,String>()
    var lastInsert = 0L
    var lastStatement = EMPTY
    private fun getCursor(table: String, columns: Array<String>, selection: String, selectionArgs: Array<String>?,
                          groupBy: String?, having: String?, orderBy: String?, limit: String?): Cursor {
        val cursor = getDb().query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit)
        if (lastInsert != 0L){
            map[System.currentTimeMillis()-lastInsert] = lastStatement
        }
        lastInsert = System.currentTimeMillis()
        lastStatement = table+selection
        activeCursors.add(cursor)
        return cursor
    }

    companion object {
        // Volatile: writes to this field are immediately made visible to other threads.
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: IntervalBuilderDatabase? = null

        fun getInstance(context: Context): IntervalBuilderDatabase {
            return when {
                instance != null -> instance!!
                else -> synchronized(this) {
                    if (instance == null) {
                        instance = IntervalBuilderDatabase(context)
                    }
                    instance!!
                }
            }
        }
    }

    /** WRITE    **
     ** TO       **
     ** DATABASE **/
    fun writeByteArray(key: String, value: ByteArray): Long {
        val values = ContentValues()
        values.put(DataTableEntry.KEY, key)
        values.put(DataTableEntry.VALUE, value)
        return insert(DataTableEntry.TABLE_NAME, DataTableEntry.KEY, key, values)
    }

    fun writeString(key: String, value: String): Long {
        val values = ContentValues()
        values.put(TextTableEntry.KEY, key)
        values.put(TextTableEntry.VALUE, value)
        return insert(TextTableEntry.TABLE_NAME, TextTableEntry.KEY, key, values)
    }

    fun writeBoolean(key: String, value: Boolean): Long {
        val values = ContentValues()
        values.put(DataTableEntry.KEY, key)
        values.put(DataTableEntry.VALUE, value)
        return insert(NumberTableEntry.TABLE_NAME, NumberTableEntry.KEY, key, values)
    }

    fun writeShort(key: String, value: Short): Long {
        val values = ContentValues()
        values.put(NumberTableEntry.KEY, key)
        values.put(NumberTableEntry.VALUE, value)
        return insert(NumberTableEntry.TABLE_NAME, NumberTableEntry.KEY, key, values)
    }

    fun writeInt(key: String, value: Int): Long {
        val values = ContentValues()
        values.put(NumberTableEntry.KEY, key)
        values.put(NumberTableEntry.VALUE, value)
        return insert(NumberTableEntry.TABLE_NAME, NumberTableEntry.KEY, key, values)
    }

    fun writeLong(key: String, value: Long): Long {
        val values = ContentValues()
        values.put(NumberTableEntry.KEY, key)
        values.put(NumberTableEntry.VALUE, value)
        return insert(NumberTableEntry.TABLE_NAME, NumberTableEntry.KEY, key, values)
    }

    fun writeFloat(key: String, value: Float): Long {
        val values = ContentValues()
        values.put(NumberTableEntry.KEY, key)
        values.put(NumberTableEntry.VALUE, value)
        return insert(NumberTableEntry.TABLE_NAME, NumberTableEntry.KEY, key, values)
    }

    fun writeDouble(key: String, value: Double): Long {
        val values = ContentValues()
        values.put(NumberTableEntry.KEY, key)
        values.put(NumberTableEntry.VALUE, value)
        return insert(NumberTableEntry.TABLE_NAME, NumberTableEntry.KEY, key, values)
    }

    fun writeIntervalSequenceList(intervalSequenceList: IntervalSequenceList): Long {
        val values = ContentValues()
        for (intervalSequence in intervalSequenceList.intervalSequences){
            writeIntervalSequence(intervalSequence)
        }
        values.put(IntervalSequenceListTableEntry.ID, intervalSequenceList.id)
        values.put(IntervalSequenceListTableEntry.TITLE, intervalSequenceList.title)
        values.put(IntervalSequenceListTableEntry.SEQUENCE_ID_LIST, intervalSequenceList.getSequenceListAsString())
        return insert(IntervalSequenceListTableEntry.TABLE_NAME, IntervalSequenceListTableEntry.ID, intervalSequenceList.id, values)
    }

    fun writeIntervalSequence(intervalSequence: IntervalSequence): Long {
        val values = ContentValues()
        values.put(IntervalSequenceTableEntry.ID, intervalSequence.id)
        values.put(IntervalSequenceTableEntry.TIME_INTERVAL, intervalSequence.timeInterval)
        values.put(IntervalSequenceTableEntry.TIME_REST, intervalSequence.timeRest)
        values.put(IntervalSequenceTableEntry.REPETITIONS, intervalSequence.repetitions)
        values.put(IntervalSequenceTableEntry.EXERCISE_ID, intervalSequence.exercise?.id)
        values.put(IntervalSequenceTableEntry.POSITION, intervalSequence.position)
        return insert(IntervalSequenceTableEntry.TABLE_NAME, IntervalSequenceTableEntry.ID, intervalSequence.id, values)
    }

    fun writeExercise(exercise: Exercise): Long {
        val values = ContentValues()
        values.put(ExerciseTableEntry.ID, exercise.id)
        values.put(ExerciseTableEntry.TITLE, exercise.title)
        values.put(ExerciseTableEntry.DESCRIPTION, exercise.description)
        values.put(ExerciseTableEntry.CATEGORY, exercise.category)
        values.put(ExerciseTableEntry.IMAGE, if (exercise.image != null) DataHelper.imageToByteArray(exercise.image!!) else null)
        return insert(ExerciseTableEntry.TABLE_NAME, ExerciseTableEntry.ID, exercise.id, values)
    }

    /** READ     **
     ** FROM     **
     ** DATABASE **/
    fun readByteArray(key: String, valueIfNull: ByteArray): ByteArray {
        val cursor = getCursor(DataTableEntry.TABLE_NAME, arrayOf(DataTableEntry.VALUE), DataTableEntry.KEY + LIKE + SINGLE_QUOTE + key + SINGLE_QUOTE, null, null, null, null, null)
        var bytes: ByteArray? = null
        if (cursor.moveToFirst()) {
            bytes = cursor.getBlob(0)
        }
        cursor.close()
        return ObjectHelper.nvl(bytes, valueIfNull)
    }

    fun readString(key: String, valueIfNull: String): String {
        val cursor = getCursor(TextTableEntry.TABLE_NAME, arrayOf(TextTableEntry.VALUE), TextTableEntry.KEY + LIKE + SINGLE_QUOTE + key + SINGLE_QUOTE, null, null, null, null, null)
        var d: String? = null
        if (cursor.moveToFirst()) {
            d = cursor.getString(0)
        }
        cursor.close()
        return ObjectHelper.nvl(d, valueIfNull)
    }

    fun readBoolean(key: String, valueIfNull: Boolean): Boolean {
        val cursor = getCursor(NumberTableEntry.TABLE_NAME, arrayOf(NumberTableEntry.VALUE), NumberTableEntry.KEY + LIKE + SINGLE_QUOTE + key + SINGLE_QUOTE, null, null, null, null, null)
        var b: Int? = null
        if (cursor.moveToFirst()) {
            b = cursor.getInt(0)
        }
        cursor.close()
        return ObjectHelper.nvl(NumberHelper.nvl(b, 0) == 1, valueIfNull)
    }

    fun readShort(key: String, valueIfNull: Short): Short {
        val cursor = getCursor(NumberTableEntry.TABLE_NAME, arrayOf(NumberTableEntry.VALUE), NumberTableEntry.KEY + LIKE + SINGLE_QUOTE + key + SINGLE_QUOTE, null, null, null, null, null)
        var s: Short? = null
        if (cursor.moveToFirst()) {
            s = cursor.getShort(0)
        }
        cursor.close()
        return NumberHelper.nvl(s, valueIfNull)
    }

    fun readInt(key: String, valueIfNull: Int): Int {
        val cursor = getCursor(NumberTableEntry.TABLE_NAME, arrayOf(NumberTableEntry.VALUE), NumberTableEntry.KEY + LIKE + SINGLE_QUOTE + key + SINGLE_QUOTE, null, null, null, null, null)
        var i: Int? = null
        if (cursor.moveToFirst()) {
            i = cursor.getInt(0)
        }
        cursor.close()
        return NumberHelper.nvl(i, valueIfNull)
    }

    fun readLong(key: String, valueIfNull: Long): Long {
        val cursor = getCursor(NumberTableEntry.TABLE_NAME, arrayOf(NumberTableEntry.VALUE), NumberTableEntry.KEY + LIKE + SINGLE_QUOTE + key + SINGLE_QUOTE, null, null, null, null, null)
        var l: Long? = null
        if (cursor.moveToFirst()) {
            l = cursor.getLong(0)
        }
        cursor.close()
        return NumberHelper.nvl(l, valueIfNull)
    }

    fun readFloat(key: String, valueIfNull: Float): Float {
        val cursor = getCursor(NumberTableEntry.TABLE_NAME, arrayOf(NumberTableEntry.VALUE), NumberTableEntry.KEY + LIKE + SINGLE_QUOTE + key + SINGLE_QUOTE, null, null, null, null, null)
        var f: Float? = null
        if (cursor.moveToFirst()) {
            f = cursor.getFloat(0)
        }
        cursor.close()
        return NumberHelper.nvl(f, valueIfNull)
    }

    fun readDouble(key: String, valueIfNull: Double): Double {
        val cursor = getCursor(NumberTableEntry.TABLE_NAME, arrayOf(NumberTableEntry.VALUE), NumberTableEntry.KEY + LIKE + SINGLE_QUOTE + key + SINGLE_QUOTE, null, null, null, null, null)
        var d: Double? = null
        if (cursor.moveToFirst()) {
            d = cursor.getDouble(0)
        }
        cursor.close()
        return NumberHelper.nvl(d, valueIfNull)
    }

    fun readAllIntervalSequenceListIds(): List<String> {
        val list = ArrayList<String>()
        val cursor = getCursor(
            IntervalSequenceListTableEntry.TABLE_NAME,
            arrayOf(
                IntervalSequenceListTableEntry.ID
            ),
            IntervalSequenceListTableEntry.ID + LIKE + WILDCARD  , null, null, null, null, null)
        while (cursor.moveToNext()){
           list.add(cursor.getString(0))
        }
        cursor.close()
        return list
    }

    fun readIntervalSequenceList(id: String): IntervalSequenceList {
        val cursor = getCursor(
            IntervalSequenceListTableEntry.TABLE_NAME,
            arrayOf(
                IntervalSequenceListTableEntry.ID,
                IntervalSequenceListTableEntry.TITLE,
                IntervalSequenceListTableEntry.SEQUENCE_ID_LIST
            ),
            IntervalSequenceListTableEntry.ID + LIKE + SINGLE_QUOTE + id + SINGLE_QUOTE, null, null, null, null, null)
        val intervalSequences = ArrayList<IntervalSequence>()
        var intervalSequenceList: IntervalSequenceList? = null
        if (cursor.moveToFirst()) {
            for (intervalSequence in cursor.getString(2).split(SEMI_COLON)){
                intervalSequences.add(readIntervalSequence(intervalSequence))
            }
            intervalSequenceList = IntervalSequenceList.create(
                cursor.getString(0),
                cursor.getString(1),
                intervalSequences
            )
        }
        cursor.close()
        return ObjectHelper.nvl(intervalSequenceList,IntervalSequenceList.NullIntervalSequenceList.get())
    }

    fun readIntervalSequence(id: String): IntervalSequence {
        val cursor = getCursor(
            IntervalSequenceTableEntry.TABLE_NAME,
            arrayOf(
                IntervalSequenceTableEntry.ID,
                IntervalSequenceTableEntry.TIME_INTERVAL,
                IntervalSequenceTableEntry.TIME_REST,
                IntervalSequenceTableEntry.REPETITIONS,
                IntervalSequenceTableEntry.EXERCISE_ID,
                IntervalSequenceTableEntry.POSITION
            ),
            IntervalSequenceTableEntry.ID + LIKE + SINGLE_QUOTE + id + SINGLE_QUOTE, null, null, null, null, null)
        var intervalSequence: IntervalSequence? = null
        if (cursor.moveToFirst()) {
            val exerciseId = cursor.getStringOrNull(4)
            var exercise: Exercise? = null
            if (exerciseId != null) {
                exercise = readExercise(exerciseId)
            }
            intervalSequence = IntervalSequence.create(
                cursor.getLong(1),
                cursor.getLong(2),
                cursor.getLong(3),
                exercise,
                cursor.getInt(5),
                cursor.getString(0)
             )
        }
        cursor.close()
        return ObjectHelper.nvl(intervalSequence,IntervalSequence.NullIntervalSequence.get())
    }


    fun readAllExerciseIds(): List<String> {
        val list = ArrayList<String>()
        val cursor = getCursor(
            ExerciseTableEntry.TABLE_NAME,
            arrayOf(
                ExerciseTableEntry.ID
            ),
            ExerciseTableEntry.ID + LIKE + WILDCARD  , null, null, null, null, null)
        while (cursor.moveToNext()){
            list.add(cursor.getString(0))
        }
        cursor.close()
        return list
    }

    fun readExercise(id: String): Exercise {
        val cursor = getCursor(
            ExerciseTableEntry.TABLE_NAME,
            arrayOf(
                ExerciseTableEntry.ID,
                ExerciseTableEntry.TITLE,
                ExerciseTableEntry.DESCRIPTION,
                ExerciseTableEntry.CATEGORY,
                ExerciseTableEntry.IMAGE
            ),
            ExerciseTableEntry.ID + LIKE + SINGLE_QUOTE + id + SINGLE_QUOTE, null, null, null, null, null)
        var exercise: Exercise? = null
        if (cursor.moveToFirst()) {
            exercise = Exercise.create(
                cursor.getString(1),
                cursor.getString(2),
                cursor.getString(3),
                DataHelper.imageFromByteArray(cursor.getBlob(4)),
                cursor.getString(0))
        }
        cursor.close()
        return ObjectHelper.nvl(exercise,Exercise.NullExercise.get())
    }

    private fun addVersioning(id: String, oldTimeStamp: Long): Long {
        var time = oldTimeStamp
        if (disableNewVersion && oldTimeStamp != 0L) {
            writeLong(VERSIONING_IDENTIFIER.plus(id), oldTimeStamp)
        } else {
            writeLong(VERSIONING_IDENTIFIER.plus(id), System.currentTimeMillis())
            time = System.currentTimeMillis()
        }
        return time
    }

    private fun readVersioning(id: String): Long {
        return readLong(VERSIONING_IDENTIFIER.plus(id), 0)
    }

    private fun deleteVersioning(id: String) {
        deleteNumber(VERSIONING_IDENTIFIER.plus(id))
    }

    /** DELETE     **
     ** FROM     **
     ** DATABASE **/
    fun deleteNumber(key: String) {
        delete(NumberTableEntry.TABLE_NAME, NumberTableEntry.KEY, key)
    }

    fun deleteString(key: String) {
        delete(TextTableEntry.TABLE_NAME, TextTableEntry.KEY, key)
    }

    fun deleteData(key: String) {
        delete(DataTableEntry.TABLE_NAME, DataTableEntry.KEY, key)
    }

    fun deleteExercise(key: String) {
        delete(ExerciseTableEntry.TABLE_NAME, ExerciseTableEntry.ID, key)
    }

    fun deleteIntervalSequence(key: String) {
        delete(IntervalSequenceTableEntry.TABLE_NAME, IntervalSequenceTableEntry.ID, key)
    }

    fun deleteIntervalSequenceList(key: String) {
        delete(IntervalSequenceListTableEntry.TABLE_NAME, IntervalSequenceListTableEntry.ID, key)
    }

    fun truncateStrings() {
        truncate(TextTableEntry.TABLE_NAME)
    }

    fun truncateNumbers() {
        truncate(NumberTableEntry.TABLE_NAME)
    }

    fun truncateData() {
        truncate(DataTableEntry.TABLE_NAME)
    }

    fun truncateIntervalSequence(){
        truncate(IntervalSequenceTableEntry.TABLE_NAME)
    }

    fun truncateIntervalSequenceList(){
        truncate(IntervalSequenceListTableEntry.TABLE_NAME)
    }

    fun truncateExercise(){
        truncate(ExerciseTableEntry.TABLE_NAME)
    }

    /** INTERNAL **/
    private fun insert(tableName: String, keyColumn: String, key: String, values: ContentValues): Long {
        delete(tableName, keyColumn, key)
        return getDb().insert(tableName, "null", values)
    }

    private fun delete(tableName: String, keyColumn: String, key: String) {
        val selection = String.format(" %s LIKE ? ",keyColumn)
        getDb().delete(tableName, selection, arrayOf(key))
    }

    private fun truncate(tableName: String) {
        getDb().delete(tableName, null, null)
    }

    fun dropAndRecreateTable(table: String?) {
        if (table == null){
            return
        }
        val collectTables = collectTableStatements(table)
        if (collectTables.isNotEmpty()) {
            openWritable()
            for (statement in collectTables) {
                getDb().execSQL(statement)
            }
            close()
        }
    }

    fun reCreateIndices() {
        reCreateIndicesInternal(
                IndexCreationWrapper(TextTableEntry.TABLE_NAME,arrayOf(TextTableEntry.KEY)),
                IndexCreationWrapper(NumberTableEntry.TABLE_NAME,arrayOf(NumberTableEntry.KEY)),
                IndexCreationWrapper(DataTableEntry.TABLE_NAME,arrayOf(DataTableEntry.KEY))
        )
    }

    private fun reCreateIndicesInternal(vararg indexWrapper: IndexCreationWrapper){
        openWritable()
        try{
        for (wrapper in indexWrapper){
            getDb().execSQL(wrapper.getDeletionSql())
            getDb().execSQL(wrapper.getCreationSql())
            getDb().execSQL(wrapper.getStatisticsSql())
        }
        }catch (e: Exception){
            //NOP
        }
    }

    class IndexCreationWrapper(tableName: String, columnNames: Array<String>){

        private var tableNameProcessed = StringHelper.stripBlank(tableName).uc()
        private var columnNamesProcessed = Array(columnNames.size) { columnIndex -> StringHelper.stripBlank(columnNames[columnIndex]).uc()}


        private fun getIndexName(): String {
            var indexName = tableNameProcessed
            for (column in columnNamesProcessed) {
                indexName += (GROUND_DASH + column)
            }
            return indexName
        }

        fun getDeletionSql(): String{
            if (columnNamesProcessed.isNotEmpty()) {
                return " DROP INDEX IF EXISTS ${getIndexName()} "
            }
            return EMPTY
        }

        fun getCreationSql(): String {
            if (columnNamesProcessed.isNotEmpty()) {
                return " CREATE INDEX IF NOT EXISTS ${getIndexName()} ON $tableNameProcessed (${StringHelper.concatTokens(COMMA,*columnNamesProcessed)}) "
            }
            return EMPTY
        }

        fun getStatisticsSql(): String {
            return " ANALYZE $tableNameProcessed "
        }
    }

}