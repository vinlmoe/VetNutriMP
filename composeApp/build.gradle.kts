import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
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
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            linkerOpts.add("-lsqlite3")
        }
    }

    jvm("desktop")

    sourceSets {

           androidMain.dependencies {
                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.room.runtime)
                implementation(libs.androidx.room.paging)
               // implementation(libs.androidx.sqlite.sqlite.ktx)

        }

        commonMain.dependencies {
            implementation(libs.androidx.sqlite.bundled)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.coroutines.test)

            implementation(compose.ui)
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
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.kotlinx.coroutines.swing)
                implementation(compose.desktop.macos_arm64)
                implementation(libs.skiko.awt)
              //  implementation(libs.junit.jupiter)
               // implementation(libs.junit.junit)

            /*    implementation(libs.xerial.sqlite.jdbc)
                implementation(libs.androidx.sqlite.sqlite.framework3)
                implementation(libs.androidx.sqlite.sqlite.ktx)*/
                implementation(libs.compose.ui.test.manifest)
            }

        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
           // implementation(kotlin("test-annotations-common"))
            implementation(libs.assertk)
            implementation(kotlin("test"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
        }
       iosMain.dependencies {
                implementation(libs.sqliter.driver)

        }
        iosTest.dependencies {
            implementation(kotlin("test"))
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
        applicationId = "fr.vetbrain.vetnutri_mp.androidApp"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
        
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
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }


}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "fr.vetbrain.vetnutri_mp.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "fr.vetbrain.vetnutri_mp"
            packageVersion = "1.0.0"
        }
    }
}

dependencies {

    implementation(libs.skiko.awt)
    implementation(libs.androidx.sqlite.bundled)

    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    add("kspIosX64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspDesktop", libs.androidx.room.compiler)
}

room {
    schemaDirectory("$projectDir/schemas")
}
