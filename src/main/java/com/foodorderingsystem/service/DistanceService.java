package com.foodorderingsystem.service;

import org.springframework.stereotype.Service;

/**
 * Service to calculate distances between coordinates using Haversine formula.
 */
@Service
public class DistanceService {

    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * Calculate distance between two coordinates using Haversine formula.
     * 
     * @param lat1 Latitude of point 1 in degrees
     * @param lon1 Longitude of point 1 in degrees
     * @param lat2 Latitude of point 2 in degrees
     * @param lon2 Longitude of point 2 in degrees
     * @return Distance in kilometers
     */
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        if (lat1 == 0 || lon1 == 0 || lat2 == 0 || lon2 == 0) {
            return 0.0;
        }

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) 
                 * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS_KM * c;
    }

    /**
     * Calculate delivery fee based on distance.
     * Base fee: 5000 VND, plus 5000 VND per km
     * 
     * @param distanceKm Distance in kilometers
     * @return Delivery fee in VND
     */
    public double calculateDeliveryFee(double distanceKm) {
        if (distanceKm <= 0) {
            return 5000.0;
        }
        return 5000.0 + (distanceKm * 5000.0);
    }

    /**
     * Tính toán thời gian giao hàng ước tính tính bằng phút.
     * Thời gian cơ bản: 15 phút, cộng với 5 phút mỗi km
     * 
     * @param distanceKm Distance in kilometers
     * @return Estimated delivery time in minutes
     */
    public int calculateDeliveryTime(double distanceKm) {
        if (distanceKm <= 0) {
            return 15;
        }
        return 15 + (int) Math.round(distanceKm * 3);
    }

    /**
     * Tiered delivery fee policy (authoritative):
     * 0-3km = 15_000
     * 3-5km = 25_000
     * 5-7km = 35_000
     * >7km = unsupported (-1)
     */
    public double calculateTieredDeliveryFee(double distanceKm) {
        if (distanceKm < 0) return 15000;
        if (distanceKm <= 3) return 15000;
        if (distanceKm <= 5) return 25000;
        if (distanceKm <= 7) return 35000;
        return -1; // unsupported
    }

    public boolean isDeliveryAvailable(double distanceKm) {
        return calculateTieredDeliveryFee(distanceKm) >= 0;
    }
}
