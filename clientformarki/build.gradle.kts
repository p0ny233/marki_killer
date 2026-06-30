plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias {libs.plugins.protobuf}
}

android {
    namespace = "com.example.clientformarki"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.clientformarki"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // 指定架构，最好是 ndk{} 放在 defaultConfig内部
        // https://www.cnblogs.com/yongfengnice/p/18456883
        ndk {
            abiFilters.clear()
            abiFilters.addAll(arrayOf("arm64-v8a"))  //这里指定的是源代码编译要支持编译出哪些架构的so库，一般支持"armeabi-v7a", "arm64-v8a"两个即可
        }
    }


    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            ndk {
                abiFilters.clear()
                abiFilters.addAll(arrayOf("arm64-v8a"))  //这里指定的是源代码编译要支持编译出哪些架构的so库，一般支持"armeabi-v7a", "arm64-v8a"两个即可
            }

        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.appcompat)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation("com.google.android.material:material:1.14.0")
    implementation("com.google.protobuf:protobuf-javalite:3.25.5")
}

// ──── 配置 protoc 编译器 ────
protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.5"   // 编译器 artifact，版本与上面一致
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite")   // 生成精简版代码
                }
            }
        }
    }
}