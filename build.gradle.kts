plugins {
    id("com.github.node-gradle.node") version "3.1.1"
    id("com.android.library") apply false
}

node {
    download.set(true)
    version.set("16.14.0")
}

val reactNativeVersion = "0.66.2"
val defaultCompileSdkVersion = 30
val defaultMinSdkVersion = 21
val defaultTargetSdkVersion = 30

// Set project extra properties
project.ext.set("compileSdkVersion", defaultCompileSdkVersion)
project.ext.set("minSdkVersion", defaultMinSdkVersion)
project.ext.set("targetSdkVersion", defaultTargetSdkVersion)

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
        project(":react-native-get-random-values").afterEvaluate {
            configure<PublishingExtension> {
                publications {
                    create<MavenPublication>("S3") {
                        from(components.get("release"))
                        groupId = "org.wordpress-mobile"
                        artifactId = "react-native-get-random-values"
                        version = "1.4.0"

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
