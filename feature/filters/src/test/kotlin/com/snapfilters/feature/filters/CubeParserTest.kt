package com.snapfilters.feature.filters

import com.snapfilters.feature.filters.parser.CubeParseException
import com.snapfilters.feature.filters.parser.CubeParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.ByteArrayInputStream

class CubeParserTest {

    private fun generateValidCubeString(size: Int, title: String = "TestLUT"): String {
        val sb = StringBuilder()
        sb.appendLine("TITLE \"$title\"")
        sb.appendLine("LUT_3D_SIZE $size")
        sb.appendLine("DOMAIN_MIN 0.0 0.0 0.0")
        sb.appendLine("DOMAIN_MAX 1.0 1.0 1.0")

        val step = 1.0f / (size - 1)
        for (b in 0 until size) {
            for (g in 0 until size) {
                for (r in 0 until size) {
                    sb.appendLine("${r * step} ${g * step} ${b * step}")
                }
            }
        }
        return sb.toString()
    }

    // FR-FLT-03: Valid 17 point .cube imports correctly
    @Test
    fun parse_valid_17_cube_succeeds() {
        val cubeStr = generateValidCubeString(17, "Test17")
        val stream = ByteArrayInputStream(cubeStr.toByteArray(Charsets.UTF_8))
        val lut = CubeParser.parse(stream)

        assertEquals("Test17", lut.title)
        assertEquals(17, lut.size)
        assertEquals(17 * 17 * 17 * 3, lut.data.size)
    }

    // FR-FLT-03: Valid 33 point .cube imports correctly
    @Test
    fun parse_valid_33_cube_succeeds() {
        val cubeStr = generateValidCubeString(33, "Test33")
        val stream = ByteArrayInputStream(cubeStr.toByteArray(Charsets.UTF_8))
        val lut = CubeParser.parse(stream)

        assertEquals("Test33", lut.title)
        assertEquals(33, lut.size)
        assertEquals(33 * 33 * 33 * 3, lut.data.size)
    }

    // Validation: Missing LUT_3D_SIZE
    @Test
    fun parse_missing_size_throws_correct_message() {
        val invalidCube = "TITLE \"NoSize\"\n0.1 0.2 0.3"
        try {
            CubeParser.parse(ByteArrayInputStream(invalidCube.toByteArray()))
            fail("Expected CubeParseException")
        } catch (e: CubeParseException) {
            assertEquals("This LUT does not declare a 3D size.", e.message)
        }
    }

    // Validation: Unsupported size (e.g. 25)
    @Test
    fun parse_unsupported_size_throws_correct_message() {
        val invalidCube = "LUT_3D_SIZE 25\n0.1 0.2 0.3"
        try {
            CubeParser.parse(ByteArrayInputStream(invalidCube.toByteArray()))
            fail("Expected CubeParseException")
        } catch (e: CubeParseException) {
            assertEquals("Only 17, 33 and 65 point LUTs are supported. This one is 25.", e.message)
        }
    }

    // Validation: 1D LUTs rejected
    @Test
    fun parse_1d_size_throws_correct_message() {
        val invalidCube = "LUT_1D_SIZE 256\n0.1 0.2 0.3"
        try {
            CubeParser.parse(ByteArrayInputStream(invalidCube.toByteArray()))
            fail("Expected CubeParseException")
        } catch (e: CubeParseException) {
            assertEquals("1D LUTs cannot express the grades this product sells.", e.message)
        }
    }

    // Validation: Line count mismatch
    @Test
    fun parse_incomplete_data_throws_correct_message() {
        val invalidCube = "LUT_3D_SIZE 17\n0.1 0.2 0.3\n0.4 0.5 0.6"
        try {
            CubeParser.parse(ByteArrayInputStream(invalidCube.toByteArray()))
            fail("Expected CubeParseException")
        } catch (e: CubeParseException) {
            assertTrue(e.message?.startsWith("This LUT is incomplete: 4913 values expected, 2 found.") == true)
        }
    }

    // Validation: File size exceeding 4 MB
    @Test
    fun parse_file_exceeding_4mb_throws_correct_message() {
        try {
            CubeParser.parse(ByteArrayInputStream(ByteArray(0)), byteLength = 5 * 1024 * 1024)
            fail("Expected CubeParseException")
        } catch (e: CubeParseException) {
            assertEquals("This LUT is too large. Files up to 4 MB are supported.", e.message)
        }
    }

    // Validation: Values outside 0-1 with default domain
    @Test
    fun parse_values_outside_0_to_1_throws_correct_message() {
        val invalidCube = "LUT_3D_SIZE 17\n1.5 0.2 0.3"
        try {
            CubeParser.parse(ByteArrayInputStream(invalidCube.toByteArray()))
            fail("Expected CubeParseException")
        } catch (e: CubeParseException) {
            assertEquals("This LUT contains values outside 0 to 1. Set DOMAIN_MIN and DOMAIN_MAX.", e.message)
        }
    }

    // Validation: NaN or Inf numbers
    @Test
    fun parse_nan_numbers_throws_correct_message() {
        val invalidCube = "LUT_3D_SIZE 17\nNaN 0.2 0.3"
        try {
            CubeParser.parse(ByteArrayInputStream(invalidCube.toByteArray()))
            fail("Expected CubeParseException")
        } catch (e: CubeParseException) {
            assertTrue(e.message?.startsWith("This LUT contains invalid numbers on line 2.") == true)
        }
    }
}
