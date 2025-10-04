// This dynamic configuration function ensures that native-only plugins
// are not included when the config is evaluated in a non-native context.
// It correctly references the '@bittingz/expo-widgets' package.

// Centralized widget configuration
const widgetConfig = [
  {
    name: "UpcomingEventWidget",
    label: "Nächster Einsatz",
    description: "Zeigt deinen nächsten zugewiesenen Event an.",
    updateInterval: 1800000 // 30 minutes
  },
  {
    name: "OpenTasksWidget",
    label: "Offene Aufgaben",
    description: "Zeigt deine offenen Aufgaben aus laufenden Events.",
    updateInterval: 1800000 // 30 minutes
  },
  {
    name: "AdminActionsWidget",
    label: "Admin Schnellzugriff",
    description: "Schnellzugriff auf Admin-Funktionen.",
    updateInterval: 3600000 // 1 hour
  },
  {
    name: "AnnouncementsWidget",
    label: "Anschlagbrett",
    description: "Zeigt die neueste Mitteilung vom Anschlagbrett.",
    updateInterval: 1800000 // 30 minutes
  }
];

module.exports = ({ config }) => {
  // Base plugins applicable to all platforms
  const plugins = [
    [
      "expo-updates",
      {
        "username": "Technik-Team",
      },
    ],
    // The widget plugin is always included for native builds to run its config modifications.
    // It will do nothing on web.
    "@bittingz/expo-widgets",
  ];

  // Overwrite the static config with our dynamic values
  config.name = "TechnikTeam";
  config.slug = "technikteam";
  config.version = "1.0.0";
  config.orientation = "portrait";
  config.icon = "./assets/icon.png";
  config.userInterfaceStyle = "automatic";
  config.splash = {
    image: "./assets/splash.png",
    resizeMode: "contain",
    backgroundColor: "#ffffff",
  };
  config.assetBundlePatterns = ["**/*"];
  config.android = {
    ...config.android,
    package: "de.technikteam",
    googleServicesFile: process.env.GOOGLE_SERVICES_JSON ?? "./google-services.json",
    adaptiveIcon: {
      foregroundImage: "./assets/adaptive-icon.png",
      backgroundColor: "#ffffff",
    },
    // This section configures the small icon that appears in the status bar
    // for push notifications on Android.
    notification: {
      icon: "./assets/notification-icon.png",
      // You can also add a color tint to the notification icon.
      // The icon should be a single-color image with a transparent background.
      color: "#ffffff"
    },
    widgets: widgetConfig,
  };
  config.ios = {
    ...config.ios,
    bundleIdentifier: "de.technikteam",
    googleServicesFile:
      process.env.GOOGLE_SERVICES_INFO_PLIST ?? "./GoogleService-Info.plist",
    // NOTE: iOS does not support a custom small icon for push notifications.
    // It uses a white version of the main app icon.
    widgets: widgetConfig,
  };
  config.web = {
    ...config.web,
    favicon: "./assets/favicon.png",
    bundler: "metro",
    notification: {
      vapidPublicKey:
        "BAv_VgqykjTTPK53NZHllECPvkkMkdJFos3buGrlZOGD_T1WY6GebGRe-N2FFmDlOybMgpppTJjuaiXBGLfQEJU",
    },
  };
  config.plugins = plugins;
  config.extra = {
    ...config.extra,
    eas: {
      projectId: "f362ae37-0995-4578-b240-654bb4a07a72",
    },
  };
  config.owner = "technikteamnobs";

  // Return the modified config object
  return config;
};