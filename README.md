# Publish artifacts of React Native dependencies to S3

The main purpose of this repository is to ease the process of adding and upgrading the React Native dependencies of Gutenberg, specifically, the part the related to publishing the artifacts of the Android libraries of the dependencies.

**For now, this repository only supports dependencies that don’t have a forked repository, so when adding a package note that we can only use versions from the NPM registry (e.g. `"react-native-get-random-values": "1.4.0"`).**

**NOTE:** This setup might not work for all dependencies. In the future, it might require further modifications to get new packages published.

## How to publish an artifact of a dependency

### When it's a new package
1. Add the package to the `devDependencies` section of the `package.json` file.

**Example:**
`"react-native-get-random-values": "1.4.0"`
2. Add the Android project of the dependency to the `settings.gradle.kts` file.

**Example:**
```
include(":react-native-get-random-values")
project(":react-native-get-random-values").projectDir = File(rootProject.projectDir, "node_modules/react-native-get-random-values/android")
```
3. Run command `npm install` to install the new package.
4. Run command `./gradlew publishToMavenLocal` and check in `$HOME/.m2/repository` location that the dependency was created successfully with its artifact.
5. TBD [How the Buildkite pipeline will publish it to S3]

### When upgrading a package
1. Update the version of the package in the `package.json` file.
2. Run command `npm install` to install the specified version of the package.
3. Run command `./gradlew publishToMavenLocal` and check in `$HOME/.m2/repository` location that the dependency was created successfully with its artifact.
5. TBD [How the Buildkite pipeline will publish it to S3]

## How to include the artifact in Gutenberg

Add a `implementation` statement referencing the package into the following files in Gutenberg:

- `packages/react-native-bridge/android/react-native-bridge/build.gradle`
- `packages/react-native-editor/android/app/build.gradle`

**Example**:
```
implementation "org.wordpress-mobile:react-native-get-random-values:${extractPackageVersion(packageJson, 'react-native-get-random-values', 'dependencies')}"
```