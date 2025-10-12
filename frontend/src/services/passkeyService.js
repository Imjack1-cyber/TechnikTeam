const { Platform } = require('react-native');

// This file acts as a dynamic dispatcher for platform-specific passkey services.
if (Platform.OS === 'web') {
    module.exports = require('./passkeyService.web.js');
} else {
    module.exports = require('./passkeyService.native.js');
}
