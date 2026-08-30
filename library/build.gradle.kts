plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
    id("maven-publish")
}

android {
    namespace = "io.github.zakayothuku.roominspector"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    buildFeatures {
        compose = true
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.sqlite)
    implementation(libs.androidx.sqlite.framework)
    compileOnly(libs.androidx.room.runtime)

    // Jetpack Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Unit Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])

                groupId = "io.github.zakayothuku"
                artifactId = "compose-room-inspector"
                version = "1.0.0"

                pom {
                    name.set("compose-room-inspector")
                    description.set("In-App SQLite & Room Database Browser and SQL Query Editor with Jetpack Compose Overlay for Android.")
                    url.set("https://github.com/zakayothuku/compose-room-inspector")

                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://opensource.org/licenses/MIT")
                        }
                    }

                    developers {
                        developer {
                            id.set("zakayothuku")
                            name.set("Zakayo Thuku")
                            email.set("zakayothuku@gmail.com")
                        }
                    }

                    scm {
                        connection.set("scm:git:github.com/zakayothuku/compose-room-inspector.git")
                        developerConnection.set("scm:git:ssh://github.com/zakayothuku/compose-room-inspector.git")
                        url.set("https://github.com/zakayothuku/compose-room-inspector")
                    }
                }
            }
        }
    }
}
