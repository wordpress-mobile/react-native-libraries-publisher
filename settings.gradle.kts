pluginManagement {
    plugins {
        id("com.android.library").version("7.4.2")
        id("com.automattic.android.publish-to-s3").version("0.8.0")
    }
    repositories {
        maven {
            url = uri("https://a8c-libs.s3.amazonaws.com/android")
            content {
                includeGroup("com.automattic.android")
                includeGroup("com.automattic.android.publish-to-s3")
            }
        }
        gradlePluginPortal()
        google()
    }
}

include(":react-native-get-random-values")
project(":react-native-get-random-values").projectDir = File(rootProject.projectDir, "node_modules/react-native-get-random-values/android")
include(":react-native-safe-area-context")
project(":react-native-safe-area-context").projectDir = File(rootProject.projectDir, "node_modules/react-native-safe-area-context/android")
include(":react-native-screens")
project(":react-native-screens").projectDir = File(rootProject.projectDir, "node_modules/react-native-screens/android")
include(":react-native-svg")
project(":react-native-svg").projectDir = File(rootProject.projectDir, "node_modules/react-native-svg/android")
include(":react-native-webview")
project(":react-native-webview").projectDir = File(rootProject.projectDir, "node_modules/react-native-webview/android")
include(":react-native-masked-view")
project(":react-native-masked-view").projectDir = File(rootProject.projectDir, "node_modules/@react-native-masked-view/masked-view/android")
include(":react-native-clipboard")
project(":react-native-clipboard").projectDir = File(rootProject.projectDir, "node_modules/@react-native-clipboard/clipboard/android")
include(":react-native-fast-image")
project(":react-native-fast-image").projectDir = File(rootProject.projectDir, "node_modules/react-native-fast-image/android")
include(":react-native-reanimated")
project(":react-native-reanimated").projectDir = File(rootProject.projectDir, "node_modules/react-native-reanimated/android")
include(":react-native-gesture-handler")
project(":react-native-gesture-handler").projectDir = File(rootProject.projectDir, "node_modules/react-native-gesture-handler/android")
include(":react-native-linear-gradient")
project(":react-native-linear-gradient").projectDir = File(rootProject.projectDir, "node_modules/react-native-linear-gradient/android")
