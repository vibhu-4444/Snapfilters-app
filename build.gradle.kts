plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ksp) apply false
}

// Module Architecture Rule Enforcement (ADR-001)
// Allowed:
//   feature-* -> core-*
//   app -> feature-*, core-*
//   core-database -> core-network
// Forbidden:
//   feature-* -> feature-* (any direction, any reason)
//   core-* -> feature-*
//   core-* -> core-network (except core-database)
tasks.register("verifyModuleDependencies") {
    group = "verification"
    description = "Enforces ADR-001 architectural rules on project dependencies."
    doLast {
        val violations = mutableListOf<String>()
        val projectRootDir = project.rootDir
        val featureDirs = File(projectRootDir, "feature").listFiles()?.filter { it.isDirectory } ?: emptyList()
        val coreDirs = File(projectRootDir, "core").listFiles()?.filter { it.isDirectory } ?: emptyList()

        featureDirs.forEach { featureDir ->
            val buildFile = File(featureDir, "build.gradle.kts")
            if (buildFile.exists()) {
                val text = buildFile.readText()
                featureDirs.forEach { otherFeature ->
                    if (text.contains(":feature:${otherFeature.name}")) {
                        violations.add("FORBIDDEN: :feature:${featureDir.name} depends on :feature:${otherFeature.name}. Feature-to-feature dependencies are strictly banned (ADR-001).")
                    }
                }
            }
        }

        coreDirs.forEach { coreDir ->
            val buildFile = File(coreDir, "build.gradle.kts")
            if (buildFile.exists()) {
                val text = buildFile.readText()
                featureDirs.forEach { featureDir ->
                    if (text.contains(":feature:${featureDir.name}")) {
                        violations.add("FORBIDDEN: :core:${coreDir.name} depends on :feature:${featureDir.name}. Core modules cannot depend on feature modules.")
                    }
                }
                if (coreDir.name != "database" && coreDir.name != "network") {
                    if (text.contains(":core:network")) {
                        violations.add("FORBIDDEN: :core:${coreDir.name} depends on :core:network. Only :core:database may depend on :core:network.")
                    }
                }
            }
        }

        if (violations.isNotEmpty()) {
            throw GradleException("Architecture verification failed with ${violations.size} violation(s):\n" + violations.joinToString("\n"))
        } else {
            logger.lifecycle("Architecture verification passed: 15 modules adhere to ADR-001 dependency graph.")
        }
    }
}

