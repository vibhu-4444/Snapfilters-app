package com.snapfilters.feature.filters.parser

import com.snapfilters.feature.filters.model.LutCube
import java.io.InputStream
import java.nio.charset.MalformedInputException

class CubeParseException(message: String) : Exception(message)

/**
 * CubeParser strictly implements PRD v3.0 Chapter 14 specification and validation rules.
 */
object CubeParser {
    const val MAX_FILE_SIZE_BYTES = 4 * 1024 * 1024 // 4 MB cap (ADR-007)

    fun parse(inputStream: InputStream, byteLength: Long? = null): LutCube {
        if (byteLength != null && byteLength > MAX_FILE_SIZE_BYTES) {
            throw CubeParseException("This LUT is too large. Files up to 4 MB are supported.")
        }

        val bytes = try {
            inputStream.readBytes()
        } catch (e: Exception) {
            throw CubeParseException("This file is not a .cube LUT.")
        }

        if (bytes.size > MAX_FILE_SIZE_BYTES) {
            throw CubeParseException("This LUT is too large. Files up to 4 MB are supported.")
        }

        // Check for binary/non-UTF8 content
        val content = try {
            val decoder = Charsets.UTF_8.newDecoder()
            decoder.decode(java.nio.ByteBuffer.wrap(bytes)).toString()
        } catch (e: MalformedInputException) {
            throw CubeParseException("This file is not a .cube LUT.")
        }

        var title: String = "Untitled LUT"
        var size: Int? = null
        var domainMin = floatArrayOf(0f, 0f, 0f)
        var domainMax = floatArrayOf(1f, 1f, 1f)

        val lines = content.lines()
        val dataFloats = mutableListOf<Float>()
        var lineNumber = 0

        for (line in lines) {
            lineNumber++
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue
            }

            if (trimmed.startsWith("TITLE", ignoreCase = true)) {
                val parts = trimmed.split("\"", limit = 3)
                if (parts.size >= 2) {
                    title = parts[1].take(60)
                }
                continue
            }

            if (trimmed.startsWith("LUT_1D_SIZE", ignoreCase = true)) {
                throw CubeParseException("1D LUTs cannot express the grades this product sells.")
            }

            if (trimmed.startsWith("LUT_3D_SIZE", ignoreCase = true)) {
                val tokens = trimmed.split(Regex("\\s+"))
                val parsedSize = tokens.getOrNull(1)?.toIntOrNull()
                if (parsedSize == null || parsedSize !in setOf(17, 33, 65)) {
                    throw CubeParseException("Only 17, 33 and 65 point LUTs are supported. This one is ${tokens.getOrNull(1) ?: "unknown"}.")
                }
                size = parsedSize
                continue
            }

            if (trimmed.startsWith("DOMAIN_MIN", ignoreCase = true)) {
                val tokens = trimmed.split(Regex("\\s+"))
                if (tokens.size >= 4) {
                    try {
                        domainMin = floatArrayOf(tokens[1].toFloat(), tokens[2].toFloat(), tokens[3].toFloat())
                    } catch (e: NumberFormatException) {
                        throw CubeParseException("This LUT contains invalid numbers on line $lineNumber.")
                    }
                }
                continue
            }

            if (trimmed.startsWith("DOMAIN_MAX", ignoreCase = true)) {
                val tokens = trimmed.split(Regex("\\s+"))
                if (tokens.size >= 4) {
                    try {
                        domainMax = floatArrayOf(tokens[1].toFloat(), tokens[2].toFloat(), tokens[3].toFloat())
                    } catch (e: NumberFormatException) {
                        throw CubeParseException("This LUT contains invalid numbers on line $lineNumber.")
                    }
                }
                continue
            }

            // Unknown directive check
            if (trimmed.first().isLetter()) {
                val directive = trimmed.substringBefore(" ")
                throw CubeParseException("Unknown directive '$directive' on line $lineNumber.")
            }

            // Data line: 3 floats
            val tokens = trimmed.split(Regex("\\s+"))
            if (tokens.size < 3) {
                throw CubeParseException("This LUT contains invalid numbers on line $lineNumber.")
            }

            for (i in 0..2) {
                val floatVal = tokens[i].toFloatOrNull()
                if (floatVal == null || floatVal.isNaN() || floatVal.isInfinite()) {
                    throw CubeParseException("This LUT contains invalid numbers on line $lineNumber.")
                }
                // Value outside 0-1 with default domain
                if (domainMin.contentEquals(floatArrayOf(0f, 0f, 0f)) && domainMax.contentEquals(floatArrayOf(1f, 1f, 1f))) {
                    if (floatVal < 0f || floatVal > 1f) {
                        throw CubeParseException("This LUT contains values outside 0 to 1. Set DOMAIN_MIN and DOMAIN_MAX.")
                    }
                }
                dataFloats.add(floatVal)
            }
        }

        if (size == null) {
            throw CubeParseException("This LUT does not declare a 3D size.")
        }

        val expectedValues = size * size * size * 3
        if (dataFloats.size != expectedValues) {
            throw CubeParseException("This LUT is incomplete: ${expectedValues / 3} values expected, ${dataFloats.size / 3} found.")
        }

        return LutCube(
            title = title,
            size = size,
            domainMin = domainMin,
            domainMax = domainMax,
            data = dataFloats.toFloatArray()
        )
    }
}
