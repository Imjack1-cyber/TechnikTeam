const { Platform } = require('react-native');

// Dispatcher for platform-specific passkey services
const impl = Platform.OS === 'web'
  ? require('./passkeyService.web.js')
  : require('./passkeyService.native.js');

module.exports = {
  passkeyService: impl.passkeyService,
};
