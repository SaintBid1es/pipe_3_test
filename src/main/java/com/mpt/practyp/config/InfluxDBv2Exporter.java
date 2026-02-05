package com.mpt.practyp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

@Component
public class InfluxDBv2Exporter {

    private static final Logger logger = LoggerFactory.getLogger(InfluxDBv2Exporter.class);
    
    @Value("${server.port:8888}")
    private int serverPort;
    
    @Value("${management.metrics.export.influx.uri:http://localhost:8086}")
    private String influxUri;
    
    @Value("${management.metrics.export.influx.org:Rostelecom}")
    private String org;
    
    @Value("${management.metrics.export.influx.bucket:metrics}")
    private String bucket;
    
    @Value("${management.metrics.export.influx.token:}")
    private String token;

    @Scheduled(fixedRate = 15000) 
    public void exportMetrics() {
        if (token == null || token.isEmpty()) {
            logger.debug("InfluxDB token not configured, skipping export");
            return;
        }
        
        try {
        
            String prometheusMetrics = getPrometheusMetricsFromEndpoint();
            
            if (prometheusMetrics == null || prometheusMetrics.trim().isEmpty()) {
                logger.warn("No metrics retrieved from Prometheus endpoint");
                return;
            }
            
           
            String lineProtocol = convertToLineProtocol(prometheusMetrics);
            
            if (lineProtocol.trim().isEmpty()) {
                logger.warn("No metrics to export after conversion");
                return;
            }
            
           
            sendToInfluxDB(lineProtocol);
            
        } catch (Exception e) {
            logger.error("Failed to export metrics to InfluxDB v2", e);
        }
    }
    
    private String getPrometheusMetricsFromEndpoint() throws Exception {
        String urlString = "http://localhost:" + serverPort + "/actuator/prometheus";
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        try {
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(10000);
            
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                StringBuilder response = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line).append("\n");
                    }
                }
                return response.toString();
            } else {
                logger.error("Failed to get Prometheus metrics: HTTP " + responseCode);
                return null;
            }
        } finally {
            conn.disconnect();
        }
    }
    
    private String convertToLineProtocol(String prometheusMetrics) {
     
        StringBuilder lineProtocol = new StringBuilder();
        String[] lines = prometheusMetrics.split("\n");
        long timestamp = System.currentTimeMillis() * 1_000_000; 
        
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            
        
            
            try {
                String measurement;
                StringBuilder tagsBuilder = new StringBuilder();
                String value;
                
               
                if (line.contains("{")) {
                   
                    int braceStart = line.indexOf('{');
                    int braceEnd = line.indexOf('}');
                    if (braceEnd == -1) continue;
                    
                    measurement = line.substring(0, braceStart).trim();
                    String tagsPart = line.substring(braceStart + 1, braceEnd);
                    
                   
                    if (!tagsPart.isEmpty()) {
                      
                        java.util.List<String> tagPairs = new java.util.ArrayList<>();
                        StringBuilder currentTag = new StringBuilder();
                        boolean inQuotes = false;
                        
                        for (char c : tagsPart.toCharArray()) {
                            if (c == '"') {
                                inQuotes = !inQuotes;
                                currentTag.append(c);
                            } else if (c == ',' && !inQuotes) {
                                if (currentTag.length() > 0) {
                                    tagPairs.add(currentTag.toString().trim());
                                    currentTag.setLength(0);
                                }
                            } else {
                                currentTag.append(c);
                            }
                        }
                        if (currentTag.length() > 0) {
                            tagPairs.add(currentTag.toString().trim());
                        }
                      
                        for (String tagPair : tagPairs) {
                            if (tagPair.isEmpty()) continue;
                            int eqIndex = tagPair.indexOf('=');
                            if (eqIndex == -1) continue;
                            
                            String key = tagPair.substring(0, eqIndex).trim();
                            String val = tagPair.substring(eqIndex + 1).trim();
                            
                       
                            if (val.startsWith("\"") && val.endsWith("\"") && val.length() > 1) {
                                val = val.substring(1, val.length() - 1);
                            }
                            
                     
                            key = sanitizeTag(key);
                            val = sanitizeTag(val);
                            
                            tagsBuilder.append(",").append(key).append("=").append(val);
                        }
                    }
                    
                  
                    String afterBrace = line.substring(braceEnd + 1).trim();
                    String[] parts = afterBrace.split("\\s+");
                    value = parts.length > 0 ? parts[0] : "0";
                } else {
                  
                    String[] parts = line.split("\\s+");
                    if (parts.length < 2) continue;
                    measurement = parts[0];
                    value = parts[1];
                }
                
              
                measurement = measurement.replaceAll("[^a-zA-Z0-9_]", "_");
                
              
                value = normalizeValue(value);
              
                lineProtocol.append(measurement)
                          .append(tagsBuilder.toString())
                          .append(" value=").append(value)
                          .append(" ").append(timestamp).append("\n");
                
            } catch (Exception e) {
                logger.debug("Failed to parse line: " + line, e);
            }
        }
        
        return lineProtocol.toString();
    }
    
    private String sanitizeTag(String tag) {
    
        return tag.replaceAll("[^a-zA-Z0-9_\\-]", "_")
                  .replaceAll("_{2,}", "_")  
                  .replaceAll("^_|_$", ""); 
    }
    
    private String normalizeValue(String value) {
        try {
            double d = Double.parseDouble(value);
            
            java.math.BigDecimal bd = new java.math.BigDecimal(value);
            
            if (bd.scale() == 0 || bd.stripTrailingZeros().scale() <= 0) {
                return String.valueOf(bd.longValue());
            }
            
         
            String result = bd.stripTrailingZeros().toPlainString();
            
            if (Math.abs(d) >= 1e15 || (Math.abs(d) < 1e-6 && d != 0)) {
                return String.valueOf(d);
            }
            
            return result;
        } catch (NumberFormatException e) {
            return "\"" + value.replace("\"", "\\\"").replace("\\", "\\\\") + "\"";
        }
    }
    
    private void sendToInfluxDB(String lineProtocol) throws Exception {
        if (lineProtocol.trim().isEmpty()) {
            return;
        }
        
        String encodedOrg = URLEncoder.encode(org, StandardCharsets.UTF_8);
        String encodedBucket = URLEncoder.encode(bucket, StandardCharsets.UTF_8);
        String urlString = influxUri + "/api/v2/write?org=" + encodedOrg + "&bucket=" + encodedBucket;
        
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        try {
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Token " + token);
            conn.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(10000);
            
            try (OutputStream os = conn.getOutputStream()) {
                os.write(lineProtocol.getBytes(StandardCharsets.UTF_8));
            }
            
            int responseCode = conn.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                logger.debug("Successfully exported metrics to InfluxDB v2");
            } else {
                String errorMessage = "Error response from InfluxDB: " + responseCode;
                try {
                    if (conn.getErrorStream() != null) {
                        Scanner scanner = new Scanner(conn.getErrorStream());
                        if (scanner.hasNext()) {
                            errorMessage += " - " + scanner.useDelimiter("\\A").next();
                        }
                        scanner.close();
                    }
                } catch (Exception e) {
                
                }
                logger.error(errorMessage);
            }
        } finally {
            conn.disconnect();
        }
    }
}

