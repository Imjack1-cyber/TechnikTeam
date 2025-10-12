import { Platform } from 'react-native';
import { useAuthStore } from '../store/authStore';
import { useBackendStatusStore } from '../store/backendStatusStore';
import * as FileSystem from 'expo-file-system';
import * as Sharing from 'expo-sharing';
import { getToken } from '../lib/storage';
import { useTransferStore } from '../store/transferStore';

export const MAX_FILE_SIZE_BYTES = 1000 * 1024 * 1024; // 1000 MB

const getApiBaseUrl = () => {
    const mode = useAuthStore.getState().backendMode;
    if (Platform.OS === 'web') {
        return '/TechnikTeam/api/v1';
    }
    const host = mode === 'dev' ? 'technikteamdev.qs0.de' : 'technikteam.qs0.de';
    return `https://${host}/TechnikTeam/api/v1`;
};

const getRootUrl = () => {
    const mode = useAuthStore.getState().backendMode;
    if (Platform.OS === 'web') {
        return '/TechnikTeam';
    }
    const host = mode === 'dev' ? 'technikteamdev.qs0.de' : 'technikteam.qs0.de';
    return `https://${host}/TechnikTeam`;
};

let onUnauthorizedCallback = () => {};
let onMaintenanceCallback = () => {};
let authToken = null;

// Helper for speed/ETA calculation
const createSpeedTracker = () => {
    let lastTime = Date.now();
    let lastLoaded = 0;
    const speedSamples = [];
    const sampleSize = 5;

    return (progress, total) => {
        const now = Date.now();
        const timeDelta = (now - lastTime) / 1000; // in seconds
        const loadedDelta = progress - lastLoaded;

        if (timeDelta > 0.5) { // Update only every 0.5s to get a stable reading
            const currentSpeed = loadedDelta / timeDelta;
            speedSamples.push(currentSpeed);
            if (speedSamples.length > sampleSize) {
                speedSamples.shift();
            }
            lastTime = now;
            lastLoaded = progress;
        }

        const avgSpeed = speedSamples.length > 0 ? speedSamples.reduce((a, b) => a + b, 0) / speedSamples.length : 0;
        const remainingBytes = total - progress;
        const eta = avgSpeed > 0 ? remainingBytes / avgSpeed : Infinity;

        return { speed: avgSpeed, eta };
    };
};

