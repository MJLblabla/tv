import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
}
val coroutinesVersion = "1.7.3"

val  ktorVersion = "2.3.9"
kotlin {
    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation("dev.mobile:dadb:1.2.7")
            implementation("io.github.alexzhirkevich:qrose:1.0.0")
            implementation("io.github.alexzhirkevich:qrose-oned:1.0.0")
            implementation("io.ktor:ktor-client-core:$ktorVersion")
            implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
           // implementation("io.ktor:ktor-client-darwin:$ktorVersion")
            implementation("io.ktor:ktor-client-cio:$ktorVersion")


        }
    }
}


compose.desktop {
    application {
        mainClass = "MainKt"
println("icon  "+project.file("ic_launcher.ico").absolutePath)
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb,TargetFormat.Exe)
            packageName = "盒子助手"
            packageVersion = "1.0.0"

            windows{
                shortcut = true
                dirChooser = true
               // upgradeUuid = "1"
               // iconFile.set(project.file("ic_launcher.ico"))
            }
        }
    }
}
