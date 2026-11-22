package com.snapfilters.architecture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * ModuleGraphTest mechanically asserts ADR-001 architectural rules:
 * - 15 modules in three layers
 * - Feature modules depend on core modules only, NEVER on another feature module
 * - Core modules never depend on feature modules
 * - core-database is the ONLY core module permitted to depend on core-network
 */
class ModuleGraphTest {

    private val projectRoot = File(System.getProperty("user.dir") ?: ".")
        .let { if (it.name == "app") it.parentFile else it }

    private val allModules = listOf(
        "app",
        "core:common",
        "core:designsystem",
        "core:database",
        "core:network",
        "feature:camera",
        "feature:filters",
        "feature:ar",
        "feature:editor",
        "feature:gallery",
        "feature:sharing",
        "feature:auth",
        "feature:groups",
        "feature:chat",
        "feature:settings"
    )

    @Test
    fun assertFifteenModulesDeclared() {
        assertEquals("Exactly 15 modules must exist per ADR-001", 15, allModules.size)

        val settingsFile = File(projectRoot, "settings.gradle.kts")
        assertTrue("settings.gradle.kts must exist", settingsFile.exists())
        val settingsText = settingsFile.readText()

        allModules.forEach { module ->
            val includeStatement = "\":$module\""
            assertTrue(
                "Module $module must be included in settings.gradle.kts",
                settingsText.contains(includeStatement)
            )
        }
    }

    @Test
    fun assertNoFeatureToFeatureDependencies() {
        val featureNames = allModules.filter { it.startsWith("feature:") }.map { it.removePrefix("feature:") }

        featureNames.forEach { feature ->
            val buildFile = File(projectRoot, "feature/$feature/build.gradle.kts")
            if (buildFile.exists()) {
                val content = buildFile.readText()
                featureNames.forEach { otherFeature ->
                    val forbiddenDep = "project(\":feature:$otherFeature\")"
                    val isForbiddenPresent = content.contains(forbiddenDep)
                    assertTrue(
                        "ADR-001 VIOLATION: feature:$feature depends on feature:$otherFeature! Feature-to-feature dependencies are strictly banned.",
                        !isForbiddenPresent
                    )
                }
            }
        }
    }

    @Test
    fun assertNoCoreToFeatureDependencies() {
        val coreNames = allModules.filter { it.startsWith("core:") }.map { it.removePrefix("core:") }
        val featureNames = allModules.filter { it.startsWith("feature:") }.map { it.removePrefix("feature:") }

        coreNames.forEach { core ->
            val buildFile = File(projectRoot, "core/$core/build.gradle.kts")
            if (buildFile.exists()) {
                val content = buildFile.readText()
                featureNames.forEach { feature ->
                    val forbiddenDep = "project(\":feature:$feature\")"
                    val isForbiddenPresent = content.contains(forbiddenDep)
                    assertTrue(
                        "ADR-001 VIOLATION: core:$core depends on feature:$feature! Core modules cannot depend on feature modules.",
                        !isForbiddenPresent
                    )
                }
            }
        }
    }

    @Test
    fun assertOnlyCoreDatabaseMayDependOnCoreNetwork() {
        val coreNames = allModules.filter { it.startsWith("core:") }.map { it.removePrefix("core:") }

        coreNames.forEach { core ->
            if (core != "database" && core != "network") {
                val buildFile = File(projectRoot, "core/$core/build.gradle.kts")
                if (buildFile.exists()) {
                    val content = buildFile.readText()
                    val forbiddenDep = "project(\":core:network\")"
                    assertTrue(
                        "ADR-001 VIOLATION: core:$core depends on core:network! Only core:database may depend on core:network.",
                        !content.contains(forbiddenDep)
                    )
                }
            }
        }
    }
}