const apiClient = {
	setup: function(callbacks) {
		onUnauthorizedCallback = callbacks.onUnauthorized;
		onMaintenanceCallback = callbacks.onMaintenance;
	},
	setAuthToken: function(token) {
		authToken = token;
	},
    getRootUrl: getRootUrl,
    getBaseUrl: getApiBaseUrl,
	request: async function(endpoint, options = {}) {
		const headers = { ...options.headers };
		
        // For native clients, always use the Authorization header.
        // For web, rely on the HttpOnly cookie and do not send the header.
		if (Platform.OS !== 'web' && authToken) {
			headers['Authorization'] = `Bearer ${authToken}`;
		}
		
		if (!(options.body instanceof FormData)) {
			headers['Content-Type'] = 'application/json';
		}
        // IMPORTANT: For FormData, we do NOT set the Content-Type header.
        // The `fetch` API (on both web and native) will automatically set it
        // to 'multipart/form-data' and add the required 'boundary' parameter.
        // Manually setting it here would break the request.

		try {
            const baseUrl = getApiBaseUrl();
			const response = await fetch(`${baseUrl}${endpoint}`, {
				...options,
				headers: headers,
			});
			const contentType = response.headers.get("content-type");
			const isJson = contentType && contentType.includes("application/json");

            if (response.status === 502) {
                useBackendStatusStore.getState().setIsBackendDown(true);
                throw new Error('Das Backend ist zurzeit nicht erreichbar (502 Bad Gateway).');
            }
			if (response.status === 503) {
				onMaintenanceCallback();
				throw new Error('Die Anwendung befindet sich im Wartungsmodus.');
			}
			if (response.status === 401) {
                await onUnauthorizedCallback();
                const authError = new Error('Session expired and user logged out.');
                authError.isAuthError = true;
                throw authError;
			}
			if (response.status === 403) {
				if (isJson) {
					const errorResult = await response.json();
					throw new Error(errorResult.message || 'Zugriff verweigert.');
				}
				throw new Error('Zugriff verweigert.');
			}
			if (response.status === 204) {
				return { success: true, message: 'Operation successful.', data: null };
			}
			if (!isJson) {
				const textError = await response.text();
				console.error("Non-JSON API response:", textError);
				throw new Error(`Serververbindung fehlgeschlagen (Status: ${response.status}).`);
			}
			const result = await response.json();
			if (!response.ok) {
                // Prioritize the message from the backend JSON response
                if (result.message) {
                    throw new Error(result.message);
                }
				if (response.status >= 500) {
					throw new Error("Ein interner Serverfehler ist aufgetreten.");
				}
				throw new Error(`Ein Fehler ist aufgetreten (Status: ${response.status})`);
			}
			return result;
		} catch (error) {
			if (error.isAuthError) {
                throw error;
            }
            const baseUrl = getApiBaseUrl();
			if (error.message.includes('Network request failed') || error.message.includes('Failed to fetch')) {
                useBackendStatusStore.getState().setIsBackendDown(true);
				console.error(`API Client Network Error: ${options.method || 'GET'} ${baseUrl}${endpoint}`, error);
				throw new Error('Netzwerkfehler: Das Backend ist nicht erreichbar.');
			}
			console.error(`API Client Error: ${options.method || 'GET'} ${baseUrl}${endpoint}`, error);
			throw error;
		}
	},
    downloadFile: async function(downloadUrl, filename, transferId) {
        const token = await getToken();
        const headers = { Authorization: `Bearer ${token}` };
        const { updateTransfer } = useTransferStore.getState();

        if (Platform.OS === 'web') {
            const controller = new AbortController();
            updateTransfer(transferId, { source: controller });
            
            const response = await fetch(downloadUrl, { headers, signal: controller.signal });
            if (!response.ok) throw new Error('Download-Link ist ungültig oder abgelaufen.');

            const total = parseInt(response.headers.get('Content-Length') || '0', 10);
            updateTransfer(transferId, { total });

            if (!response.body) {
                throw new Error("Response body is not available.");
            }

            const reader = response.body.getReader();
            const chunks = [];
            let loaded = 0;
            const tracker = createSpeedTracker();

            while (true) {
                try {
                    const { done, value } = await reader.read();
                    if (done) break;
                    chunks.push(value);
                    loaded += value.length;
                    const { speed, eta } = tracker(loaded, total);
                    updateTransfer(transferId, { progress: loaded, speed, eta, status: 'progressing' });
                } catch (err) {
                    if (err.name === 'AbortError') {
                        console.log('Download aborted by user.');
                        throw err; // Re-throw to be caught by outer catch block
                    }
                    throw err;
                }
            }
            
            const blob = new Blob(chunks);
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = filename;
            document.body.appendChild(a);
            a.click();
            a.remove();
            window.URL.revokeObjectURL(url);
            updateTransfer(transferId, { status: 'completed' });
        } else {
            // Native logic with progress callback
            const fileUri = FileSystem.documentDirectory + filename.replace(/[^a-zA-Z0-9.\-_]/g, '_');
            const tracker = createSpeedTracker();
            const downloadResumable = FileSystem.createDownloadResumable(
                downloadUrl,
                fileUri,
                { headers },
                (progress) => {
                    const { speed, eta } = tracker(progress.totalBytesWritten, progress.totalBytesExpectedToWrite);
                    updateTransfer(transferId, {
                        progress: progress.totalBytesWritten,
                        total: progress.totalBytesExpectedToWrite,
                        speed,
                        eta,
                        status: 'progressing'
                    });
                }
            );
            updateTransfer(transferId, { source: { abort: () => downloadResumable.pauseAsync() }}); // Adapt abort for resumable
            try {
                const { uri } = await downloadResumable.downloadAsync();
                updateTransfer(transferId, { fileUri: uri, status: 'completed' });

                if (await Sharing.isAvailableAsync()) {
                    await Sharing.shareAsync(uri, { dialogTitle: filename });
                }
            } catch (e) {
                if (e.message && e.message.includes('paused')) {
                    console.log(`Download ${transferId} cancelled by user.`);
                } else {
                    throw e; // Re-throw other errors
                }
            }
        }
    },
	uploadWithProgress: function(endpoint, formData, transferId) {
        return new Promise(async (resolve, reject) => {
            const xhr = new XMLHttpRequest();
            const { updateTransfer } = useTransferStore.getState();
            updateTransfer(transferId, { source: xhr });

            const tracker = createSpeedTracker();
            
            xhr.upload.onprogress = (event) => {
                if (event.lengthComputable) {
                    const { speed, eta } = tracker(event.loaded, event.total);
                    updateTransfer(transferId, {
                        progress: event.loaded,
                        total: event.total,
                        speed,
                        eta,
                        status: 'progressing'
                    });
                }
            };
            
            xhr.onload = () => {
                if (xhr.status >= 200 && xhr.status < 300) {
                    resolve(JSON.parse(xhr.responseText));
                } else {
                    try {
                        reject(new Error(JSON.parse(xhr.responseText).message || `HTTP Error: ${xhr.status}`));
                    } catch {
                        reject(new Error(`HTTP Error: ${xhr.status}`));
                    }
                }
            };
            
            xhr.onerror = () => reject(new Error('Network request failed'));
            xhr.onabort = () => reject(new Error('Upload canceled'));

            const url = `${this.getBaseUrl()}${endpoint}`;
            xhr.open('POST', url, true);
            
            if (Platform.OS !== 'web') {
                const token = await getToken();
                if (token) {
                    xhr.setRequestHeader('Authorization', `Bearer ${token}`);
                }
            }
            
            xhr.send(formData);
        });
    },
	get(endpoint) {
		return this.request(endpoint, { method: 'GET' });
	},
	post(endpoint, body) {
		const options = { method: 'POST', body: body instanceof FormData ? body : JSON.stringify(body) };
		return this.request(endpoint, options);
	},
	put(endpoint, body) {
		return this.request(endpoint, { method: 'PUT', body: JSON.stringify(body) });
	},
	delete(endpoint) {
		return this.request(endpoint, { method: 'DELETE' });
	},
};

export default apiClient;