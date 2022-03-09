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
                        val packageVersion = packageDevDependencies.optString(project.name)
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
