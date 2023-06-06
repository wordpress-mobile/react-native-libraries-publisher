#!/bin/bash

set -euo pipefail

# Publish each project individually after verifying the specific version is not already published

PROJECTS=(
react-native-get-random-values
react-native-safe-area-context
react-native-screen
react-native-svg
react-native-webview
react-native-masked-view
react-native-clipboard
react-native-fast-image
)

for project in "${PROJECTS[@]}"
do
    EXIT_CODE=0
    if [ "${USE_MAVEN_LOCAL:-False}" == "True" ]; then
        echo "========================="
        echo "Publishing to Maven local"
        echo "Project: $project"
        echo "========================="
        ./gradlew -DDISABLE_PUBLISH_TO_S3=true $project:publishToMavenLocal
    else
        ./gradlew :$project:assertVersionIsNotAlreadyPublished || EXIT_CODE=$?
        # If the project is not published already, publish it
        if [ $EXIT_CODE -eq 0 ]; then
            ./gradlew :$project:publishS3PublicationToS3Repository
        fi
    fi
done
