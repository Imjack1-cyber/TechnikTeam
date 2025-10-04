// This dynamic configuration function ensures that native-only plugins
// are not included when the config is evaluated in a non-native context.
// It correctly references the '@bittingz/expo-widgets' package.

module.exports = ({ config }) => {
  // Determine the platform from environment variables set by Expo CLI and EAS Build.
  // This will be 'android', 'ios', or 'web' in build contexts, and undefined otherwise.
  const platform = process.env.EAS_BUILD_PLATFORM || process.env.EXPO_PLATFORM;

  // Base plugins applicable to all platforms
  const plugins = [
    [
      "expo-updates",
      {
        "username": "Technik-Team",
      },
    ],
  ];

  // Conditionally add the native-only '@bittingz/expo-widgets' plugin
  if (platform === 'android' || platform === 'ios') {
    plugins.push([
      "@bittingz/expo-widgets",
      {
        widgets: [
          {
            name: "UpcomingEventWidget",
            label: "Nächster Einsatz",
            description: "Zeigt deinen nächsten zugewiesenen Event an.",
            updateInterval: 1800000
          },
          {
            name: "OpenTasksWidget",
            label: "Offene Aufgaben",
            description: "Zeigt deine offenen Aufgaben aus laufenden Events.",
            updateInterval: 1800000
          },
          {
            name: "AdminActionsWidget",
            label: "Admin Schnellzugriff",
            description: "Schnellzugriff auf Admin-Funktionen.",
            updateInterval: 3600000
          },
          {
            name: "AnnouncementsWidget",
            label: "Anschlagbrett",
            description: "Zeigt die neueste Mitteilung vom Anschlagbrett.",
            updateInterval: 1800000
          }
        ]
      }
    ]);
  }

  // Return the complete, final configuration object.
  return {
    ...config,
    name: "TechnikTeam",
    slug: "technikteam",
    version: "1.0.0",
    orientation: "portrait",
    icon: "./assets/icon.png",
    userInterfaceStyle: "automatic",
    splash: {
      image: "./assets/splash.png",
      resizeMode: "contain",
      backgroundColor: "#ffffff",
    },
    assetBundlePatterns: ["**/*"],
    android: {
      ...config.android,
      package: "de.technikteam",
      googleServicesFile: process.env.GOOGLE_SERVICES_JSON ?? "./google-services.json",
      adaptiveIcon: {
        foregroundImage: "./assets/adaptive-icon.png",
        backgroundColor: "#ffffff",
      },
      notification: {
        icon: "./assets/notification-icon.png",
      },
    },
    ios: {
      ...config.ios,
      bundleIdentifier: "de.technikteam",
      googleServicesFile:
        process.env.GOOGLE_SERVICES_INFO_PLIST ?? "./GoogleService-Info.plist",
    },
    web: {
      ...config.web,
      favicon: "./assets/favicon.png",
      bundler: "metro",
      notification: {
        vapidPublicKey:
          "BAv_VgqykjTTPK53NZHllECPvkkMkdJFos3buGrlZOGD_T1WY6GebGRe-N2FFmDlOybMgpppTJjuaiXBGLfQEJU",
      },
    },
    plugins: plugins,
    extra: {
      ...config.extra,
      eas: {
        projectId: "f362ae37-0995-4578-b240-654bb4a07a72",
      },
    },
    owner: "technikteamnobs",
  };
};