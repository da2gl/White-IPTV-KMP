plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false

    // Code quality plugins
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
}

// Detekt configuration
detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    baseline = file("$rootDir/config/detekt/baseline.xml")

    source.setFrom(
        "composeApp/src/commonMain/kotlin",
        "composeApp/src/androidMain/kotlin",
        "composeApp/src/iosMain/kotlin",
    )
}

dependencies {
    detektPlugins(libs.detekt.formatting)
    detektPlugins(libs.detekt.compose.rules)
}

// ktlint configuration
ktlint {
    version.set(libs.versions.ktlint.get())
    android.set(true)
    ignoreFailures.set(false)
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.SARIF)
    }
    filter {
        exclude("**/generated/**")
        exclude("**/build/**")
    }
}

// Common tasks for all lint and format operations
tasks.register("lintAll") {
    group = "verification"
    description = "Run all lint checks (ktlint + detekt)"
    dependsOn("ktlintCheck", "detekt")
}

tasks.register("formatAll") {
    group = "formatting"
    description = "Format all Kotlin code with ktlint"
    dependsOn("ktlintFormat")
}

tasks.register("lintFix") {
    group = "verification"
    description = "Auto-fix lint issues where possible"
    dependsOn("ktlintFormat", "detekt")
}
