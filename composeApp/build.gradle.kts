import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.gradle.jvm.tasks.Jar

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
            implementation(libs.qr.kit)
            
            // Forcer la mise à jour de androidx.graphics pour compatibilité 16KB
            // Cette dépendance est transitive de androidx.compose.ui:ui-graphics-android
            // Les versions récentes devraient être compatibles 16KB
            implementation("androidx.graphics:graphics-path:1.0.1") {
                // Forcer la résolution pour éviter les conflits
            }
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
                implementation("io.github.g0dkar:qrcode-kotlin:4.5.0")
            }
        }

        val iosMain by creating {
            dependencies {
                implementation(libs.sqliter.driver)
                implementation(libs.ktor.client.darwin)
                implementation(libs.qr.kit)
            }
        }
    }
}

android {
    namespace = "fr.vetbrain.vetnutri_mp"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    buildFeatures {
        buildConfig = true
    }

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "fr.vetbrain.vetnutri_mp"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 240
        versionName = "3.2.40"

        // Configuration de Room

        // Configuration pour la compatibilité avec les pages mémoire de 16KB (Android 15+)
        // Référence: https://developer.android.com/guide/practices/page-sizes?hl=fr#compile-16-kb-alignment
        // AGP 8.5.1+ (nous avons 8.7.3) applique automatiquement l'alignement 16KB
        // lors du packaging des bibliothèques partagées non compressées
        ndk {
            // Filtrer les ABI pour ne garder que celles compatibles avec 16KB
            // arm64-v8a est compatible avec 16KB pages sur Android 15+
            // IMPORTANT: 
            // - armeabi-v7a réintroduit pour améliorer la compatibilité Chromebook
            //   (certains environnements Android sur ChromeOS restent limités en 32-bit)
            // - x86 exclu (architecture 32-bit legacy)
            // - x86_64 conservé pour la compatibilité Chromebook (Android on ChromeOS)
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64")
        }
    }

    packaging {
        resources {
            // excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        // Configuration pour la compatibilité avec les pages mémoire de 16KB
        // Référence: https://developer.android.com/guide/practices/page-sizes?hl=fr#compile-16-kb-alignment
        // AGP 8.5.1+ applique automatiquement l'alignement 16KB pour les bibliothèques non compressées
        jniLibs {
            // useLegacyPackaging = false permet à AGP d'appliquer l'alignement 16KB automatiquement
            useLegacyPackaging = false
            // Préserver les symboles de debug pour faciliter le diagnostic si nécessaire
            keepDebugSymbols += "**/*.so"
            // Exclure la bibliothèque problématique libimage_processing_util_jni.so
            // Cette bibliothèque n'est pas alignée sur 16KB (4096 bytes au lieu de 16384)
            // Elle est utilisée pour optimiser PathEffect.dashPathEffect mais Compose a
            // une implémentation de fallback en Java/Kotlin qui fonctionnera correctement
            // Risque: Légère dégradation de performance pour les lignes pointillées (négligeable)
            excludes += listOf("**/libimage_processing_util_jni.so")
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            versionNameSuffix = rootProject.extra["releaseVersionNameSuffix"] as String
            // AGP 8.5.1+ applique automatiquement l'alignement 16KB lors du packaging
            // Référence: https://developer.android.com/guide/practices/page-sizes?hl=fr#compile-16-kb-alignment
            isDebuggable = false
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

        buildTypes.release { proguard { isEnabled.set(false) } }

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Exe, TargetFormat.Deb)
            packageName = "VetNutriMP"
            packageVersion = "3.2.40"
            description = "Application de nutrition vétérinaire multiplateforme"
            copyright = "© 2026 VetBrain"
            vendor = "VetBrain"

            // Configuration macOS
            macOS {
                iconFile.set(project.file("src/desktopMain/resources/icon.icns"))
                bundleID = "fr.vetbrain.vetnutri_mp"
                // Note: La signature est effectuée après compilation via le script
                // compile_and_sign_macos.sh
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
// Force exclude kotlinx-coroutines-android from desktop configurations
configurations.configureEach {
    if (name.lowercase().contains("desktop")) {
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-android")
    }
}

// Native Image (GraalVM) - expérimental pour Windows single-file
val desktopMainClass = "fr.vetbrain.vetnutri_mp.MainKt"
val nativeImageConfigDir = layout.buildDirectory.dir("native-image/config")
val nativeImageOutputDir = layout.buildDirectory.dir("native/native-image")
val isWindowsHost = providers.systemProperty("os.name")
    .map { it.lowercase().contains("windows") }
    .orElse(false)

tasks.register<JavaExec>("runDesktopWithNativeAgent") {
    group = "native image"
    description = "Lance l'app desktop avec l'agent Native Image pour générer la config de réflexion/JNI."
    dependsOn("desktopJar")
    mainClass.set(desktopMainClass)
    classpath = files(
        tasks.named<Jar>("desktopJar").flatMap { it.archiveFile },
        configurations.getByName("desktopRuntimeClasspath")
    )
    jvmArgs(
        "-agentlib:native-image-agent=config-output-dir=${nativeImageConfigDir.get().asFile.absolutePath}"
    )
}

tasks.register<Exec>("buildWindowsNativeImage") {
    group = "native image"
    description = "Construit un exe Windows single-file via GraalVM native-image (expérimental)."
    dependsOn("desktopJar")
    onlyIf {
        if (!isWindowsHost.get()) {
            logger.warn("buildWindowsNativeImage ignorée: cette tâche doit être lancée sur Windows.")
            false
        } else {
            true
        }
    }

    doFirst {
        val outputDir = nativeImageOutputDir.get().asFile
        outputDir.mkdirs()

        val desktopJar = tasks.named<Jar>("desktopJar").get().archiveFile.get().asFile
        val runtimeFiles = configurations.getByName("desktopRuntimeClasspath").resolve()
        val classpath = (listOf(desktopJar) + runtimeFiles).joinToString(";") { it.absolutePath }
        val outputExe = outputDir.resolve("VetNutriMP.exe").absolutePath

        val args = mutableListOf(
            "native-image",
            "--no-fallback",
            "--enable-url-protocols=http,https",
            "-H:+AddAllCharsets",
            "-H:+ReportExceptionStackTraces",
            "-Dfile.encoding=UTF-8",
            "-cp",
            classpath,
            desktopMainClass,
            outputExe
        )

        val configDir = nativeImageConfigDir.get().asFile
        if (configDir.exists()) {
            args.add(1, "-H:ConfigurationFileDirectories=${configDir.absolutePath}")
        } else {
            logger.lifecycle(
                "Aucune config agent trouvée dans ${configDir.absolutePath}. " +
                    "Exécute d'abord runDesktopWithNativeAgent pour améliorer les chances de succès."
            )
        }

        commandLine(args)
    }
}
