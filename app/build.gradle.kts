plugins {
    alias(libs.plugins.android.application)
    alias {libs.plugins.protobuf}
}

android {
    namespace = "com.hook"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.hook"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    // 4. 引入xposed依赖，仅参与编译，不参与构建，从 libs.version.toml 中导入
    compileOnly (libs.api)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
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