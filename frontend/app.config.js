// app.config.js
import withExpoWidgets from './node_modules/@bittingz/expo-widgets/plugin/build/index.js';

const widgetConfig = [
  {
    name: "UpcomingEventWidget",
    label: "Nächster Einsatz",
    description: "Zeigt deinen nächsten zugewiesenen Event an.",
    updateInterval: 1800000,
    component: "./src/widgets/UpcomingEventWidget.jsx",
  },
  {
    name: "OpenTasksWidget",
    label: "Offene Aufgaben",
    description: "Zeigt deine offenen Aufgaben aus laufenden Events.",
    updateInterval: 1800000,
    component: "./src/widgets/OpenTasksWidget.jsx",
  },
  {
    name: "AdminActionsWidget",
    label: "Admin Schnellzugriff",
    description: "Schnellzugriff auf Admin-Funktionen.",
    updateInterval: 3600000,
    component: "./src/widgets/AdminActionsWidget.jsx",
  },
  {
    name: "AnnouncementsWidget",
    label: "Anschlagbrett",
    description: "Zeigt die neueste Mitteilung vom Anschlagbrett.",
    updateInterval: 1800000,
    component: "./src/widgets/AnnouncementsWidget.jsx",
  },
];

export default ({ config }) => {
  // Apply the widgets plugin first
  config = withExpoWidgets(config, { widgets: widgetConfig });

  // Base plugins
  config.plugins = config.plugins || [];
  config.plugins.push(["expo-updates", { username: "Technik-Team" }]);

  // General app config
  config.name = "TechnikTeam";
  config.slug = "technikteam";
  config.scheme = "technikteam";
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

  // Android config
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
      color: "#ffffff",
    },
    intentFilters: [
      {
        action: "VIEW",
        autoVerify: true,
        data: [
          { scheme: "https", host: "technikteam.qs0.de" },
          { scheme: "https", host: "technikteamdev.qs0.de" },
        ],
        category: ["BROWSABLE", "DEFAULT"],
      },
    ],
  };

  // iOS config
  config.ios = {
    ...(config.ios || {}),
    bundleIdentifier: "de.technikteam",
    googleServicesFile: process.env.GOOGLE_SERVICES_INFO_PLIST ?? "./GoogleService-Info.plist",
    associatedDomains: [
      "applinks:technikteam.qs0.de",
      "applinks:technikteamdev.qs0.de",
    ],
  };

  // Web config
  config.web = {
    ...(config.web || {}),
    favicon: "./assets/favicon.png",
    bundler: "metro",
    notification: {
      vapidPublicKey:
        "BAv_VgqykjTTPK53NZHllECPvkkMkdJFos3buGrlZOGD_T1WY6GebGRe-N2FFmDlOybMgpppTJjuaiXBGLfQEJU",
    },
  };

  // EAS/extra config
  config.extra = {
    ...(config.extra || {}),
    eas: {
      projectId: "f362ae37-0995-4578-b240-654bb4a07a72",
    },
  };

  config.owner = "technikteamnobs";

  return config;
};
