// This dynamic configuration function ensures that native-only plugins
// are not included when the config is evaluated in a non-native context.
// It correctly references the '@bittingz/expo-widgets' package.
import AsyncStorage from '@react-native-async-storage/async-storage';

// Centralized widget configuration
const widgetConfig = [
  {
    name: "UpcomingEventWidget",
    label: "Nächster Einsatz",
    description: "Zeigt deinen nächsten zugewiesenen Event an.",
    updateInterval: 1800000, // 30 minutes
    component: "./src/widgets/UpcomingEventWidget.jsx",
  },
  {
    name: "OpenTasksWidget",
    label: "Offene Aufgaben",
    description: "Zeigt deine offenen Aufgaben aus laufenden Events.",
    updateInterval: 1800000, // 30 minutes
    component: "./src/widgets/OpenTasksWidget.jsx",
  },
  {
    name: "AdminActionsWidget",
    label: "Admin Schnellzugriff",
    description: "Schnellzugriff auf Admin-Funktionen.",
    updateInterval: 3600000, // 1 hour
    component: "./src/widgets/AdminActionsWidget.jsx",
  },
  {
    name: "AnnouncementsWidget",
    label: "Anschlagbrett",
    description: "Zeigt die neueste Mitteilung vom Anschlagbrett.",
    updateInterval: 1800000, // 30 minutes
    component: "./src/widgets/AnnouncementsWidget.jsx",
  }
];

// This function tells the native widget renderer how to get its data.
// It reads the persisted state from the Zustand store in AsyncStorage.
const getWidgetData = async () => {
    try {
        const persistedState = await AsyncStorage.getItem('widget-storage');
        if (!persistedState) {
            console.log('[getWidgetData] No persisted widget state found.');
            return {};
        }

        const state = JSON.parse(persistedState).state;
        console.log('[getWidgetData] Successfully loaded widget state from AsyncStorage:', state);

        return {
            UpcomingEventWidget: { props: { nextEvent: state.nextEvent, error: state.error } },
            OpenTasksWidget: { props: { tasks: state.openTasks, error: state.error } },
            AnnouncementsWidget: { props: { announcement: state.latestAnnouncement, error: state.error } },
            // AdminActionsWidget has no dynamic props
        };
    } catch (e) {
        console.error('[getWidgetData] Failed to read widget data from AsyncStorage:', e);
        return {};
    }
};

module.exports = ({ config }) => {
  // Base plugins applicable to all platforms
  const plugins = [
    [
      "expo-updates",
      {
        "username": "Technik-Team",
      },
    ],
    // The widget plugin is always included. Configuration is passed as the second element.
    ["@bittingz/expo-widgets", { widgets: widgetConfig, getData: getWidgetData }],
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
    ...(config.android || {}),
    package: "de.technikteam",
    googleServicesFile: process.env.GOOGLE_SERVICES_JSON ?? "./google-services.json",
    adaptiveIcon: {
      foregroundImage: "./assets/adaptive-icon.png",
      backgroundColor: "#ffffff",
    },
    notification: {
      icon: "./assets/notification-icon.png",
      color: "#ffffff"
    },
  };
  config.ios = {
    ...(config.ios || {}),
    bundleIdentifier: "de.technikteam",
    googleServicesFile:
      process.env.GOOGLE_SERVICES_INFO_PLIST ?? "./GoogleService-Info.plist",
  };
  config.web = {
    ...(config.web || {}),
    favicon: "./assets/favicon.png",
    bundler: "metro",
    notification: {
      vapidPublicKey:
        "BAv_VgqykjTTPK53NZHllECPvkkMkdJFos3buGrlZOGD_T1WY6GebGRe-N2FFmDlOybMgpppTJjuaiXBGLfQEJU",
    },
  };
  config.plugins = plugins;
  config.extra = {
    ...(config.extra || {}),
    eas: {
      projectId: "f362ae37-0995-4578-b240-654bb4a07a72",
    },
  };
  config.owner = "technikteamnobs";

  // Return the modified config object
  return config;
};