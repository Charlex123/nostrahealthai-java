package com.nostrahealthai.sdk.modules;

import com.nostrahealthai.sdk.NostraHealthAI;
import com.nostrahealthai.sdk.exceptions.NostraHealthAIException;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Skin infection analysis module.
 *
 * <p>Supports detection of fungal, bacterial, viral, parasitic, and inflammatory skin conditions.</p>
 */
public class SkinInfectionModule {
    private final NostraHealthAI client;

    public SkinInfectionModule(NostraHealthAI client) {
        this.client = client;
    }

    /**
     * Analyze a skin image for infections and conditions.
     *
     * @param file         Skin image file
     * @param affectedArea Optional body area affected
     * @param duration     Optional symptom duration
     * @param symptoms     Optional list of symptoms
     * @return Job ID for polling results
     */
    public String analyze(File file, String affectedArea, String duration, List<String> symptoms)
            throws NostraHealthAIException {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(),
                        RequestBody.create(file, NostraHealthAI.getMimeType(file)));

        if (affectedArea != null) builder.addFormDataPart("affectedArea", affectedArea);
        if (duration != null) builder.addFormDataPart("duration", duration);
        if (symptoms != null) builder.addFormDataPart("symptoms", String.join(",", symptoms));

        Map<String, Object> result = client.postMultipart("/api/v1/ai/skin-infections", builder.build());
        return (String) result.get("jobId");
    }

    /**
     * Analyze a skin image with just the file.
     */
    public String analyze(File file) throws NostraHealthAIException {
        return analyze(file, null, null, null);
    }

    /**
     * Get the status of a skin analysis job.
     */
    public Map<String, Object> getJobStatus(String jobId) throws NostraHealthAIException {
        return client.get("/api/v1/ai/skin-infections/job/" + jobId);
    }

    /**
     * Wait for a skin analysis job to complete.
     */
    public Map<String, Object> waitForCompletion(String jobId, long pollInterval, int maxAttempts)
            throws NostraHealthAIException, InterruptedException {
        int attempts = 0;
        while (attempts < maxAttempts) {
            Map<String, Object> status = getJobStatus(jobId);
            String statusStr = (String) status.get("status");

            if ("completed".equals(statusStr)) return status;
            if ("failed".equals(statusStr)) {
                throw new NostraHealthAIException("Skin analysis failed: " + status.getOrDefault("error", "Unknown error"));
            }

            Thread.sleep(pollInterval);
            attempts++;
        }
        throw new NostraHealthAIException("Skin analysis timeout");
    }

    /**
     * Wait for a skin analysis job to complete with default settings.
     */
    public Map<String, Object> waitForCompletion(String jobId) throws NostraHealthAIException, InterruptedException {
        return waitForCompletion(jobId, 2000, 60);
    }

    /**
     * Get all skin analyses for the current user.
     */
    public List<Map<String, Object>> getAllAnalyses() throws NostraHealthAIException {
        Map<String, Object> result = client.get("/api/v1/ai/skin-infections");
        return NostraHealthAI.getDataList(result);
    }

    /**
     * Get a specific skin analysis by ID.
     */
    public Map<String, Object> getAnalysis(String analysisId) throws NostraHealthAIException {
        Map<String, Object> result = client.get("/api/v1/ai/skin-infections/" + analysisId);
        return NostraHealthAI.getDataMap(result);
    }

    /**
     * Delete a skin analysis.
     */
    public void deleteAnalysis(String analysisId) throws NostraHealthAIException {
        client.deleteRequest("/api/v1/ai/skin-infections/" + analysisId);
    }

    /**
     * Get list of supported skin conditions.
     */
    public Map<String, Object> getSupportedConditions() throws NostraHealthAIException {
        Map<String, Object> result = client.get("/api/v1/ai/skin-infections/conditions");
        return NostraHealthAI.getDataMap(result);
    }

    // =========================================================================
    // ASYNC METHODS
    // =========================================================================

    /**
     * Analyze a skin image asynchronously with full parameters.
     */
    public CompletableFuture<String> analyzeAsync(File file, String affectedArea, String duration, List<String> symptoms) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return analyze(file, affectedArea, duration, symptoms);
            } catch (NostraHealthAIException e) {
                throw new CompletionException(e);
            }
        }, client.getExecutorService());
    }

    /**
     * Analyze a skin image asynchronously with just the file.
     */
    public CompletableFuture<String> analyzeAsync(File file) {
        return analyzeAsync(file, null, null, null);
    }

    /**
     * Get the status of a skin analysis job asynchronously.
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
     * Get all skin analyses asynchronously.
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
     * Get a specific skin analysis by ID asynchronously.
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
     * Delete a skin analysis asynchronously.
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
}
