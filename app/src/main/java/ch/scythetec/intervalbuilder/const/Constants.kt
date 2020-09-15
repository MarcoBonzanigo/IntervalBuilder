package ch.scythetec.intervalbuilder.const

class Constants {
    companion object {
        const val APPLICATION_ID: String = "ch.scythetec.intervalbuilder.IntervalBuilder"
        //Kotlin
        const val REFLECTION: String = " (Kotlin reflection is not available)"
        //Storage
        const val SHARED_PREFERENCES: String = "intervalBuilderSharedPreferences"
        //NUMBERS
        val COORDINATES_PATTERN: Regex = "-?[1]?[0-9]{1,2}\\.[0-9]{1,6},-?[1]?[0-9]{1,2}\\.[0-9]{1,6}".toRegex()
        val ICON_PATTERN: Regex = ".*[^\\x20-\\x7E].*".toRegex()
        const val EARTH_RADIUS_M: Double = 6371000.0
        const val COLOR_BOUND: Int = 16777215
        const val MILLION_D: Double = 1000000.0
        const val MILLION_I: Int = 1000000
        const val WIFI_DIRECT_PORT: Int = 8118
        const val DAY_MS: Long = 86400000
        const val HOUR_MS: Long = 3600000
        const val MIN_MS: Long = 60000
        const val SEC_MS: Long = 1000
        const val FIVE_MIN_MS: Long = 300000
        const val TEN_SEC_MS: Long = 10000
        const val FIVE_SEC_MS: Long = 5000
        const val TWO_SEC_MS: Long = 2000
        const val ONE_SEC_MS: Long = 1000
        const val HALF_SEC_MS: Long = 500
        const val THIRD_SEC_MS: Long = 300
        const val THOUSAND_D: Double = 1000.0
        const val HUNDRED_D: Double = 100.0
        const val EIGHT_KB: Int = 8192
        const val ONE_I: Int = 1
        const val ZERO_S: String = "0"
        const val ZERO_D: Double = 0.0
        const val ZERO_F: Float = 0.0f
        const val ZERO_L: Long = 0
        const val ZERO_I: Int = 0
        //PERMISSIONS
        const val PERMISSION_REQUEST_ALL: Int = 888
        const val PERMISSION_REQUEST_GPS: Int = 666
        const val UNKNOWN_RESPONSE: Int = 404
        //REQUESTS
        const val SELECT_PICTURE = 333
        const val IMPORT_DATA_FOLDER: Int = 222
        const val IMPORT_DATA_FILE: Int = 111
        //VALIDATION
        const val NOT_VALIDATED: Int = 0
        const val VALIDATION_OK: Int = 1
        const val VALIDATION_EMPTY: Int = 2
        const val VALIDATION_FAILED: Int = 3
        const val VALIDATION_INVALID: Int = 4
        const val VALIDATION_NO_DATA: Int = 5
        //UID-IDENTIFIERS
        const val TARGET_STEP_UID_IDENTIFIER: String = "target_step_"
        const val CHANGE_UID_IDENTIFIER: String = "change_"
        const val CHECK_VALUE_UID_IDENTIFIER: String = "check_value_"
        const val CHECK_MODE_UID_IDENTIFIER: String = "check_mode_"
        const val HASH_MAP_UID_IDENTIFIER: String = "hash_map_"
        const val HASH_MAP_OPTIONS_IDENTIFIER: String = "hash_map_options_"
        const val HASH_MAP_LINK_IDENTIFIER: String = "hash_map_link_"
        const val ARRAY_LIST_WHAT_IF_IDENTIFIER: String = "array_list_what_if_"
        const val VERSIONING_IDENTIFIER: String = "version_"
        const val MIN_IDENTIFIER: String = "min_"
        const val MAX_IDENTIFIER: String = "max_"
        const val INIT_IDENTIFIER: String = "init_"
        //GENERAL
        const val NEW_LINE_C: Char = '\n'
        const val SPACE_C: Char = ' '
        const val EQUALS: String = "="
        const val NEW_LINE: String = "\n"
        const val CARRIAGE_RETURN: String = "\r"
        const val REPLACEMENT_TOKEN: String = "!!REPLACEMENT!!"
        const val PERCENT: String = "%"
        const val SPACE: String = " "
        const val COMMA: String = ","
        const val COMMA_DELIM: String = ", "
        const val SEMI_COLON: String = ";"
        const val DOLLAR_STRING: Char = '$'
        const val EMPTY: String = ""
        const val SINGLE_QUOTE = "'"
        const val DOUBLE_QUOTE: Char = '"'
        const val WILDCARD: String = "'%'"
        const val BRACKET_OPEN: Char = '<'
        const val BRACKET_CLOSE: Char = '>'
        const val DASH: String = "-"
        const val GROUND_DASH: String = "_"
        const val SLASH: String = "/"
        const val BOLD_START: String = "<b>"
        const val BOLD_END: String = "</b>"
        const val BREAK: String = "<br>"
        const val ARROW_RIGHT: String = "->"
        const val ARROW_LEFT: String = "<-"
        //XML
        const val EMPTY_LIST: String = "EmptyList"
        const val NULL: String = "NULL"
        const val NULL_CLASS: String = "Null"
        const val HYPHEN: String = "-"
        const val STRING: String = "String"
        const val INT: String = "Int"
        const val FLOAT: String = "Float"
        const val LONG: String = "Long"
        const val DOUBLE: String = "Double"
        const val BOOLEAN: String = "Boolean"
        const val HASH_MAP_ENTRY: String = "HashMap\$HashMapEntry"
        //FILE FORMATS
        const val FILE_TYPE_IB: String = ".ib"
        const val FILE_TYPE_TFF = ".ttf"
        const val FILE_TYPE_TXT = ".txt"
        const val FILE_TYPE_PNG = ".png"
        const val FILE_TYPE_CSV = ".csv"
        //FILE LOCATIONS
        const val FOLDER_ROOT: String = "/IntervalBuilder"
        const val FOLDER_ANALYTICS: String = "/Analytics"
        const val FOLDER_EXPORT: String = "/Export"
        const val FOLDER_IMPORT: String = "/Import"
        const val FOLDER_DATABASE: String = "/Database"
        const val FOLDER_TEMP: String = "/Temp"
        //DATABASE
        //Package Private
        const val LIKE = " LIKE "
        const val EQ = " = "
        const val NEQ = " != "
        const val ONE = " 1 "
        const val MIN_ONE = " -1 "
        const val ZERO = " 0 "
        const val AND = " AND "
        const val ANY = "%"
        //Private
        const val TEXT_TYPE = " TEXT "
        const val NUMBER_TYPE = " INTEGER "
        const val FLOATING_NUMBER_TYPE = " REAL "
        const val DATA_TYPE = " BLOB "
        const val KEY_TYPE = " PRIMARY KEY "
        //Reflection
        const val BASE_COLUMNS_ID = "ch.scythetec.intervalbuilder.datamodel.database.IntervalBuilderBaseColumns"
        val COLUMN_TYPE = "COLUMN_TYPE_"
        val TABLE_NAME = "TABLE_NAME"
        val ID_COLUMN = "_ID"
    }
}