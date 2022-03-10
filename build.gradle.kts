import org.json.JSONObject

plugins {
    id("com.android.library") apply false
}

val defaultCompileSdkVersion = 30
val defaultMinSdkVersion = 21
val defaultTargetSdkVersion = 30

// Set project extra properties
project.ext.set("compileSdkVersion", defaultCompileSdkVersion)
project.ext.set("minSdkVersion", defaultMinSdkVersion)
project.ext.set("targetSdkVersion", defaultTargetSdkVersion)

// Fetch dependencies versions from package.json
val packageJson = JSONObject(File("$rootDir/package.json").readText())
val packageDevDependencies = packageJson.optJSONObject("devDependencies")

val reactNativeVersion = packageDevDependencies.optString("react-native")

subprojects {
    apply(plugin = "maven-publish")

    configure<PublishingExtension> {
        repositories {
            maven {
                url = uri("s3://a8c-libs.s3.amazonaws.com/android")
                credentials(AwsCredentials::class) {
                    accessKey = System.getenv("AWS_ACCESS_KEY")
                    secretKey = System.getenv("AWS_SECRET_KEY")
                }
            }
        }
    }

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
                        val packageVersion = getPackageVersion(project.name)
                        println("Publishing configuration:\n\tartifactId=\"${project.name}\"\n\tversion=\"$packageVersion\"")

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
                        groupId = "org.wordpress-mobile"
                        artifactId = project.name
                        version = packageVersion

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
}

fun getPackageVersion(projectName: String): String {
    val jsonProperty = when {
        projectName == "react-native-masked-view" -> "@react-native-masked-view/masked-view"
        projectName == "react-native-clipboard" -> "@react-native-clipboard/clipboard"
        else -> projectName
    }
    val packageVersion = packageDevDependencies.optString(jsonProperty)

    // Extract version from filename of tarball URL
    val isTarball = packageVersion.endsWith(".tgz")
    if (isTarball) {
        // Replace special characters of package name as "npm pack" command does, to be used in the filename.
        // Reference: https://github.com/npm/cli/blob/699c2d708d2a24b4f495a74974b2a345f33ee08a/lib/pack.js#L66-L67
        val packageNameSanitized = jsonProperty.replace("@", "").replace("/", "-")
        val fileName = packageVersion.substring(packageVersion.lastIndexOf("/") + 1)
        val version = fileName.replace(packageNameSanitized + "-", "").replace(".tgz", "")
        return version
    }

    return packageVersion
}
