package com.nostrahealthai.sdk;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.nostrahealthai.sdk.exceptions.NostraHealthAIException;
import com.nostrahealthai.sdk.modules.*;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

/**
 * NostraHealthAI SDK Client
 *
 * <p>Official Java SDK for interacting with NostraHealthAI Medical AI Platform.</p>
 *
 * <pre>{@code
 * NostraHealthAI nostra = new NostraHealthAI.Builder()
 *     .apiKey("your-api-key")
 *     .build();
 *
 * // Medical Chat
 * Map<String, Object> response = nostra.chat("What causes high blood pressure?");
 *
 * // Skin Analysis
 * String jobId = nostra.skin().analyze(new File("skin_image.jpg"));
 * Map<String, Object> result = nostra.skin().waitForCompletion(jobId);
 *
 * // Subscriptions
 * Map<String, Object> plans = nostra.subscriptions().getPlans();
 * Map<String, Object> usage = nostra.subscriptions().getAiUsage();
 * }</pre>
 */
public class NostraHealthAI {
    private static final String DEFAULT_BASE_URL = "https://www.api.nostrahealth.com";
    private static final int DEFAULT_TIMEOUT = 60;

    private final OkHttpClient httpClient;
    private final String baseUrl;
    private final String apiKey;
    private final Gson gson;
    private final ExecutorService executorService;
    private final boolean ownsExecutor;

    // Sub-modules
    private final SkinInfectionModule skinModule;
    private final EyeDiagnosisModule eyeModule;
    private final WoundHealingModule woundModule;
    private final DrugVerificationModule drugModule;
    private final FHIRModule fhirModule;
    private final SubscriptionModule subscriptionModule;

