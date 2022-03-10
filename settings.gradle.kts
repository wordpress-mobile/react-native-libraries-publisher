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
include(":react-native-gesture-handler")
project(":react-native-gesture-handler").projectDir = File(rootProject.projectDir, "node_modules/react-native-gesture-handler/android")
