package com.nostrahealthai.sdk.modules;

import com.nostrahealthai.sdk.NostraHealthAI;
import com.nostrahealthai.sdk.exceptions.NostraHealthAIException;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Eye diagnosis analysis module.
 *
 * <p>Supports detection of refractive, infection, inflammation, degenerative,
 * vascular, neurological, and structural eye conditions.</p>
 */
public class EyeDiagnosisModule {
    private final NostraHealthAI client;

    public EyeDiagnosisModule(NostraHealthAI client) {
        this.client = client;
    }

    /**
     * Analyze an eye image for conditions.
     *
     * @param file     Eye image file
     * @param eyeSide  Optional: "left", "right", or "both"
     * @param symptoms Optional list of symptoms
     * @return Job ID for polling results
     */
    public String analyze(File file, String eyeSide, List<String> symptoms) throws NostraHealthAIException {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(),
                        RequestBody.create(file, NostraHealthAI.getMimeType(file)));

        if (eyeSide != null) builder.addFormDataPart("eyeSide", eyeSide);
        if (symptoms != null) builder.addFormDataPart("symptoms", String.join(",", symptoms));

        Map<String, Object> result = client.postMultipart("/api/v1/ai/eye-diagnosis", builder.build());
        return (String) result.get("jobId");
    }

    /**
     * Analyze an eye image with extended parameters.
     */
    public String analyze(File file, String eyeSide, List<String> symptoms,
                          String symptomDuration, Integer painLevel, List<String> visionChanges,
                          List<String> medicalHistory, List<String> currentMedications,
                          List<String> allergies, Boolean wearingCorrectiveLenses)
            throws NostraHealthAIException {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(),
                        RequestBody.create(file, NostraHealthAI.getMimeType(file)));

        if (eyeSide != null) builder.addFormDataPart("eyeSide", eyeSide);
        if (symptoms != null) builder.addFormDataPart("symptoms", String.join(",", symptoms));
        if (symptomDuration != null) builder.addFormDataPart("symptomDuration", symptomDuration);
        if (painLevel != null) builder.addFormDataPart("painLevel", painLevel.toString());
        if (visionChanges != null) builder.addFormDataPart("visionChanges", String.join(",", visionChanges));
        if (medicalHistory != null) builder.addFormDataPart("medicalHistory", String.join(",", medicalHistory));
        if (currentMedications != null) builder.addFormDataPart("currentMedications", String.join(",", currentMedications));
        if (allergies != null) builder.addFormDataPart("allergies", String.join(",", allergies));
        if (wearingCorrectiveLenses != null) builder.addFormDataPart("wearingCorrectiveLenses", wearingCorrectiveLenses.toString());

        Map<String, Object> result = client.postMultipart("/api/v1/ai/eye-diagnosis", builder.build());
        return (String) result.get("jobId");
    }

    /**
     * Analyze an eye image with just the file.
     */
    public String analyze(File file) throws NostraHealthAIException {
        return analyze(file, null, null);
    }

    /**
     * Get the status of an eye diagnosis job.
     */
    public Map<String, Object> getJobStatus(String jobId) throws NostraHealthAIException {
        return client.get("/api/v1/ai/eye-diagnosis/job/" + jobId);
    }

    /**
     * Wait for an eye diagnosis job to complete.
     */
    public Map<String, Object> waitForCompletion(String jobId, long pollInterval, int maxAttempts)
            throws NostraHealthAIException, InterruptedException {
        int attempts = 0;
        while (attempts < maxAttempts) {
            Map<String, Object> status = getJobStatus(jobId);
            String statusStr = (String) status.get("status");

            if ("completed".equals(statusStr)) return status;
            if ("failed".equals(statusStr)) {
                throw new NostraHealthAIException("Eye diagnosis failed: " + status.getOrDefault("error", "Unknown error"));
            }

            Thread.sleep(pollInterval);
            attempts++;
        }
        throw new NostraHealthAIException("Eye diagnosis timeout");
    }

    /**
     * Wait for an eye diagnosis job to complete with default settings.
     */
    public Map<String, Object> waitForCompletion(String jobId) throws NostraHealthAIException, InterruptedException {
        return waitForCompletion(jobId, 2000, 60);
    }

    /**
     * Get all eye diagnoses for the current user.
     */
    public Map<String, Object> getAllAnalyses(Integer limit, String lastDocId) throws NostraHealthAIException {
        StringBuilder endpoint = new StringBuilder("/api/v1/ai/eye-diagnosis");
        String sep = "?";
        if (limit != null) { endpoint.append(sep).append("limit=").append(limit); sep = "&"; }
        if (lastDocId != null) { endpoint.append(sep).append("lastDocId=").append(lastDocId); }

        return client.get(endpoint.toString());
    }

    /**
     * Get all eye diagnoses with default pagination.
     */
    public Map<String, Object> getAllAnalyses() throws NostraHealthAIException {
        return getAllAnalyses(null, null);
    }

    /**
     * Get a specific eye diagnosis by ID.
     */
    public Map<String, Object> getAnalysis(String analysisId) throws NostraHealthAIException {
        Map<String, Object> result = client.get("/api/v1/ai/eye-diagnosis/" + analysisId);
        return NostraHealthAI.getDataMap(result);
    }

    /**
     * Delete an eye diagnosis.
     */
    public void deleteAnalysis(String analysisId) throws NostraHealthAIException {
        client.deleteRequest("/api/v1/ai/eye-diagnosis/" + analysisId);
    }

    /**
     * Get list of supported eye conditions.
     */
    public Map<String, Object> getSupportedConditions() throws NostraHealthAIException {
        Map<String, Object> result = client.get("/api/v1/ai/eye-diagnosis/conditions");
        return NostraHealthAI.getDataMap(result);
    }

    // =========================================================================
    // ASYNC METHODS
    // =========================================================================

    /**
     * Analyze an eye image asynchronously with basic parameters.
     */
    public CompletableFuture<String> analyzeAsync(File file, String eyeSide, List<String> symptoms) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return analyze(file, eyeSide, symptoms);
            } catch (NostraHealthAIException e) {
                throw new CompletionException(e);
            }
        }, client.getExecutorService());
    }

    /**
     * Analyze an eye image asynchronously with just the file.
     */
    public CompletableFuture<String> analyzeAsync(File file) {
        return analyzeAsync(file, null, null);
    }

    /**
     * Get the status of an eye diagnosis job asynchronously.
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
     * Get all eye diagnoses asynchronously.
     */
    public CompletableFuture<Map<String, Object>> getAllAnalysesAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getAllAnalyses();
            } catch (NostraHealthAIException e) {
                throw new CompletionException(e);
            }
        }, client.getExecutorService());
    }

    /**
     * Get a specific eye diagnosis by ID asynchronously.
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
     * Delete an eye diagnosis asynchronously.
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
