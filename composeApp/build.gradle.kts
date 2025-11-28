import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
            // Optimisations sélectives pour réduire la complexité sans tout désactiver
            freeCompilerArgs.addAll(
                    "-Xjvm-default=all",
                    "-Xno-param-assertions",
                    "-Xno-call-assertions",
                    "-Xno-receiver-assertions",
                    "-Xoptimization-phase-step=15", // Réduire les étapes d'optimisation
                    "-Xinline-max-instruction-count=150", // Limiter l'inlining sélectif
                    "-Xdisable-phases=DevirtualizationAnalysis" // Désactiver seulement l'analyse
                    // problématique
                    )
        }
    }

    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            linkerOpts.add("-lsqlite3")
            binaryOption("bundleId", "fr.vetbrain.vetnutri")
        }
        // Configuration iOS - Configuration basique uniquement
        iosTarget.compilerOptions {
            freeCompilerArgs.addAll(
                    // C'est l'argument qui désactive la phase gourmande en mémoire
                    "-Xdisable-phases=DevirtualizationAnalysis,DCEPhase,StaticInitializersOptimization,RemoveRedundantCallsToStaticInitializersPhase"
            )
        }
    }

    jvm("desktop")

    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.room.paging)
            // implementation(libs.androidx.sqlite.sqlite.ktx)
            implementation(libs.ktor.client.android)
        }

        commonMain.dependencies {
            implementation("io.github.koalaplot:koalaplot-core:0.8.0")
            implementation("org.jetbrains.compose.material:material-icons-extended:1.7.3")
            implementation(libs.androidx.sqlite.bundled)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation("org.jetbrains.compose.ui:ui-util:1.7.0")
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.kotlin.test)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.room.paging)
            implementation(libs.okio)
            implementation(libs.okio)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.androidx.datastore.core.okio)
            implementation(libs.androidx.datastore.preferences.core)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.androidx.paging.common)
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs) // ✅ auto-resolve skiko
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.kotlinx.coroutines.swing)
                //  implementation(libs.junit.jupiter)
                // implementation(libs.junit.junit)

                /*    implementation(libs.xerial.sqlite.jdbc)
                implementation(libs.androidx.sqlite.sqlite.framework3)
                implementation(libs.androidx.sqlite.sqlite.ktx)*/
                implementation(libs.compose.ui.test.manifest)
                implementation("com.openhtmltopdf:openhtmltopdf-core:1.0.10")
                implementation("com.openhtmltopdf:openhtmltopdf-pdfbox:1.0.10")
                implementation("com.openhtmltopdf:openhtmltopdf-svg-support:1.0.10")
                implementation(libs.ktor.client.okhttp)
                implementation(libs.ktor.client.content.negotiation)
            }
        }

        val iosMain by creating { 
            dependencies { 
                implementation(libs.sqliter.driver)
                implementation(libs.ktor.client.darwin)
            } 
        }
    }
}

android {
    namespace = "fr.vetbrain.vetnutri_mp"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "fr.vetbrain.vetnutri_mp"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 200
        versionName = "3.2.00"

        // Configuration de Room

    }

    packaging {
        resources {
            // excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            versionNameSuffix = rootProject.extra["releaseVersionNameSuffix"] as String
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies { debugImplementation(compose.uiTooling) }

compose.desktop {
    application {
        mainClass = "fr.vetbrain.vetnutri_mp.MainKt"

        buildTypes.release {
            proguard {
                isEnabled.set(false)
            }
        }

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Exe, TargetFormat.Deb)
            packageName = "VetNutriMP"
            packageVersion = "3.2.00"
            description = "Application de nutrition vétérinaire multiplateforme"
            copyright = "© 2025 VetBrain"
            vendor = "VetBrain"

            // Configuration macOS
            macOS {
                iconFile.set(project.file("src/desktopMain/resources/icon.icns"))
                bundleID = "fr.vetbrain.vetnutri_mp"
                // Note: La signature est effectuée après compilation via le script compile_and_sign_macos.sh
                // Compose Desktop ne supporte pas encore la signature automatique dans Gradle
            }
            windows {
                iconFile.set(project.file("src/desktopMain/resources/icon.ico"))
                // Configuration pour un exécutable portable
                menuGroup = "VetNutriMP"
                upgradeUuid = "18159995-d967-4cd2-8885-77BFA97CFA9F"
            }
            linux { iconFile.set(project.file("src/desktopMain/resources/icon.png")) }
        }
    }
}

dependencies {
    implementation("org.jetbrains.compose.material:material-icons-extended:1.7.3")

    implementation(libs.androidx.sqlite.bundled)
    implementation(kotlin("test"))
    implementation(kotlin("test-common"))
    implementation(kotlin("test-annotations-common"))
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    add("kspIosX64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspDesktop", libs.androidx.room.compiler)
    implementation("io.github.kevinnzou:compose-webview-multiplatform:1.9.40")

    // Reorderable - Drag and Drop pour Compose
    implementation("org.burnoutcrew.composereorderable:reorderable:0.9.6")
}

room { schemaDirectory("$projectDir/schemas") }

