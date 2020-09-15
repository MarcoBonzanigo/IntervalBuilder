package ch.scythetec.intervalbuilder.datamodel.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import ch.scythetec.intervalbuilder.const.Constants.Companion.BASE_COLUMNS_ID
import ch.scythetec.intervalbuilder.const.Constants.Companion.COLUMN_TYPE
import ch.scythetec.intervalbuilder.const.Constants.Companion.COMMA
import ch.scythetec.intervalbuilder.const.Constants.Companion.DATA_TYPE
import ch.scythetec.intervalbuilder.const.Constants.Companion.EMPTY
import ch.scythetec.intervalbuilder.const.Constants.Companion.FLOATING_NUMBER_TYPE
import ch.scythetec.intervalbuilder.const.Constants.Companion.ID_COLUMN
import ch.scythetec.intervalbuilder.const.Constants.Companion.KEY_TYPE
import ch.scythetec.intervalbuilder.const.Constants.Companion.NUMBER_TYPE
import ch.scythetec.intervalbuilder.const.Constants.Companion.SPACE
import ch.scythetec.intervalbuilder.const.Constants.Companion.TABLE_NAME
import ch.scythetec.intervalbuilder.const.Constants.Companion.TEXT_TYPE
import ch.scythetec.intervalbuilder.helper.StringHelper
import ch.scythetec.intervalbuilder.helper.lc

abstract class AbstractDatabase {
    private val tableCreations = ArrayList<String>()
    private val tableBackups = ArrayList<String>()
    private val tableDeletions = ArrayList<String>()
    private val tableRestorations = ArrayList<String>()
    private val tableCleanup = ArrayList<String>()
    val tables = ArrayList<String?>()

    init {
        collectTableStatements()
    }

    //Creation
    protected fun collectTableStatements(specificTable: String? = null): ArrayList<String> {
        tableCreations.clear()
        for (declaredClass in AbstractDatabase::class.java.declaredClasses) {
            var create = false
            for (interfaces in declaredClass.interfaces) {
                if (interfaces.name == BASE_COLUMNS_ID) create = true
            }
            if (!create) continue
            val columns = HashMap<String, String>()
            var tableName: String? = null
            var idColumnExists = false
            for (field in declaredClass.fields) {
                if (field.declaringClass.name == BASE_COLUMNS_ID) {
                    when (ID_COLUMN) {
                        field.name -> idColumnExists = true
                    }
                    continue
                }
                when {
                    field.name.contains(TABLE_NAME) -> tableName = (field.get(declaredClass) as String).replace(" ", "")
                    field.name.contains(COLUMN_TYPE) -> columns[StringHelper.extractNameFromClassString(field.name).replace(COLUMN_TYPE, SPACE).plus(SPACE)] = field.get(declaredClass) as String
                    field.name == ID_COLUMN -> columns[" " + StringHelper.extractNameFromClassString(field.name).lc().plus(" ")] = NUMBER_TYPE + KEY_TYPE
                }
            }
            if (specificTable == null) {
                tables.add(tableName)
            }
            val creation = composeCreationStatement(tableName, columns, idColumnExists)
            val backup = String.format("ALTER TABLE %s RENAME TO 'TEMP_%s'",tableName, tableName)
            val drop = String.format("DROP TABLE IF EXISTS %s",tableName)
            val restoration = composeRestorationStatement(tableName, columns, idColumnExists)
            val cleanup = String.format("DROP TABLE IF EXISTS TEMP_%s",tableName)
            if (specificTable != null && tableName != specificTable){
                continue
            }else if (specificTable != null && tableName == specificTable) {
                val listOfStatements = ArrayList<String>()
                listOfStatements.add(drop)
                listOfStatements.add(creation)
                return listOfStatements
            }
            tableCreations.add(creation)
            tableBackups.add(backup)
            tableDeletions.add(drop)
            tableRestorations.add(restoration)
            tableCleanup.add(cleanup)
        }
        return ArrayList()
    }

    private fun composeCreationStatement(tableName: String?, columns: HashMap<String, String>, idColumnExists: Boolean): String {
        if (tableName == null || columns.isEmpty()) {
            return ""
        }
        return String.format("CREATE TABLE IF NOT EXISTS %s (%s)", tableName, concatenateColumns(idColumnExists, columns))
    }

    private fun composeRestorationStatement(tableName: String?, columns: HashMap<String, String>, idColumnExists: Boolean): String {
        if (tableName == null || columns.isEmpty()) {
            return ""
        }
        return String.format("INSERT INTO %s (%s) SELECT %s FROM TEMP_%s",tableName, concatenateColumns(idColumnExists,  columns, false), concatenateColumns(idColumnExists,  columns, false), tableName)
    }

    private fun concatenateColumns(idColumnExists: Boolean, columns: HashMap<String, String>, withDataType: Boolean = true): String {
        var subStatement = if (idColumnExists) String.format(" _ID %s,", if (withDataType) NUMBER_TYPE else EMPTY) else EMPTY
        for (entry in columns) {
            subStatement = String.format("%s %s %s,",subStatement, entry.key, if (withDataType) entry.value else EMPTY)
        }
        subStatement = subStatement.substring(0, subStatement.length - COMMA.length)
        return subStatement
    }

