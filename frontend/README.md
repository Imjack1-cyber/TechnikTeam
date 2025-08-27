# React Native Frontend for TechnikTeam

This directory contains the React Native source code for the TechnikTeam mobile application.

## Getting Started

Follow the "React Native CLI Quickstart" guide for your development OS and target OS (iOS/Android) on the official React Native documentation.

1.  **Prerequisites:** Ensure you have Node.js, Watchman, the React Native CLI, JDK, and either Android Studio or Xcode installed.

2.  **Install Dependencies:**
    ```shell
    npm install
    ```

3.  **Configure Backend URL:**
    Open `frontend/src/services/apiClient.js` and `frontend/src/hooks/useWebSocket.js`. Update the `BASE_URL` and `WS_BASE_URL` constants to point to your running Spring Boot backend.
    - For Android emulators, `http://10.0.2.2:PORT` is typically used to refer to the host machine's localhost.
    - For physical devices, use your machine's local network IP address.

4.  **Run the Application:**

    **For Android:**
    ```shell
    npm run android
    ```

    **For iOS:**
    ```shell
    npm run ios
    ```

## Key Libraries

-   **Navigation:** React Navigation
-   **State Management:** Zustand
-   **API Communication:** Fetch API
-   **UI Components:** React Native core components, react-native-vector-icons, react-native-calendars, etc.
-   **Storage:** @react-native-async-storage/async-storage