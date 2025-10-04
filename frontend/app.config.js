// This dynamic configuration function ensures that native-only plugins
// are not included when the config is evaluated in a non-native context.
// It explicitly defines all required metadata to satisfy EAS Build requirements.
// The configuration is exported as a static object to ensure compatibility with all versions of expo-cli and eas-cli.

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

// Conditionally add the native-only 'expo-widgets' plugin
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

module.exports = {
  expo: {
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
      bundleIdentifier: "de.technikteam",
      googleServicesFile:
        process.env.GOOGLE_SERVICES_INFO_PLIST ?? "./GoogleService-Info.plist",
    },

    web: {
      favicon: "./assets/favicon.png",
      bundler: "metro",
      notification: {
        vapidPublicKey:
          "BAv_VgqykjTTPK53NZHllECPvkkMkdJFos3buGrlZOGD_T1WY6GebGRe-N2FFmDlOybMgpppTJjuaiXBGLfQEJU",
      },
    },

    plugins: plugins,

    extra: {
      eas: {
        projectId: "f362ae37-0995-4578-b240-654bb4a07a72",
      },
    },

    owner: "technikteamnobs",
  },
};