import { Platform } from 'react-native';
import { passkeyService as webService } from './passkeyService.web.js';
import { passkeyService as nativeService } from './passkeyService.native.js';

export const passkeyService = Platform.select({
    web: webService,
    default: nativeService,
});