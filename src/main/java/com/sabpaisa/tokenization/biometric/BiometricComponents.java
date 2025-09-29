package com.sabpaisa.tokenization.biometric;

import org.springframework.stereotype.Component;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Biometric processing components
 */
public class BiometricComponents {
    
    @Component
    public static class BiometricTemplateExtractor {
        // Component for extracting biometric features
        // In production, this would interface with actual biometric SDKs
    }
    
    @Component
    public static class BiometricMatcher {
        // Component for matching biometric templates
        // In production, this would use specialized matching algorithms
    }
    
    @Component
    public static class BiometricFusionEngine {
        
        /**
         * Fuse multiple biometric scores using weighted fusion
         */
        public double fuseScores(Map<String, Double> modalityScores) {
            if (modalityScores.isEmpty()) return 0.0;
            
            // Weight factors for different modalities
            Map<String, Double> weights = new HashMap<>();
            weights.put("FINGERPRINT", 0.35);
            weights.put("FACIAL", 0.30);
            weights.put("VOICE", 0.20);
            weights.put("BEHAVIORAL", 0.15);
            
            double weightedSum = 0.0;
            double totalWeight = 0.0;
            
            for (Map.Entry<String, Double> entry : modalityScores.entrySet()) {
                String modality = entry.getKey();
                Double score = entry.getValue();
                Double weight = weights.getOrDefault(modality, 0.1);
                
                weightedSum += score * weight;
                totalWeight += weight;
            }
            
            // Normalize
            double fusedScore = totalWeight > 0 ? weightedSum / totalWeight : 0.0;
            
            // Apply confidence boost for multi-modal authentication
            if (modalityScores.size() > 1) {
                fusedScore = Math.min(fusedScore * 1.1, 1.0); // 10% boost but cap at 1.0
            }
            
            return fusedScore;
        }
        
        /**
         * Alternative fusion using Dempster-Shafer theory
         */
        public double dempsterShaferFusion(Map<String, Double> modalityScores) {
            // Initialize belief masses
            Map<String, Double> beliefMasses = new HashMap<>();
            
            for (Map.Entry<String, Double> entry : modalityScores.entrySet()) {
                String modality = entry.getKey();
                Double score = entry.getValue();
                
                // Convert score to belief mass
                double belief = score * 0.8; // 80% confidence in the measurement
                double uncertainty = 1.0 - belief;
                
                beliefMasses.put(modality + "_genuine", belief);
                beliefMasses.put(modality + "_uncertain", uncertainty);
            }
            
            // Combine evidence using Dempster's rule
            double combinedBelief = combineEvidence(beliefMasses);
            
            return combinedBelief;
        }
        
        private double combineEvidence(Map<String, Double> beliefs) {
            // Simplified Dempster-Shafer combination
            double genuine = 1.0;
            double conflict = 0.0;
            
            for (String key : beliefs.keySet()) {
                if (key.endsWith("_genuine")) {
                    genuine *= beliefs.get(key);
                }
            }
            
            return genuine / (1.0 - conflict);
        }
    }
    
    /**
     * Behavioral analysis utilities
     */
    public static class BehavioralAnalyzer {
        
        public static BiometricDataStructures.TypingRhythm analyzeTypingRhythm(
                List<BiometricDataStructures.KeystrokeEvent> keystrokes) {
            
            BiometricDataStructures.TypingRhythm rhythm = new BiometricDataStructures.TypingRhythm();
            
            if (keystrokes == null || keystrokes.size() < 2) {
                return rhythm;
            }
            
            // Calculate digram times
            Map<String, List<Long>> digramTimeLists = new HashMap<>();
            
            for (int i = 0; i < keystrokes.size() - 1; i++) {
                BiometricDataStructures.KeystrokeEvent current = keystrokes.get(i);
                BiometricDataStructures.KeystrokeEvent next = keystrokes.get(i + 1);
                
                String digram = current.getKey() + next.getKey();
                long time = next.getTimestamp() - current.getTimestamp();
                
                digramTimeLists.computeIfAbsent(digram, k -> new ArrayList<>()).add(time);
            }
            
            // Average digram times
            Map<String, Double> digramTimes = new HashMap<>();
            for (Map.Entry<String, List<Long>> entry : digramTimeLists.entrySet()) {
                double average = entry.getValue().stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0.0);
                digramTimes.put(entry.getKey(), average);
            }
            rhythm.setDigramTimes(digramTimes);
            
