package com.snapfilters.feature.filters.repository

import com.snapfilters.feature.filters.catalogue.BuiltInFilters
import com.snapfilters.feature.filters.model.LutCube
import com.snapfilters.feature.filters.parser.CubeParser
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

/**
 * LutRepository manages built-in filters and imported custom LUTs.
 * Implements LRU cache of 3 LUTs in memory (~2.4 MB at 33^3) per PRD v3.0 Chapter 14.
 */
class LutRepository(private val lutsDirectory: File) {

    // LRU cache capped at 3 LUTs
    private val memoryCache = object : LinkedHashMap<String, LutCube>(3, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, LutCube>?): Boolean {
            return size > 3
        }
    }

    init {
        if (!lutsDirectory.exists()) {
            lutsDirectory.mkdirs()
        }
    }

    fun getLut(filterId: String): LutCube {
        synchronized(memoryCache) {
            memoryCache[filterId]?.let { return it }
        }

        // Check if built-in
        val builtIn = BuiltInFilters.ALL.find { it.id == filterId }
        if (builtIn != null) {
            val lut = BuiltInFilters.getLut(filterId, size = 33)
            synchronized(memoryCache) {
                memoryCache[filterId] = lut
            }
            return lut
        }

        // Check if custom imported LUT file exists
        val customFile = File(lutsDirectory, "$filterId.cube")
        if (customFile.exists()) {
            FileInputStream(customFile).use { stream ->
                val lut = CubeParser.parse(stream, customFile.length())
                synchronized(memoryCache) {
                    memoryCache[filterId] = lut
                }
                return lut
            }
        }

        // Fallback to Skin True
        return BuiltInFilters.getLut("skin_true", size = 33)
    }

    /**
     * Imports a custom .cube LUT file into app-internal storage under luts/<sha256-prefix>.cube.
     */
    fun importLut(sourceBytes: ByteArray): Pair<String, LutCube> {
        val parsed = CubeParser.parse(sourceBytes.inputStream(), sourceBytes.size.toLong())

        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(sourceBytes)
        val sha256Prefix = hash.take(8).joinToString("") { "%02x".format(it) }

        val destinationFile = File(lutsDirectory, "$sha256Prefix.cube")
        destinationFile.writeBytes(sourceBytes)

        synchronized(memoryCache) {
            memoryCache[sha256Prefix] = parsed
        }

        return Pair(sha256Prefix, parsed)
    }
}
