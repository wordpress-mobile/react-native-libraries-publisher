#!/bin/bash

set -euo pipefail

# Publish each project individually after verifying the specific version is not already published

PROJECTS=(
react-native-get-random-values
react-native-reanimate
react-native-safe-area-context
react-native-screen
react-native-svg
react-native-webview
react-native-masked-view
react-native-clipboard
react-native-gesture-handler
)

for project in "${PROJECTS[@]}"
do
    EXIT_CODE=0
    ./gradlew :$project:assertVersionIsNotAlreadyPublished || EXIT_CODE=$?
    # If the project is not published already, publish it
    if [ $EXIT_CODE -eq 0 ]; then
        ./gradlew :$project:publishS3PublicationToS3Repository
    fi
done
