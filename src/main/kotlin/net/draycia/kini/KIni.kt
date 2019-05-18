package net.draycia.kini

import java.io.File
import java.io.FileReader
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.IllegalArgumentException
import java.lang.NumberFormatException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class KIni(private val stream: InputStream? = null, private val file: File? = null) {
    private val config = HashMap<String, ArrayList<Pair<String, String>>>()

    fun loadFile(): Boolean {
        val reader = when {
            stream != null -> InputStreamReader(stream)
            file != null -> FileReader(file)
            else -> return false
        }

        // Store the current section name and its key+value pairs
        var section = ""
        val values = ArrayList<Pair<String, String>>()

        // Regexps to ensure section and key+value lines are valid
        val sectionRegex = Regex("^\\[([\\w]+)]$")
        val entryRegex = Regex("^([\\w][\\w\\s]*[\\w])\\s*=\\s*([^;\\n]+)?")

        // Iterate through each line in the config and load it if valid
        reader.forEachLine { line ->
            if (line.startsWith("[")) {
                // Section marker
                val match = sectionRegex.find(line)

                if (match != null && match.groupValues.size == 2) {
                    config[section] = ArrayList(values) // Previous section found, store old data
                    values.clear()

                    section = match.groupValues[1]
                }
            } else if (line.startsWith(";") || line.isBlank()) {
                // Comment or empty line. Do nothing since you can't "continue".
            } else {
                // Key/Value
                val match = entryRegex.find(line)

                if (match != null && match.groupValues.size == 3) {
                    values.add(Pair(match.groupValues[1], match.groupValues[2]))
                }
            }
        }

        reader.close()

        config[section] = values // Save last bit of data

        return true
    }

    fun reloadFile() {
        loadFile()
    }

    private fun get(section: String?, key: String): String? {
        val sect = section ?: ""

        config[sect]?.let { options ->
            options.forEach { option ->
                if (option.first == key) {
                    return option.second
                }
            }
        }

        return null
    }

    fun getDouble(section: String?, key: String, default: Double? = null): Double? {
        return get(section, key)?.trim()?.toDoubleOrNull() ?: default
    }

    fun getFloat(section: String?, key: String, default: Float? = null): Float? {
        return get(section, key)?.trim()?.toFloatOrNull() ?: default
    }

    fun getLong(section: String?, key: String, default: Long? = null): Long? {
        return get(section, key)?.trim()?.toLongOrNull() ?: default
    }

    fun getInt(section: String?, key: String, default: Int? = null): Int? {
        return get(section, key)?.trim()?.toIntOrNull() ?: default
    }

    fun getShort(section: String?, key: String, default: Short? = null): Short? {
        return get(section, key)?.trim()?.toShortOrNull() ?: default
    }

    fun getByte(section: String?, key: String, default: Byte? = null): Byte? {
        return get(section, key)?.trim()?.toByteOrNull() ?: default
    }

    fun getBoolean(section: String?, key: String, default: Boolean? = null): Boolean? {
        return when (get(section, key)?.trim() ?: return default) {
            "true", "t", "1", "on" -> true
            else -> false
        }
    }

    fun getString(section: String?, key: String, default: String? = null): String? {
        return get(section, key) ?: default
    }

    fun getUUID(section: String?, key: String, default: UUID? = null): UUID? {
        return try {
            UUID.fromString(get(section, key)?.trim()) ?: default
        } catch (exception: IllegalArgumentException) {
            null
        }
    }

    fun getList(section: String?, key: String, default: List<String>?): List<String>? {
        return get(section, key)?.split(",") ?: default
    }

    @Throws(NumberFormatException::class)
    fun getIntList(section: String?, key: String, default: List<Int>?): List<Int>? {
        return get(section, key)?.split(",")?.map { it.trim().toInt() } ?: default
    }
}