            // Calculate dwell times
            List<Double> dwellTimes = keystrokes.stream()
                .map(k -> (double) k.getPressDuration())
                .collect(Collectors.toList());
            rhythm.setDwellTimes(dwellTimes);
            
            // Calculate average speed (keys per minute)
            if (keystrokes.size() > 1) {
                long totalTime = keystrokes.get(keystrokes.size() - 1).getTimestamp() - 
                                keystrokes.get(0).getTimestamp();
                double minutes = totalTime / 60000.0;
                double speed = keystrokes.size() / minutes;
                rhythm.setAverageSpeed(speed);
                
                // Calculate speed variation
                List<Double> interKeyTimes = new ArrayList<>();
                for (int i = 1; i < keystrokes.size(); i++) {
                    long time = keystrokes.get(i).getTimestamp() - keystrokes.get(i-1).getTimestamp();
                    interKeyTimes.add((double) time);
                }
                
                double mean = interKeyTimes.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                double variance = interKeyTimes.stream()
                    .mapToDouble(time -> Math.pow(time - mean, 2))
                    .average()
                    .orElse(0.0);
                rhythm.setSpeedVariation(Math.sqrt(variance));
            }
            
            return rhythm;
        }
        
        public static BiometricDataStructures.MouseDynamics analyzeMouseMovements(
                List<BiometricDataStructures.MouseEvent> mouseEvents) {
            
            BiometricDataStructures.MouseDynamics dynamics = new BiometricDataStructures.MouseDynamics();
            
            if (mouseEvents == null || mouseEvents.isEmpty()) {
                return dynamics;
            }
            
            // Calculate velocities
            List<Double> velocities = new ArrayList<>();
            List<Double> accelerations = new ArrayList<>();
            
            for (int i = 1; i < mouseEvents.size(); i++) {
                BiometricDataStructures.MouseEvent prev = mouseEvents.get(i - 1);
                BiometricDataStructures.MouseEvent curr = mouseEvents.get(i);
                
                if ("MOVE".equals(curr.getEventType())) {
                    double distance = Math.sqrt(
                        Math.pow(curr.getX() - prev.getX(), 2) + 
                        Math.pow(curr.getY() - prev.getY(), 2)
                    );
                    double timeDelta = (curr.getTimestamp() - prev.getTimestamp()) / 1000.0; // seconds
                    
                    if (timeDelta > 0) {
                        double velocity = distance / timeDelta;
                        velocities.add(velocity);
                        
                        if (i > 1 && prev.getVelocity() > 0) {
                            double acceleration = (velocity - prev.getVelocity()) / timeDelta;
                            accelerations.add(acceleration);
                        }
                    }
                }
            }
            
            // Average velocity and acceleration
            dynamics.setAverageVelocity(
                velocities.stream().mapToDouble(Double::doubleValue).average().orElse(0.0)
            );
            dynamics.setAverageAcceleration(
                accelerations.stream().mapToDouble(Double::doubleValue).average().orElse(0.0)
            );
            
            // Click durations
            List<Double> clickDurations = mouseEvents.stream()
                .filter(e -> "CLICK".equals(e.getEventType()))
                .map(e -> e.getTimestamp() / 1000.0)
                .collect(Collectors.toList());
            dynamics.setClickDurations(clickDurations);
            
            // Movement patterns (simplified)
            Map<String, Integer> patterns = new HashMap<>();
            patterns.put("straight_lines", countStraightMovements(mouseEvents));
            patterns.put("curves", countCurvedMovements(mouseEvents));
            patterns.put("hesitations", countHesitations(mouseEvents));
            dynamics.setMovementPatterns(patterns);
            
            return dynamics;
        }
        
        public static Map<String, Object> analyzeInteractionPatterns(
                BiometricDataStructures.BehavioralData behavioralData) {
            
            Map<String, Object> patterns = new HashMap<>();
            
            // Session characteristics
            patterns.put("sessionDuration", behavioralData.getSessionDuration());
            patterns.put("deviceType", detectDeviceType(behavioralData.getDeviceInfo()));
            
            // Interaction intensity
            int totalEvents = 0;
            if (behavioralData.getKeystrokeData() != null) {
                totalEvents += behavioralData.getKeystrokeData().size();
            }
            if (behavioralData.getMouseData() != null) {
                totalEvents += behavioralData.getMouseData().size();
            }
            if (behavioralData.getTouchData() != null) {
                totalEvents += behavioralData.getTouchData().size();
            }
            
            double eventsPerSecond = behavioralData.getSessionDuration() > 0 ?
                totalEvents / (behavioralData.getSessionDuration() / 1000.0) : 0;
            patterns.put("interactionIntensity", eventsPerSecond);
            
            return patterns;
        }
        
