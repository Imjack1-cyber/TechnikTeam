# React Native Frontend for TechnikTeam

This directory contains the React Native source code for the TechnikTeam mobile application, built with **Expo**.

## Getting Started

Follow the "Expo Go Quickstart" guide on the official React Native documentation for your development OS.

1.  **Prerequisites:** Ensure you have Node.js, Watchman, the React Native CLI, JDK, and either Android Studio (for Android emulator) or Xcode (for iOS simulator) installed. You will also need the Expo Go app on your physical device or emulator/simulator.

2.  **Install Dependencies:**
    ```shell
    npm install
    ```

3.  **Install Peer Dependencies for Expo:** Some libraries require native code. Install them using the `expo install` command to ensure version compatibility.
    ```shell
    npx expo install expo-av expo-document-picker expo-file-system expo-sharing expo-notifications expo-device
    ```

4.  **Configure Backend URL:**
    The application now supports switching between a `prod` and `dev` backend directly from the login screen. No manual code changes are needed to set the URL.

5.  **Run the Application:**

    **For Android:**
    ```shell
    npm run android
    ```

    **For iOS:**
    ```shell
    npm run ios
    ```

    **For Web (Development Server):**
    ```shell
    npm run web
    ```

## Firebase Cloud Messaging (Push Notifications) Setup

For push notifications to work on native devices, you must provide the necessary configuration files in this (`frontend`) directory.

1.  **Create Firebase Project:** Create a new project on the [Firebase Console](https://console.firebase.google.com/).
2.  **Get Expo Project ID:** If you haven't already, create a project on [expo.dev](https://expo.dev). Find your Project ID from the dashboard. Open `app.json` and replace `"YOUR_EXPO_PROJECT_ID_HERE"` with your actual ID. This is required for Expo's push notification service to work.
3.  **Android:**
    -   In your Firebase project, add an Android app with the package name `de.technikteam`.
    -   Download the generated `google-services.json` file.
    -   **Place this file in the root of the `frontend` directory (`frontend/google-services.json`).**
4.  **iOS:**
    -   Add an iOS app with the bundle identifier `de.technikteam`.
    -   Download the generated `GoogleService-Info.plist` file.
    -   **Place this file in the root of the `frontend` directory (`frontend/GoogleService-Info.plist`).**
5.  **Server Authentication:** The backend requires its own credentials to send notifications. See the main project `README.md` for instructions on how to set up the server-side authentication using the `GOOGLE_APPLICATION_CREDENTIALS` environment variable.

## Troubleshooting

### Native Build Errors (`Plugin not found`, `Could not get unknown property`)
If you encounter these errors after installing dependencies or changing versions, your native project folders (`android` and `ios`) may be out of sync. To fix this:

1.  Delete the `android` and `ios` directories from the `frontend` folder.
2.  Run the prebuild command to regenerate them cleanly:
    ```shell
    npx expo prebuild --clean
    ```
3.  Then, try running the application again.

### Android SDK XML / CXX5304 Errors
If you see errors like `SDK XML versions up to 3 but an SDK XML file of version 4 was encountered` during an Android build, it means there's a mismatch between your installed Android SDK tools and the version expected by a native dependency.

1.  Open the `frontend/android/build.gradle` file.
2.  Ensure the `plugins` block at the top of the file exists and specifies the Android Gradle Plugin version, like this:
    ```groovy
    plugins {
      id 'com.android.application' version '8.2.1' apply false
      id 'com.android.library' version '8.2.1' apply false
      id 'org.jetbrains.kotlin.android' version '1.8.10' apply false
    }
    ```
3.  Run `npx expo prebuild --clean` again to apply the changes.

**IMPORTANT:** Always add new Expo packages using the `npx expo install` command, not `npm install`. This ensures you get a version of the package that is compatible with your Expo SDK version.

## Key Libraries

-   **Navigation:** React Navigation
-   **State Management:** Zustand
-   **API Communication:** Fetch API
-   **UI Components:** React Native core components, @expo/vector-icons, react-native-calendars, etc.
-   **Audio:** expo-av
-   **File System:** expo-document-picker, expo-file-system, expo-sharing
-   **Storage:** @react-native-async-storage/async-storage