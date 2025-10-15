export default ({ config }) => {
  // Base plugins
  config.plugins = config.plugins || [];
  config.plugins.push(["expo-updates", { username: "Technik-Team" }]);
  // REMOVED: The expo-passkeys plugin is removed to fix the native build.
  // config.plugins.push("expo-passkeys");


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
    // REMOVED: The widgets array is now handled natively.
    intentFilters: [
      {
        action: "VIEW",
        autoVerify: true,
        data: [
          { scheme: "https", host: "technikteam.qs0.de" },
          { scheme: "https", host: "technikteamdev.qs0.de" },
          // Add intent filter for your app's custom scheme
          { scheme: "technikteam" }
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
      "webcredentials:technikteam.qs0.de",
      "webcredentials:technikteamdev.qs0.de",
    ],
    "usesAppleSignIn": true
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