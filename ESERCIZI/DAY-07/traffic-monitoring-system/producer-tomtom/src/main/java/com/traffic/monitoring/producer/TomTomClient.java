package com.traffic.monitoring.producer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Client for TomTom Traffic API
 * Handles HTTP requests with retry logic and rate limiting
 */
public class TomTomClient {
    private static final Logger logger = LoggerFactory.getLogger(TomTomClient.class);
    private static final String TRAFFIC_FLOW_API_URL = "https://api.tomtom.com/traffic/services/4/flowSegmentData/absolute/{zoom}/json";
    private static final int DEFAULT_ZOOM = 10;
    private static final int MAX_RETRIES = 3;
    
    private final String apiKey;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public TomTomClient(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Fetch traffic flow data for a geographic bounding box
     */
    public List<TrafficData> fetchTrafficFlow(double minLat, double minLon, double maxLat, double maxLon) {
        List<TrafficData> results = new ArrayList<>();
        
        // Sample multiple points in the bounding box
        double latStep = (maxLat - minLat) / 3;
        double lonStep = (maxLon - minLon) / 3;
        
        for (double lat = minLat; lat <= maxLat; lat += latStep) {
            for (double lon = minLon; lon <= maxLon; lon += lonStep) {
                try {
                    TrafficData data = fetchTrafficFlowPoint(lat, lon);
                    if (data != null) {
                        results.add(data);
                    }
                    // Rate limiting - avoid hitting API limits
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.error("Interrupted during rate limiting", e);
                    break;
                } catch (Exception e) {
                    logger.error("Error fetching traffic data for point ({}, {})", lat, lon, e);
                }
            }
        }
        
        return results;
    }
    
    /**
     * Fetch traffic flow data for a specific point with retry logic
     */
    private TrafficData fetchTrafficFlowPoint(double latitude, double longitude) {
        int retries = 0;
        Exception lastException = null;
        
        while (retries < MAX_RETRIES) {
            try {
                String url = buildTrafficFlowUrl(latitude, longitude);
                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .build();
                
                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseBody = response.body().string();
                        return parseTrafficData(responseBody, latitude, longitude);
                    } else {
                        logger.warn("TomTom API returned status {}: {}", 
                                   response.code(), response.message());
                        
                        // Check if it's a rate limit error (429)
                        if (response.code() == 429) {
                            long backoffMs = (long) Math.pow(2, retries) * 1000;
                            logger.info("Rate limited. Backing off for {} ms", backoffMs);
                            Thread.sleep(backoffMs);
                        }
                    }
                }
            } catch (IOException e) {
                lastException = e;
                logger.warn("HTTP error on attempt {}/{}", retries + 1, MAX_RETRIES, e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Interrupted during backoff", e);
                return null;
            }
            
            retries++;
            
            // Exponential backoff
            if (retries < MAX_RETRIES) {
                try {
                    long backoffMs = (long) Math.pow(2, retries) * 1000;
                    Thread.sleep(backoffMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }
        
        logger.error("Failed to fetch traffic data after {} retries", MAX_RETRIES, lastException);
        return null;
    }
    
    private String buildTrafficFlowUrl(double latitude, double longitude) {
        String baseUrl = TRAFFIC_FLOW_API_URL.replace("{zoom}", String.valueOf(DEFAULT_ZOOM));
        return String.format("%s?key=%s&point=%f,%f", baseUrl, apiKey, latitude, longitude);
    }
    
    private TrafficData parseTrafficData(String jsonResponse, double latitude, double longitude) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode flowData = root.path("flowSegmentData");
            
            if (flowData.isMissingNode()) {
                logger.warn("No flowSegmentData found in response");
                return null;
            }
            
            return TrafficData.builder()
                    .latitude(latitude)
                    .longitude(longitude)
                    .currentSpeed(flowData.path("currentSpeed").asDouble(0.0))
                    .freeFlowSpeed(flowData.path("freeFlowSpeed").asDouble(0.0))
                    .currentTravelTime(flowData.path("currentTravelTime").asInt(0))
                    .freeFlowTravelTime(flowData.path("freeFlowTravelTime").asInt(0))
                    .confidence(flowData.path("confidence").asDouble(0.0))
                    .roadClosure(flowData.path("roadClosure").asBoolean(false))
                    .timestamp(System.currentTimeMillis())
                    .build();
            
        } catch (Exception e) {
            logger.error("Error parsing TomTom API response", e);
            return null;
        }
    }
}
