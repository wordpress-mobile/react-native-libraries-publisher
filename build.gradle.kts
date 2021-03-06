import com.automattic.android.publish.CheckS3Version
import org.json.JSONObject

plugins {
    id("com.android.library") apply false
    id("com.automattic.android.publish-to-s3") apply false
}

val defaultCompileSdkVersion = 30
val defaultMinSdkVersion = 21
val defaultTargetSdkVersion = 30
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

val publishGroupId = "org.wordpress-mobile"

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
                substitute(module("com.facebook.react:react-native"))
                    .with(module("com.facebook.react:react-native:$reactNativeVersion"))
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
                        // version is set by 'publish-to-s3' plugin

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
