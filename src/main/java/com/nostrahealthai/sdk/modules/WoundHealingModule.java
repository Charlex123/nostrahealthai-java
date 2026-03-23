package com.nostrahealthai.sdk.modules;

import com.nostrahealthai.sdk.NostraHealthAI;
import com.nostrahealthai.sdk.exceptions.NostraHealthAIException;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Wound healing analysis and tracking module.
 *
 * <p>Supports wound image analysis, profile management, healing timeline tracking,
 * and infection risk assessment.</p>
 */
public class WoundHealingModule {
    private final NostraHealthAI client;

    public WoundHealingModule(NostraHealthAI client) {
        this.client = client;
    }

    // =========================================================================
    // WOUND ANALYSIS
    // =========================================================================

    /**
     * Analyze a wound image.
     *
     * @param file           Wound image file
     * @param woundProfileId Optional wound profile ID for tracking
     * @param woundType      Optional wound type
     * @param bodyLocation   Optional body location
     * @param painLevel      Optional pain level (0-10)
     * @param symptoms       Optional list of symptoms
     * @return Job ID for polling results
     */
    public String analyze(File file, String woundProfileId, String woundType,
                          String bodyLocation, Integer painLevel, List<String> symptoms)
            throws NostraHealthAIException {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(),
                        RequestBody.create(file, NostraHealthAI.getMimeType(file)));

        if (woundProfileId != null) builder.addFormDataPart("woundProfileId", woundProfileId);
        if (woundType != null) builder.addFormDataPart("woundType", woundType);
        if (bodyLocation != null) builder.addFormDataPart("bodyLocation", bodyLocation);
        if (painLevel != null) builder.addFormDataPart("painLevel", painLevel.toString());
        if (symptoms != null) builder.addFormDataPart("symptoms", String.join(",", symptoms));

