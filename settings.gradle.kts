pluginManagement {
    plugins {
        id("com.android.library").version("4.2.2")
    }
    repositories {
        gradlePluginPortal()
        google()
    }
}

include(":react-native-get-random-values")
project(":react-native-get-random-values").projectDir = File(rootProject.projectDir, "node_modules/react-native-get-random-values/android")

include(":react-native-reanimated")
project(":react-native-reanimated").projectDir = File(rootProject.projectDir, "node_modules/react-native-reanimated/android")