    //Table for Numbers
    class NumberTableEntry private constructor() : IntervalBuilderBaseColumns {
        companion object {
            const val TABLE_NAME = " NUMBER_TABLE "
            const val COLUMN_TYPE_KEY = TEXT_TYPE + KEY_TYPE
            const val COLUMN_TYPE_VALUE = FLOATING_NUMBER_TYPE
            const val COLUMN_TYPE_TIMESTAMP = NUMBER_TYPE
            const val KEY = " KEY "
            const val VALUE = " VALUE "
            const val TIMESTAMP = " TIMESTAMP "
        }
    }

    //Table for Texts
    class TextTableEntry private constructor() : IntervalBuilderBaseColumns {
        companion object {
            const val TABLE_NAME = " TEXT_TABLE "
            const val COLUMN_TYPE_KEY = TEXT_TYPE + KEY_TYPE
            const val COLUMN_TYPE_VALUE = TEXT_TYPE
            const val COLUMN_TYPE_TIMESTAMP = NUMBER_TYPE
            const val KEY = " KEY "
            const val VALUE = " VALUE "
            const val TIMESTAMP = " TIMESTAMP "
        }
    }

    //Table for Data
    class DataTableEntry private constructor() : IntervalBuilderBaseColumns {
        companion object {
            const val TABLE_NAME = " DATA_TABLE "
            const val COLUMN_TYPE_KEY = TEXT_TYPE + KEY_TYPE
            const val COLUMN_TYPE_VALUE = DATA_TYPE
            const val COLUMN_TYPE_TIMESTAMP = NUMBER_TYPE
            const val KEY = " KEY "
            const val VALUE = " VALUE "
            const val TIMESTAMP = " TIMESTAMP "
        }
    }

    class IntervalSequenceTableEntry private constructor() : IntervalBuilderBaseColumns {
        companion object {
            const val TABLE_NAME = " INTERVAL_SEQUENCE_TABLE "
            const val COLUMN_TYPE_ID = TEXT_TYPE + KEY_TYPE
            const val COLUMN_TYPE_TIME_INTERVAL = NUMBER_TYPE
            const val COLUMN_TYPE_TIME_REST = NUMBER_TYPE
            const val COLUMN_TYPE_REPETITIONS = NUMBER_TYPE
            const val COLUMN_TYPE_EXERCISE_ID = TEXT_TYPE
            const val COLUMN_TYPE_POSITION = NUMBER_TYPE
            const val ID = " ID "
            const val TIME_INTERVAL = " TIME_INTERVAL "
            const val TIME_REST = " TIME_REST "
            const val REPETITIONS = " REPETITIONS "
            const val EXERCISE_ID = " EXERCISE_ID "
            const val POSITION = " POSITION "
        }
    }

    class IntervalSequenceListTableEntry private constructor() : IntervalBuilderBaseColumns {
        companion object {
            const val TABLE_NAME = " INTERVAL_SEQUENCE_LIST_TABLE "
            const val COLUMN_TYPE_ID = TEXT_TYPE + KEY_TYPE
            const val COLUMN_TYPE_TITLE = TEXT_TYPE
            const val COLUMN_TYPE_SEQUENCE_ID_LIST = TEXT_TYPE
            const val ID = " ID "
            const val TITLE = " TITLE "
            const val SEQUENCE_ID_LIST = " SEQUENCE_ID_LIST "
        }
    }

    class ExerciseTableEntry private constructor() : IntervalBuilderBaseColumns {
        companion object {
            const val TABLE_NAME = " EXERCISE_TABLE "
            const val COLUMN_TYPE_ID = TEXT_TYPE + KEY_TYPE
            const val COLUMN_TYPE_TITLE = TEXT_TYPE
            const val COLUMN_TYPE_DESCRIPTION = TEXT_TYPE
            const val COLUMN_TYPE_CATEGORY = TEXT_TYPE
            const val COLUMN_TYPE_IMAGE = DATA_TYPE
            const val ID = " ID "
            const val TITLE = " TITLE "
            const val DESCRIPTION = " DESCRIPTION "
            const val CATEGORY = " CATEGORY "
            const val IMAGE = " IMAGE "
        }
    }

    protected inner class DbHelper(context: Context, path: String, databaseVersion: Int) : SQLiteOpenHelper(context, path, null, databaseVersion) {

        init {
            onCreate(writableDatabase)
        }

        override fun onCreate(db: SQLiteDatabase) {
            for (creation in tableCreations) {
                db.execSQL(creation)
            }
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            for (backup in tableBackups) {
                db.execSQL(backup)
            }
            for (deletion in tableDeletions) {
                db.execSQL(deletion)
            }
            onCreate(db)
            for (restoration in tableRestorations) {
                db.execSQL(restoration)
            }
            for (cleanup in tableCleanup) {
                db.execSQL(cleanup)
            }
        }

        override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            onUpgrade(db, oldVersion, newVersion)
        }
    }
}

interface IntervalBuilderBaseColumns