        Map<String, Object> result = client.postMultipart("/api/v1/ai/wound-healing/analyze", builder.build());
        return (String) result.get("jobId");
    }

    /**
     * Analyze a wound image with just the file.
     */
    public String analyze(File file) throws NostraHealthAIException {
        return analyze(file, null, null, null, null, null);
    }

    /**
     * Get the status of a wound analysis job.
     */
    public Map<String, Object> getJobStatus(String jobId) throws NostraHealthAIException {
        return client.get("/api/v1/ai/wound-healing/job/" + jobId);
    }

    /**
     * Wait for a wound analysis job to complete.
     */
    public Map<String, Object> waitForCompletion(String jobId, long pollInterval, int maxAttempts)
            throws NostraHealthAIException, InterruptedException {
        int attempts = 0;
        while (attempts < maxAttempts) {
            Map<String, Object> status = getJobStatus(jobId);
            String statusStr = (String) status.get("status");

            if ("completed".equals(statusStr)) return status;
            if ("failed".equals(statusStr)) {
                throw new NostraHealthAIException("Wound analysis failed: " + status.getOrDefault("error", "Unknown error"));
            }

            Thread.sleep(pollInterval);
            attempts++;
        }
        throw new NostraHealthAIException("Wound analysis timeout");
    }

    /**
     * Wait for a wound analysis job to complete with default settings.
     */
    public Map<String, Object> waitForCompletion(String jobId) throws NostraHealthAIException, InterruptedException {
        return waitForCompletion(jobId, 2000, 60);
    }

    /**
     * Get all wound analyses for the current user.
     */
    public List<Map<String, Object>> getAllAnalyses(Integer limit) throws NostraHealthAIException {
        String endpoint = "/api/v1/ai/wound-healing/analyses";
        if (limit != null) endpoint += "?limit=" + limit;

        Map<String, Object> result = client.get(endpoint);
        return NostraHealthAI.getDataList(result);
    }

    /**
     * Get all wound analyses.
     */
    public List<Map<String, Object>> getAllAnalyses() throws NostraHealthAIException {
        return getAllAnalyses(null);
    }

    /**
     * Get a specific wound analysis by ID.
     */
    public Map<String, Object> getAnalysis(String analysisId) throws NostraHealthAIException {
        Map<String, Object> result = client.get("/api/v1/ai/wound-healing/analyses/" + analysisId);
        return NostraHealthAI.getDataMap(result);
    }

    /**
     * Delete a wound analysis.
     */
    public void deleteAnalysis(String analysisId) throws NostraHealthAIException {
        client.deleteRequest("/api/v1/ai/wound-healing/analyses/" + analysisId);
    }

    // =========================================================================
    // WOUND PROFILES
    // =========================================================================

    /**
     * Create a new wound profile for tracking.
     *
     * @param name         Profile name
     * @param woundType    Type of wound
     * @param bodyLocation Body location
     * @return Created profile data
     */
    public Map<String, Object> createProfile(String name, String woundType, String bodyLocation)
            throws NostraHealthAIException {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("woundType", woundType);
        body.put("bodyLocation", bodyLocation);

        Map<String, Object> result = client.postJson("/api/v1/ai/wound-healing/profiles", body);
        return NostraHealthAI.getDataMap(result);
    }

    /**
     * Create a new wound profile with additional details.
     */
    public Map<String, Object> createProfile(String name, String woundType, String bodyLocation,
                                              String bodyLocationDetails, String etiology, String notes)
            throws NostraHealthAIException {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("woundType", woundType);
        body.put("bodyLocation", bodyLocation);
        if (bodyLocationDetails != null) body.put("bodyLocationDetails", bodyLocationDetails);
        if (etiology != null) body.put("etiology", etiology);
        if (notes != null) body.put("notes", notes);

        Map<String, Object> result = client.postJson("/api/v1/ai/wound-healing/profiles", body);
        return NostraHealthAI.getDataMap(result);
    }

    /**
     * Get all wound profiles for the current user.
     */
    public Map<String, Object> getProfiles() throws NostraHealthAIException {
        Map<String, Object> result = client.get("/api/v1/ai/wound-healing/profiles");
        return NostraHealthAI.getDataMap(result);
    }

    /**
     * Get a specific wound profile with recent analyses.
     */
    public Map<String, Object> getProfile(String profileId) throws NostraHealthAIException {
        Map<String, Object> result = client.get("/api/v1/ai/wound-healing/profiles/" + profileId);
        return NostraHealthAI.getDataMap(result);
    }

    /**
     * Update a wound profile.
     */
    public Map<String, Object> updateProfile(String profileId, Map<String, Object> updates)
            throws NostraHealthAIException {
        Map<String, Object> result = client.putJson("/api/v1/ai/wound-healing/profiles/" + profileId, updates);
        return NostraHealthAI.getDataMap(result);
    }

    /**
     * Archive a wound profile (mark as healed).
     */
    public void archiveProfile(String profileId) throws NostraHealthAIException {
        client.postJson("/api/v1/ai/wound-healing/profiles/" + profileId + "/archive", new HashMap<>());
    }

    /**
     * Delete a wound profile and all associated analyses.
     */
    public void deleteProfile(String profileId) throws NostraHealthAIException {
        client.deleteRequest("/api/v1/ai/wound-healing/profiles/" + profileId);
    }

    /**
     * Get wound healing timeline for a profile.
     */
    public Map<String, Object> getTimeline(String profileId) throws NostraHealthAIException {
        Map<String, Object> result = client.get("/api/v1/ai/wound-healing/profiles/" + profileId + "/timeline");
        return NostraHealthAI.getDataMap(result);
    }

    /**
     * Get reference data (wound types and body locations).
     */
    public Map<String, Object> getReferenceData() throws NostraHealthAIException {
        Map<String, Object> result = client.get("/api/v1/ai/wound-healing/reference-data");
        return NostraHealthAI.getDataMap(result);
    }

    // =========================================================================
    // ASYNC METHODS
    // =========================================================================

    /**
     * Analyze a wound image asynchronously with full parameters.
     */
    public CompletableFuture<String> analyzeAsync(File file, String woundProfileId, String woundType,
                                                   String bodyLocation, Integer painLevel, List<String> symptoms) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return analyze(file, woundProfileId, woundType, bodyLocation, painLevel, symptoms);
            } catch (NostraHealthAIException e) {
                throw new CompletionException(e);
            }
        }, client.getExecutorService());
    }

    /**
     * Analyze a wound image asynchronously with just the file.
     */
    public CompletableFuture<String> analyzeAsync(File file) {
        return analyzeAsync(file, null, null, null, null, null);
    }

    /**
     * Get the status of a wound analysis job asynchronously.
     */
    public CompletableFuture<Map<String, Object>> getJobStatusAsync(String jobId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getJobStatus(jobId);
            } catch (NostraHealthAIException e) {
                throw new CompletionException(e);
            }
        }, client.getExecutorService());
    }

    /**
     * Get all wound analyses asynchronously.
     */
    public CompletableFuture<List<Map<String, Object>>> getAllAnalysesAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getAllAnalyses();
            } catch (NostraHealthAIException e) {
                throw new CompletionException(e);
            }
        }, client.getExecutorService());
    }

    /**
     * Get a specific wound analysis by ID asynchronously.
     */
    public CompletableFuture<Map<String, Object>> getAnalysisAsync(String analysisId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getAnalysis(analysisId);
            } catch (NostraHealthAIException e) {
                throw new CompletionException(e);
            }
        }, client.getExecutorService());
    }

    /**
     * Delete a wound analysis asynchronously.
     */
    public CompletableFuture<Void> deleteAnalysisAsync(String analysisId) {
        return CompletableFuture.runAsync(() -> {
            try {
                deleteAnalysis(analysisId);
            } catch (NostraHealthAIException e) {
                throw new CompletionException(e);
            }
        }, client.getExecutorService());
    }

    /**
     * Create a wound profile asynchronously.
     */
    public CompletableFuture<Map<String, Object>> createProfileAsync(String name, String woundType, String bodyLocation) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return createProfile(name, woundType, bodyLocation);
            } catch (NostraHealthAIException e) {
                throw new CompletionException(e);
            }
        }, client.getExecutorService());
    }

    /**
     * Get all wound profiles asynchronously.
     */
    public CompletableFuture<Map<String, Object>> getProfilesAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getProfiles();
            } catch (NostraHealthAIException e) {
                throw new CompletionException(e);
            }
        }, client.getExecutorService());
    }

    /**
     * Get a specific wound profile asynchronously.
     */
    public CompletableFuture<Map<String, Object>> getProfileAsync(String profileId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getProfile(profileId);
            } catch (NostraHealthAIException e) {
                throw new CompletionException(e);
            }
        }, client.getExecutorService());
    }

    /**
     * Get wound healing timeline asynchronously.
     */
    public CompletableFuture<Map<String, Object>> getTimelineAsync(String profileId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getTimeline(profileId);
            } catch (NostraHealthAIException e) {
                throw new CompletionException(e);
            }
        }, client.getExecutorService());
    }
}
