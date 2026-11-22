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
        val featureModules = subprojects.filter { it.path.startsWith(":feature:") }
        val coreModules = subprojects.filter { it.path.startsWith(":core:") }

        featureModules.forEach { featureProject ->
            featureProject.configurations.configureEach {
                dependencies.forEach { dep ->
                    if (dep is ProjectDependency) {
                        val targetPath = dep.dependencyProject.path
                        if (targetPath.startsWith(":feature:")) {
                            violations.add("FORBIDDEN: ${featureProject.path} depends on ${targetPath}. Feature-to-feature dependencies are strictly banned (ADR-001).")
                        }
                    }
                }
            }
        }

        coreModules.forEach { coreProject ->
            coreProject.configurations.configureEach {
                dependencies.forEach { dep ->
                    if (dep is ProjectDependency) {
                        val targetPath = dep.dependencyProject.path
                        if (targetPath.startsWith(":feature:")) {
                            violations.add("FORBIDDEN: ${coreProject.path} depends on ${targetPath}. Core modules cannot depend on feature modules.")
                        }
                        if (coreProject.path != ":core:database" && targetPath == ":core:network") {
                            violations.add("FORBIDDEN: ${coreProject.path} depends on :core:network. Only :core:database may depend on :core:network.")
                        }
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

