import com.automattic.android.publish.CheckS3Version
import org.json.JSONObject

// This value is introduced to control breaking changes to the publisher itself.
// Whenever we need to make a change, such as updating the `compileSdkVersion`, we will want to
// publish the artifacts of the libraries again using the version. However, we don't want to
// override existing artifacts, so we are using a hardcoded value that's supposed to be updated
// after every breaking change.
//
// For example, let's say we have already published the `9.13.6` version of the `react-native-svg`
// library with `compileSdkVersion = 30`. Then, we decided to update the `compileSdkVersion` to 31.
// In this case, we'll want to increment the `publisherVersion` so that the artifacts will be
// published again in a different path.
//
// Although this allows different clients to use different artifacts, since we only have one client
// this is now the most important use case for this implementation. Instead, this implementation
// aims to make it easier to test publisher changes without having to override the artifacts.
val publisherVersion = "v3"

plugins {
    id("com.android.library") apply false
    id("com.automattic.android.publish-to-s3") apply false
}

val defaultCompileSdkVersion = 34
val defaultMinSdkVersion = 24
val defaultTargetSdkVersion = 34
val excludeAppGlideModule = true

// Set project extra properties
project.ext.set("compileSdkVersion", defaultCompileSdkVersion)
project.ext.set("minSdkVersion", defaultMinSdkVersion)
project.ext.set("targetSdkVersion", defaultTargetSdkVersion)
project.ext.set("excludeAppGlideModule", excludeAppGlideModule)

// Fetch dependencies versions from package.json
val packageJson = JSONObject(File("$rootDir/package.json").readText())
val packageDevDependencies = packageJson.optJSONObject("devDependencies")

val reactNativeVersion = packageDevDependencies.optString("react-native")

val publishGroupId = "org.wordpress-mobile.react-native-libraries.$publisherVersion"

subprojects {
    apply(plugin = "maven-publish")
    apply(plugin = "com.automattic.android.publish-to-s3")

    repositories {
        exclusiveContent {
            forRepository {
                maven {
                    url = uri("https://a8c-libs.s3.amazonaws.com/android/react-native-mirror")
                }
            }
            filter {
                includeModule("com.facebook.react", "react-native")
            }
        }

        mavenCentral()
        google()
    }

    configurations.all {
        resolutionStrategy {
            dependencySubstitution {
                // This substitution is based on React Native Gradle plugin.
                // Reference: https://t.ly/38jk
                substitute(module("com.facebook.react:react-android"))
                    .with(module("com.facebook.react:react-android:$reactNativeVersion"))
                substitute(module("com.facebook.react:hermes-android"))
                    .with(module("com.facebook.react:hermes-android:$reactNativeVersion"))
                // For backward-compatibility, we also substitute `react-native` module
                // with the new module `react-android`.
                substitute(module("com.facebook.react:react-native"))
                    .with(module("com.facebook.react:react-android:$reactNativeVersion"))
            }
        }
    }

    afterEvaluate {
        afterEvaluate {
            configure<PublishingExtension> {
                publications {
                    create<MavenPublication>("S3") {
                        if (project.name == "react-native-reanimated" ) {
                            val defaultArtifacts = configurations.getByName("default").artifacts
                            if(defaultArtifacts.isEmpty()) {
                                throw Exception("'$name' - No default artifact found, aborting publishing!")
                            }
                            val defaultArtifact = defaultArtifacts.getFiles().getSingleFile()
                            artifact(defaultArtifact)
                        }
                        else {
                            from(components.get("release"))
                        }
                        groupId = publishGroupId
                        artifactId = project.name

                        // Version is overriden by 'publish-to-s3' plugin, however there seems to be an
                        // edge case where the `.module` metadata file doesn't contain the correct
                        // version information for some libraries (i.e. "react-native-fast-image").
                        // So, we are setting the version information here as well.
                        //
                        // Hopefully we can address this in the `publish-to-s3-gradle-plugin`, but
                        // even after it's fixed in the plugin, this is OK to keep. The reason we 
                        // normally don't set it is to communicate that it'll be overriden, but with
                        // this documentation in place, that's not a problem.
                        version = getPackageVersion(project.name)

                        versionMapping {
                            allVariants {
                                fromResolutionOf("releaseRuntimeClasspath")
                            }
                        }
                    }
                }
            }
        }
    }

    tasks.withType(com.automattic.android.publish.PrepareToPublishToS3Task::class.java) {
        val packageVersion = getPackageVersion(project.name)

        // Override the default behaviour of 'publish-to-s3' plugin since we always want to specify the version
        tagName = packageVersion
    }
    tasks.register("assertVersionIsNotAlreadyPublished") {
        doLast {
            val packageVersion = getPackageVersion(project.name)
            val checkS3Version = CheckS3Version(publishGroupId, project.name, packageVersion)
            if (checkS3Version.check()) {
                throw IllegalStateException("'${project.name}' version '$packageVersion' is already published!")
            }
        }
    }
}

fun getPackageVersion(projectName: String): String {
    val jsonProperty = when {
        projectName == "react-native-masked-view" -> "@react-native-masked-view/masked-view"
        projectName == "react-native-clipboard" -> "@react-native-clipboard/clipboard"
        else -> projectName
    }
    return packageDevDependencies.optString(jsonProperty)
}
