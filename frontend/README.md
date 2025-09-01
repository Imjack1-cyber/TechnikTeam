# React Native Frontend for TechnikTeam

This directory contains the React Native source code for the TechnikTeam mobile application, built with **Expo**.

## Getting Started

Follow the "Expo Go Quickstart" guide on the official React Native documentation for your development OS.

1.  **Prerequisites:** Ensure you have Node.js and either Android Studio (for Android emulator) or Xcode (for iOS simulator) installed. You will also need the Expo Go app on your physical device or emulator/simulator.

2.  **Install Dependencies:**
    ```shell
    npm install
    ```

3.  **Install Peer Dependencies for Expo:** Some libraries require native code. Install them using the `expo install` command to ensure version compatibility.
    ```shell
    npx expo install expo-av
    ```

4.  **Configure Backend URL:**
    Open `frontend/src/services/apiClient.js` and `frontend/src/hooks/useWebSocket.js`. Update the `BASE_URL` and `WS_BASE_URL` constants to point to your running Spring Boot backend.
    - For Android emulators, `http://10.0.2.2:PORT` is typically used to refer to the host machine's localhost.
    - For physical devices, use your machine's local network IP address.

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

## Key Libraries

-   **Navigation:** React Navigation
-   **State Management:** Zustand
-   **API Communication:** Fetch API
-   **UI Components:** React Native core components, react-native-vector-icons, react-native-calendars, etc.
-   **Audio:** expo-av
-   **Storage:** @react-native-async-storage/async-storage