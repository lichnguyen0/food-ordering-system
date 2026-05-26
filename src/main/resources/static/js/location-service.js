/**
 * LocationService - Handles geolocation, geocoding, and distance calculations for LunoFood
 * Uses browser Geolocation API + OpenStreetMap Nominatim + OSRM
 */
class LocationService {
    constructor() {
        this.cachedLocation = null;
        this.cacheExpiry = 5 * 60 * 1000; // 5 minutes cache
    }

    /**
     * Get current user position using browser Geolocation API
     * @returns {Promise<{lat: number, lng: number}>}
     */
    async getCurrentPosition() {
        return new Promise((resolve, reject) => {
            if (!navigator.geolocation) {
                reject(new Error('Geolocation is not supported by this browser'));
                return;
            }

            navigator.geolocation.getCurrentPosition(
                (position) => {
                    const location = {
                        lat: position.coords.latitude,
                        lng: position.coords.longitude
                    };
                    this.cachedLocation = { ...location, timestamp: Date.now() };
                    resolve(location);
                },
                (error) => {
                    let message = 'Unable to retrieve your location';
                    switch (error.code) {
                        case error.PERMISSION_DENIED:
                            message = 'Location permission denied';
                            break;
                        case error.POSITION_UNAVAILABLE:
                            message = 'Location information is unavailable';
                            break;
                        case error.TIMEOUT:
                            message = 'Location request timed out';
                            break;
                    }
                    reject(new Error(message));
                },
                {
                    enableHighAccuracy: true,
                    timeout: 10000,
                    maximumAge: 300000 // 5 minutes
                }
            );
        });
    }

    /**
     * Get cached location if still valid
     */
    getCachedLocation() {
        if (this.cachedLocation && 
            (Date.now() - this.cachedLocation.timestamp) < this.cacheExpiry) {
            return {
                lat: this.cachedLocation.lat,
                lng: this.cachedLocation.lng
            };
        }
        return null;
    }