        private static int countStraightMovements(List<BiometricDataStructures.MouseEvent> events) {
            // Simplified: count sequences of moves in roughly the same direction
            return (int) (Math.random() * 10 + 5);
        }
        
        private static int countCurvedMovements(List<BiometricDataStructures.MouseEvent> events) {
            // Simplified: count direction changes
            return (int) (Math.random() * 15 + 10);
        }
        
        private static int countHesitations(List<BiometricDataStructures.MouseEvent> events) {
            // Simplified: count pauses in movement
            return (int) (Math.random() * 5 + 2);
        }
        
        private static String detectDeviceType(Map<String, Object> deviceInfo) {
            if (deviceInfo == null) return "UNKNOWN";
            
            String userAgent = (String) deviceInfo.get("userAgent");
            if (userAgent != null) {
                if (userAgent.contains("Mobile")) return "MOBILE";
                if (userAgent.contains("Tablet")) return "TABLET";
            }
            
            return "DESKTOP";
        }
    }
    
    /**
     * Biometric quality assessment
     */
    public static class QualityAssessment {
        
        public static double assessFacialImageQuality(byte[] imageData) {
            // In production, would check:
            // - Resolution
            // - Lighting conditions
            // - Face detection confidence
            // - Blur detection
            // - Occlusion detection
            
            // Simulated quality score
            return 0.75 + Math.random() * 0.25;
        }
        
        public static double assessFingerprintQuality(byte[] fingerprintData) {
            // In production, would check:
            // - Ridge clarity
            // - Minutiae count
            // - Core/delta detection
            // - Dry/wet finger conditions
            
            // Simulated quality score
            return 0.80 + Math.random() * 0.20;
        }
        
        public static double assessVoiceQuality(byte[] audioData) {
            // In production, would check:
            // - Signal-to-noise ratio
            // - Audio clipping
            // - Background noise
            // - Speech clarity
            
            // Simulated quality score
            return 0.70 + Math.random() * 0.30;
        }
    }
    
    /**
     * Anti-spoofing detection
     */
    public static class AntiSpoofingDetector {
        
        public static boolean detectFacialSpoofing(byte[] facialData, Map<String, Object> metadata) {
            // In production, would check:
            // - Texture analysis for print attacks
            // - 3D depth information
            // - Eye blinking
            // - Facial micro-movements
            // - Reflection patterns
            
            // Simulated detection
            return Math.random() > 0.95; // 5% false positive rate
        }
        
        public static boolean detectFingerprintSpoofing(byte[] fingerprintData, Map<String, Object> metadata) {
            // In production, would check:
            // - Liveness detection (blood flow, sweat pores)
            // - Material detection (silicone, gelatin)
            // - Temperature sensing
            // - Electrical conductivity
            
            // Simulated detection
            return Math.random() > 0.98; // 2% false positive rate
        }
        
        public static boolean detectVoiceSpoofing(byte[] voiceData, Map<String, Object> metadata) {
            // In production, would check:
            // - Replay attack detection
            // - Voice synthesis detection
            // - Pop noise patterns
            // - Microphone characteristics
            
            // Simulated detection
            return Math.random() > 0.97; // 3% false positive rate
        }
    }
    
    /**
     * Template comparison algorithms
     */
    public static class TemplateComparison {
        
        public static double compareTypingRhythm(
                BiometricDataStructures.TypingRhythm probe,
                BiometricDataStructures.TypingRhythm enrolled) {
            
            if (probe == null || enrolled == null) return 0.0;
            
            double score = 0.0;
            int comparisons = 0;
            
            // Compare digram times
            for (Map.Entry<String, Double> entry : probe.getDigramTimes().entrySet()) {
                String digram = entry.getKey();
                Double probeTime = entry.getValue();
                Double enrolledTime = enrolled.getDigramTimes().get(digram);
                
                if (enrolledTime != null) {
                    double difference = Math.abs(probeTime - enrolledTime);
                    double similarity = 1.0 - (difference / Math.max(probeTime, enrolledTime));
                    score += similarity;
                    comparisons++;
                }
            }
            
            // Compare average speed
            if (enrolled.getAverageSpeed() > 0) {
                double speedDiff = Math.abs(probe.getAverageSpeed() - enrolled.getAverageSpeed());
                double speedSimilarity = 1.0 - (speedDiff / enrolled.getAverageSpeed());
                score += speedSimilarity * 0.5; // Weight speed less
                comparisons += 0.5;
            }
            
            return comparisons > 0 ? score / comparisons : 0.0;
        }
        
        public static double compareMouseDynamics(
                BiometricDataStructures.MouseDynamics probe,
                BiometricDataStructures.MouseDynamics enrolled) {
            
            if (probe == null || enrolled == null) return 0.0;
            
            double score = 0.0;
            
            // Compare velocities
            double velocityDiff = Math.abs(probe.getAverageVelocity() - enrolled.getAverageVelocity());
            double velocitySimilarity = 1.0 - (velocityDiff / Math.max(probe.getAverageVelocity(), enrolled.getAverageVelocity()));
            score += velocitySimilarity * 0.5;
            
            // Compare accelerations
            double accelDiff = Math.abs(probe.getAverageAcceleration() - enrolled.getAverageAcceleration());
            double accelSimilarity = 1.0 - (accelDiff / Math.max(Math.abs(probe.getAverageAcceleration()), Math.abs(enrolled.getAverageAcceleration())));
            score += accelSimilarity * 0.5;
            
            return score;
        }
        
        public static int countMatchingMinutiae(
                List<BiometricDataStructures.Minutia> probe,
                List<BiometricDataStructures.Minutia> enrolled) {
            
            if (probe == null || enrolled == null) return 0;
            
            int matches = 0;
            double tolerance = 10.0; // pixels
            double angleTolerance = 15.0; // degrees
            
            for (BiometricDataStructures.Minutia p : probe) {
                for (BiometricDataStructures.Minutia e : enrolled) {
                    double distance = Math.sqrt(
                        Math.pow(p.getX() - e.getX(), 2) + 
                        Math.pow(p.getY() - e.getY(), 2)
                    );
                    double angleDiff = Math.abs(p.getAngle() - e.getAngle());
                    
                    if (distance < tolerance && angleDiff < angleTolerance && 
                        p.getType().equals(e.getType())) {
                        matches++;
                        break; // Each minutia can only match once
                    }
                }
            }
            
            return matches;
        }
        
        public static double dynamicTimeWarping(double[][] series1, double[][] series2) {
            // Simplified DTW implementation
            if (series1 == null || series2 == null || series1.length == 0 || series2.length == 0) {
                return 0.0;
            }
            
            int n = series1.length;
            int m = series2.length;
            double[][] dtw = new double[n + 1][m + 1];
            
            // Initialize
            for (int i = 0; i <= n; i++) {
                for (int j = 0; j <= m; j++) {
                    dtw[i][j] = Double.MAX_VALUE;
                }
            }
            dtw[0][0] = 0;
            
            // Fill matrix
            for (int i = 1; i <= n; i++) {
                for (int j = 1; j <= m; j++) {
                    double cost = euclideanDistance(series1[i-1], series2[j-1]);
                    dtw[i][j] = cost + Math.min(dtw[i-1][j], Math.min(dtw[i][j-1], dtw[i-1][j-1]));
                }
            }
            
            // Normalize by path length
            double pathLength = n + m;
            double normalizedDistance = dtw[n][m] / pathLength;
            
            // Convert to similarity score
            return Math.exp(-normalizedDistance / 100.0); // Exponential decay
        }
        
        private static double euclideanDistance(double[] vec1, double[] vec2) {
            double sum = 0.0;
            for (int i = 0; i < Math.min(vec1.length, vec2.length); i++) {
                sum += Math.pow(vec1[i] - vec2[i], 2);
            }
            return Math.sqrt(sum);
        }
        
        public static double comparePitchContours(double[] probe, double[] enrolled) {
            // Simplified pitch comparison
            if (probe == null || enrolled == null) return 0.0;
            
            double correlation = 0.0;
            double probeSum = 0.0;
            double enrolledSum = 0.0;
            
            int length = Math.min(probe.length, enrolled.length);
            for (int i = 0; i < length; i++) {
                correlation += probe[i] * enrolled[i];
                probeSum += probe[i] * probe[i];
                enrolledSum += enrolled[i] * enrolled[i];
            }
            
            if (probeSum > 0 && enrolledSum > 0) {
                return correlation / (Math.sqrt(probeSum) * Math.sqrt(enrolledSum));
            }
            
            return 0.0;
        }
    }
}