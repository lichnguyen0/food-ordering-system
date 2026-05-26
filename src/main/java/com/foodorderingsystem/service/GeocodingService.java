package com.foodorderingsystem.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Service to interact with Nominatim API for geocoding.
 */
@Service
public class GeocodingService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Value("${app.geocoding.nominatim.url:https://nominatim.openstreetmap.org}")
    private String nominatimUrl;

    @Value("${app.geocoding.nominatim.email:}")
    private String contactEmail;

    public GeocodingService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Response class for geocoding results.
     */
    public static class Coordinates {
        private final double latitude;
        private final double longitude;
        private final String displayName;

        public Coordinates(double latitude, double longitude, String displayName) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.displayName = displayName;
        }

        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
        public String getDisplayName() { return displayName; }
    }

    /**
     * Geocode an address to coordinates asynchronously.
     * 
     * @param address The address to geocode
     * @return CompletableFuture containing Coordinates or null if not found
     */
    public CompletableFuture<Coordinates> geocodeAddress(String address) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
                String url = nominatimUrl + "/search?format=json&q=" + encodedAddress + "&limit=1&addressdetails=1";
                
                HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                        .GET()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(10))
                        .header("User-Agent", "LunoFood/1.0");

                if (contactEmail != null && !contactEmail.isEmpty()) {
                    requestBuilder.header("From", contactEmail);
                }

                HttpRequest request = requestBuilder.build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JsonNode results = objectMapper.readTree(response.body());
                    if (results.isArray() && results.size() > 0) {
                        JsonNode first = results.get(0);
                        double lat = first.get("lat").asDouble();
                        double lon = first.get("lon").asDouble();
                        String displayName = first.get("display_name").asText();
                        return new Coordinates(lat, lon, displayName);
                    }
                }
            } catch (IOException | InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return null;
        });
    }

    /**
     * Geocode synchronously (for convenience).
     * 
     * @param address The address to geocode
     * @return Coordinates or null if not found
     */
    public Coordinates geocodeAddressSync(String address) {
        try {
            return geocodeAddress(address).get();
        } catch (Exception e) {
            return null;
        }
    }
}