    /**
     * Geocode an address string to coordinates using Nominatim
     * @param {string} address 
     * @returns {Promise<{lat: number, lng: number, displayName: string}>}
     */
    async geocodeAddress(address) {
        if (!address || address.trim().length < 3) {
            throw new Error('Address is too short');
        }

        const encodedAddress = encodeURIComponent(address.trim());
        const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodedAddress}&limit=1&addressdetails=1&countrycodes=vn`;

        try {
            const response = await fetch(url, {
                headers: {
                    'User-Agent': 'LunoFood/1.0 (https://lunofood.vn)',
                    'Accept': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`Geocoding failed: ${response.status}`);
            }

            const results = await response.json();
            
            if (results.length === 0) {
                throw new Error('Address not found');
            }

            const result = results[0];
            return {
                lat: parseFloat(result.lat),
                lng: parseFloat(result.lon),
                displayName: result.display_name
            };
        } catch (error) {
            console.error('Geocoding error:', error);
            throw new Error('Failed to geocode address: ' + error.message);
        }
    }

    /**
     * Reverse geocode: convert coordinates to a human-readable address.
     * Uses Nominatim reverse endpoint.
     * 
     * @param {number} lat 
     * @param {number} lng 
     * @returns {Promise<{displayName: string, address: object}>}
     */
    async reverseGeocode(lat, lng) {
        const url = `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}&addressdetails=1&zoom=16&accept-language=vi`;

        try {
            const response = await fetch(url, {
                headers: {
                    'User-Agent': 'LunoFood/1.0 (https://lunofood.vn)',
                    'Accept': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`Reverse geocoding failed: ${response.status}`);
            }

            const result = await response.json();

            if (!result || !result.display_name) {
                throw new Error('No address found for this location');
            }

            return {
                displayName: result.display_name,
                address: result.address || {},
                lat: parseFloat(result.lat),
                lng: parseFloat(result.lon)
            };
        } catch (error) {
            console.error('Reverse geocoding error:', error);
            // Fallback to a generic string
            return {
                displayName: `Vị trí hiện tại (${lat.toFixed(4)}, ${lng.toFixed(4)})`,
                address: {},
                lat,
                lng
            };
        }
    }

    /**
     * Calculate straight-line distance using Haversine formula (client-side)
     * For more accurate routing distance, use calculateRouteDistance
     */
    calculateHaversineDistance(lat1, lon1, lat2, lon2) {
        const R = 6371; // Earth radius in km
        const dLat = this.toRad(lat2 - lat1);
        const dLon = this.toRad(lon2 - lon1);
        const a = 
            Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(this.toRad(lat1)) * Math.cos(this.toRad(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2);
        const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    toRad(deg) {
        return deg * (Math.PI / 180);
    }

    /**
     * Get actual driving distance and time using OSRM
     * @returns {Promise<{distance: number, duration: number}>} distance in km, duration in minutes
     */
    async calculateRouteDistance(lat1, lon1, lat2, lon2) {
        const url = `https://router.project-osrm.org/route/v1/driving/${lon1},${lat1};${lon2},${lat2}?overview=false`;

        try {
            const response = await fetch(url, {
                headers: { 'User-Agent': 'LunoFood/1.0' }
            });

            if (!response.ok) {
                throw new Error('Routing service unavailable');
            }

            const data = await response.json();
            
            if (data.code !== 'Ok' || !data.routes || data.routes.length === 0) {
                throw new Error('No route found');
            }

            const route = data.routes[0];
            return {
                distance: route.distance / 1000, // meters to km
                duration: Math.round(route.duration / 60) // seconds to minutes
            };
        } catch (error) {
            console.warn('OSRM routing failed, falling back to Haversine:', error);
            // Fallback to straight-line + 30% buffer for roads
            const straightLine = this.calculateHaversineDistance(lat1, lon1, lat2, lon2);
            return {
                distance: straightLine * 1.3,
                duration: Math.round(straightLine * 1.3 * 3) // ~3 min per km estimate
            };
        }
    }

    /**
     * Format distance for display
     */
    formatDistance(km) {
        if (km < 1) {
            return (km * 1000).toFixed(0) + ' m';
        }
        return km.toFixed(1) + ' km';
    }

    /**
     * Format time for display
     */
    formatTime(minutes) {
        if (minutes < 60) {
            return minutes + ' phút';
        }
        const hours = Math.floor(minutes / 60);
        const mins = minutes % 60;
        return hours + ' giờ ' + (mins > 0 ? mins + ' phút' : '');
    }

    /**
     * Store location in sessionStorage for the current session
     */
    storeLocation(lat, lng) {
        try {
            sessionStorage.setItem('userLocation', JSON.stringify({
                lat, lng, timestamp: Date.now()
            }));
        } catch (e) {
            // sessionStorage might be unavailable
        }
    }

    /**
     * Retrieve stored location
     */
    getStoredLocation() {
        try {
            const stored = sessionStorage.getItem('userLocation');
            if (stored) {
                const data = JSON.parse(stored);
                // Valid for 30 minutes
                if (Date.now() - data.timestamp < 30 * 60 * 1000) {
                    return { lat: data.lat, lng: data.lng };
                }
            }
        } catch (e) {}
        return null;
    }

    /**
     * Main helper: Try to get user location (cached → stored → geolocation)
     */
    async detectUserLocation() {
        // 1. Check memory cache
        let location = this.getCachedLocation();
        if (location) return location;

        // 2. Check session storage
        location = this.getStoredLocation();
        if (location) {
            this.cachedLocation = { ...location, timestamp: Date.now() };
            return location;
        }

        // 3. Try browser geolocation
        try {
            location = await this.getCurrentPosition();
            this.storeLocation(location.lat, location.lng);
            return location;
        } catch (error) {
            console.log('Could not get user location:', error.message);
            throw error;
        }
    }
}

// Export as global
window.LocationService = LocationService;