    private NostraHealthAI(Builder builder) {
        this.apiKey = builder.apiKey;
        this.baseUrl = builder.baseUrl != null ? builder.baseUrl : DEFAULT_BASE_URL;
        int timeout = builder.timeout > 0 ? builder.timeout : DEFAULT_TIMEOUT;

        if (builder.executorService != null) {
            this.executorService = builder.executorService;
            this.ownsExecutor = true;
        } else {
            this.executorService = ForkJoinPool.commonPool();
            this.ownsExecutor = false;
        }

        this.gson = new GsonBuilder().create();

        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request request = original.newBuilder()
                            .header("Authorization", "Bearer " + apiKey)
                            .header("Accept", "application/json")
                            .build();
                    return chain.proceed(request);
                })
                .build();

        this.skinModule = new SkinInfectionModule(this);
        this.eyeModule = new EyeDiagnosisModule(this);
        this.woundModule = new WoundHealingModule(this);
        this.drugModule = new DrugVerificationModule(this);
        this.fhirModule = new FHIRModule(this);
        this.subscriptionModule = new SubscriptionModule(this);
    }

    // =========================================================================
    // MODULE ACCESSORS
    // =========================================================================

    public SkinInfectionModule skin() { return skinModule; }
    public EyeDiagnosisModule eye() { return eyeModule; }
    public WoundHealingModule wound() { return woundModule; }
    public DrugVerificationModule drug() { return drugModule; }
    public FHIRModule fhir() { return fhirModule; }
    public SubscriptionModule subscriptions() { return subscriptionModule; }

    /**
     * Get the executor service used for async operations.
     */
    public ExecutorService getExecutorService() { return executorService; }

    /**
     * Shutdown the executor service if a custom one was provided.
     * This should be called when the client is no longer needed.
     */
    public void shutdown() {
        if (ownsExecutor && executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    // =========================================================================
    // ASYNC CHAT METHODS
    // =========================================================================

    /**
     * Send a message to the medical AI assistant asynchronously.
     */
    public CompletableFuture<Map<String, Object>> chatAsync(String message) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return chat(message);
            } catch (NostraHealthAIException e) {
                throw new java.util.concurrent.CompletionException(e);
            }
        }, executorService);
    }

    /**
     * Send a message to the medical AI assistant asynchronously with conversation ID.
     */
    public CompletableFuture<Map<String, Object>> chatAsync(String message, String conversationId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return chat(message, conversationId);
            } catch (NostraHealthAIException e) {
                throw new java.util.concurrent.CompletionException(e);
            }
        }, executorService);
    }

    /**
     * Send an audio file for voice-based conversation asynchronously.
     */
    public CompletableFuture<Map<String, Object>> audioChatAsync(File audioFile, String conversationId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return audioChat(audioFile, conversationId);
            } catch (NostraHealthAIException e) {
                throw new java.util.concurrent.CompletionException(e);
            }
        }, executorService);
    }

    /**
     * Get all conversations asynchronously.
     */
    public CompletableFuture<List<Map<String, Object>>> getConversationsAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getConversations();
            } catch (NostraHealthAIException e) {
                throw new java.util.concurrent.CompletionException(e);
            }
        }, executorService);
    }

    /**
     * Analyze a medical file asynchronously.
     */
    public CompletableFuture<String> analyzeFileAsync(File file) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return analyzeFile(file);
            } catch (NostraHealthAIException e) {
                throw new java.util.concurrent.CompletionException(e);
            }
        }, executorService);
    }

    // =========================================================================
    // MEDICAL CHAT
    // =========================================================================

    /**
     * Send a message to the medical AI assistant.
     *
     * @param message User message to send
     * @return Chat response with AI message and model info
     */
    public Map<String, Object> chat(String message) throws NostraHealthAIException {
        return chat(message, null);
    }

    /**
     * Send a message to the medical AI assistant.
     *
     * @param message        User message to send
     * @param conversationId Optional conversation ID to continue
     * @return Chat response with AI message and model info
     */
    public Map<String, Object> chat(String message, String conversationId) throws NostraHealthAIException {
        Map<String, Object> body = new HashMap<>();
        body.put("message", message);
        if (conversationId != null) {
            body.put("conversationId", conversationId);
        }

        Map<String, Object> result = postJson("/api/v1/ai/chat", body);
        return getDataMap(result);
    }

    /**
     * Send an audio file for voice-based conversation.
     *
     * @param audioFile      Audio file to send
     * @param conversationId Optional conversation ID
     * @return Response with transcription and AI response
     */
    public Map<String, Object> audioChat(File audioFile, String conversationId) throws NostraHealthAIException {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("audio", audioFile.getName(),
                        RequestBody.create(audioFile, getMimeType(audioFile)));

        if (conversationId != null) {
            builder.addFormDataPart("conversationId", conversationId);
        }

        Map<String, Object> result = postMultipart("/api/v1/ai/audio-chat", builder.build());
        return getDataMap(result);
    }

    /**
     * Get all conversations for the current user.
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getConversations() throws NostraHealthAIException {
        Map<String, Object> result = get("/api/v1/ai/conversations");
        return (List<Map<String, Object>>) result.get("data");
    }

    /**
     * Get messages for a specific conversation.
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getConversationMessages(String conversationId) throws NostraHealthAIException {
        Map<String, Object> result = get("/api/v1/ai/conversations/" + conversationId + "/messages");
        return (List<Map<String, Object>>) result.get("data");
    }

    // =========================================================================
    // MEDICAL FILE ANALYSIS
    // =========================================================================

    /**
     * Analyze a medical file (image, lab report, etc.).
     *
     * @param file File to analyze
     * @return Job ID for polling the analysis result
     */
    public String analyzeFile(File file) throws NostraHealthAIException {
        MultipartBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(),
                        RequestBody.create(file, getMimeType(file)))
                .build();

        Map<String, Object> result = postMultipart("/api/v1/ai/analyze", body);
        return (String) result.get("jobId");
    }

    /**
     * Get the status of a file analysis job.
     */
    public Map<String, Object> getJobStatus(String jobId) throws NostraHealthAIException {
        return get("/api/v1/ai/job/" + jobId);
    }

    /**
     * Wait for a file analysis job to complete.
     *
     * @param jobId        Job ID
     * @param pollInterval Polling interval in milliseconds
     * @param maxAttempts  Maximum polling attempts
     * @return Completed job status with results
     */
    public Map<String, Object> waitForJobCompletion(String jobId, long pollInterval, int maxAttempts)
            throws NostraHealthAIException, InterruptedException {
        int attempts = 0;

        while (attempts < maxAttempts) {
            Map<String, Object> status = getJobStatus(jobId);
            String statusStr = (String) status.get("status");

            if ("completed".equals(statusStr)) {
                return status;
            }
            if ("failed".equals(statusStr)) {
                throw new NostraHealthAIException("Job failed: " + status.getOrDefault("error", "Unknown error"));
            }

            Thread.sleep(pollInterval);
            attempts++;
        }

        throw new NostraHealthAIException("Job polling timeout after " + maxAttempts + " attempts");
    }

    /**
     * Wait for a file analysis job to complete with default settings.
     */
    public Map<String, Object> waitForJobCompletion(String jobId) throws NostraHealthAIException, InterruptedException {
        return waitForJobCompletion(jobId, 2000, 60);
    }

    /**
     * Get all analysis jobs for the current user.
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getUserJobs() throws NostraHealthAIException {
        Map<String, Object> result = get("/api/v1/ai/jobs");
        return (List<Map<String, Object>>) result.get("jobs");
    }

    // =========================================================================
    // HTTP METHODS (public for module access from sub-packages)
    // =========================================================================

    public Map<String, Object> get(String endpoint) throws NostraHealthAIException {
        Request request = new Request.Builder()
                .url(baseUrl + endpoint)
                .get()
                .build();
        return executeRequest(request);
    }

    public Map<String, Object> postJson(String endpoint, Map<String, Object> body) throws NostraHealthAIException {
        RequestBody requestBody = RequestBody.create(
                gson.toJson(body),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(baseUrl + endpoint)
                .post(requestBody)
                .build();
        return executeRequest(request);
    }

    public Map<String, Object> putJson(String endpoint, Map<String, Object> body) throws NostraHealthAIException {
        RequestBody requestBody = RequestBody.create(
                gson.toJson(body),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(baseUrl + endpoint)
                .put(requestBody)
                .build();
        return executeRequest(request);
    }

    public Map<String, Object> deleteRequest(String endpoint) throws NostraHealthAIException {
        Request request = new Request.Builder()
                .url(baseUrl + endpoint)
                .delete()
                .build();
        return executeRequest(request);
    }

    public Map<String, Object> deleteWithBody(String endpoint, Map<String, Object> body) throws NostraHealthAIException {
        RequestBody requestBody = RequestBody.create(
                gson.toJson(body),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(baseUrl + endpoint)
                .delete(requestBody)
                .build();
        return executeRequest(request);
    }

    public Map<String, Object> postMultipart(String endpoint, MultipartBody body) throws NostraHealthAIException {
        Request request = new Request.Builder()
                .url(baseUrl + endpoint)
                .post(body)
                .build();
        return executeRequest(request);
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    @SuppressWarnings("unchecked")
    private Map<String, Object> executeRequest(Request request) throws NostraHealthAIException {
        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";

            Type type = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> data = gson.fromJson(responseBody, type);

            if (!response.isSuccessful()) {
                String error = data != null && data.containsKey("error")
                        ? (String) data.get("error")
                        : "HTTP " + response.code();
                throw new NostraHealthAIException(error, response.code(), data);
            }

            if (data != null && Boolean.FALSE.equals(data.get("success"))) {
                String error = data.containsKey("error") ? (String) data.get("error") : "Request failed";
                throw new NostraHealthAIException(error, response.code(), data);
            }

            return data != null ? data : new HashMap<>();
        } catch (IOException e) {
            throw new NostraHealthAIException(e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getDataMap(Map<String, Object> result) {
        Object data = result.get("data");
        if (data instanceof Map) {
            return (Map<String, Object>) data;
        }
        return new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> getDataList(Map<String, Object> result) {
        Object data = result.get("data");
        if (data instanceof List) {
            return (List<Map<String, Object>>) data;
        }
        return List.of();
    }

    public static MediaType getMimeType(File file) {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".png")) return MediaType.parse("image/png");
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return MediaType.parse("image/jpeg");
        if (name.endsWith(".gif")) return MediaType.parse("image/gif");
        if (name.endsWith(".webp")) return MediaType.parse("image/webp");
        if (name.endsWith(".pdf")) return MediaType.parse("application/pdf");
        if (name.endsWith(".mp3")) return MediaType.parse("audio/mpeg");
        if (name.endsWith(".wav")) return MediaType.parse("audio/wav");
        if (name.endsWith(".webm")) return MediaType.parse("audio/webm");
        if (name.endsWith(".m4a")) return MediaType.parse("audio/mp4");
        return MediaType.parse("application/octet-stream");
    }

    public Gson getGson() {
        return gson;
    }

    // =========================================================================
    // BUILDER
    // =========================================================================

    public static class Builder {
        private String apiKey;
        private String baseUrl;
        private int timeout;
        private ExecutorService executorService;

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder timeout(int timeoutSeconds) {
            this.timeout = timeoutSeconds;
            return this;
        }

        /**
         * Set a custom ExecutorService for async operations.
         * If not set, {@link ForkJoinPool#commonPool()} will be used.
         *
         * @param executorService Custom executor service
         * @return this builder
         */
        public Builder executorService(ExecutorService executorService) {
            this.executorService = executorService;
            return this;
        }

        public NostraHealthAI build() {
            if (apiKey == null || apiKey.isEmpty()) {
                throw new IllegalArgumentException("API key is required");
            }
            return new NostraHealthAI(this);
        }
    }
}
