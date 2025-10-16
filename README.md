# Offline ML Dictionary

A sample Android app that demonstrates how to use Google ML Kit's on-device translation APIs to build a lightweight English→Chinese dictionary. The UI is built with Jetpack Compose and Material 3, and the translation workflow runs entirely on device after the offline model is downloaded.

## Features

- Downloads the English-to-Chinese translation model on first launch and exposes a retry option when the download fails.
- Provides a clean Compose-based interface for entering English text, triggering translation, and reviewing the output.
- Shows translation progress, model status, and friendly empty states to guide the user through the flow.

## Getting started

1. Open the project in Android Studio Iguana or newer.
2. Sync Gradle to download dependencies and the ML Kit translation model.
3. Run the `app` module on a device (API level 26+) or emulator with Google Play services.

The first translation request may take a little longer because ML Kit needs to download the offline model. Subsequent translations run fully offline.
