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
 * Drug verification module.
 *
 * <p>Supports single and batch drug verification, NDC and barcode validation,
 * counterfeit detection, and recall information.</p>
 */
public class DrugVerificationModule {
    private final NostraHealthAI client;

    public DrugVerificationModule(NostraHealthAI client) {
        this.client = client;
    }

    /**
     * Verify a drug's authenticity.
     *
     * @param drugName     Optional drug name
     * @param manufacturer Optional manufacturer
     * @param batchNumber  Optional batch number
     * @param ndc          Optional National Drug Code
     * @param barcode      Optional barcode
     * @param image        Optional drug package image
     * @return Verification result
     */
    public Map<String, Object> verify(String drugName, String manufacturer, String batchNumber,
                                       String ndc, String barcode, File image) throws NostraHealthAIException {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);

        if (drugName != null) builder.addFormDataPart("drugName", drugName);
        if (manufacturer != null) builder.addFormDataPart("manufacturer", manufacturer);
        if (batchNumber != null) builder.addFormDataPart("batchNumber", batchNumber);
        if (ndc != null) builder.addFormDataPart("ndc", ndc);
        if (barcode != null) builder.addFormDataPart("barcode", barcode);
        if (image != null) {
            builder.addFormDataPart("image", image.getName(),
                    RequestBody.create(image, NostraHealthAI.getMimeType(image)));
        }

        Map<String, Object> result = client.postMultipart("/api/v1/ai/drug-verification/verify", builder.build());
        return NostraHealthAI.getDataMap(result);
    }

    /**
     * Verify a drug by name and manufacturer.
     */
    public Map<String, Object> verify(String drugName, String manufacturer) throws NostraHealthAIException {
        return verify(drugName, manufacturer, null, null, null, null);
    }

    /**
     * Verify multiple drugs in batch.
     *
     * @param drugs List of drug verification request maps
     * @return List of verification results
     */
    public List<Map<String, Object>> batchVerify(List<Map<String, Object>> drugs) throws NostraHealthAIException {
        Map<String, Object> body = new HashMap<>();
        body.put("drugs", drugs);

        Map<String, Object> result = client.postJson("/api/v1/ai/drug-verification/verify-batch", body);
        return NostraHealthAI.getDataList(result);
    }

    /**
     * Get all drug verifications for the current user.
     */
    public List<Map<String, Object>> getVerifications() throws NostraHealthAIException {
        Map<String, Object> result = client.get("/api/v1/ai/drug-verification/verifications");
        return NostraHealthAI.getDataList(result);
    }

    /**
     * Get a specific drug verification by ID.
     */
    public Map<String, Object> getVerification(String verificationId) throws NostraHealthAIException {
        Map<String, Object> result = client.get("/api/v1/ai/drug-verification/verifications/" + verificationId);
        return NostraHealthAI.getDataMap(result);
    }

    /**
     * Get drug verification statistics.
     */
    public Map<String, Object> getStats() throws NostraHealthAIException {
        Map<String, Object> result = client.get("/api/v1/ai/drug-verification/verifications/stats");
        return NostraHealthAI.getDataMap(result);
    }

    /**
     * Delete a drug verification record.
     */
    public void deleteVerification(String verificationId) throws NostraHealthAIException {
        client.deleteRequest("/api/v1/ai/drug-verification/verifications/" + verificationId);
    }

    // =========================================================================
    // ASYNC METHODS
    // =========================================================================

    /**
     * Verify a drug's authenticity asynchronously with full parameters.
     */
    public CompletableFuture<Map<String, Object>> verifyAsync(String drugName, String manufacturer, String batchNumber,
                                                               String ndc, String barcode, File image) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return verify(drugName, manufacturer, batchNumber, ndc, barcode, image);
            } catch (NostraHealthAIException e) {
                throw new CompletionException(e);
            }
        }, client.getExecutorService());
    }

    /**
     * Verify a drug by name and manufacturer asynchronously.
     */
    public CompletableFuture<Map<String, Object>> verifyAsync(String drugName, String manufacturer) {
        return verifyAsync(drugName, manufacturer, null, null, null, null);
    }

    /**
     * Verify multiple drugs in batch asynchronously.
     */
    public CompletableFuture<List<Map<String, Object>>> batchVerifyAsync(List<Map<String, Object>> drugs) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return batchVerify(drugs);
            } catch (NostraHealthAIException e) {
                throw new CompletionException(e);
            }
        }, client.getExecutorService());
    }

    /**
     * Get all drug verifications asynchronously.
     */
    public CompletableFuture<List<Map<String, Object>>> getVerificationsAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getVerifications();
            } catch (NostraHealthAIException e) {
                throw new CompletionException(e);
            }
        }, client.getExecutorService());
    }

    /**
     * Get a specific drug verification by ID asynchronously.
     */
    public CompletableFuture<Map<String, Object>> getVerificationAsync(String verificationId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getVerification(verificationId);
            } catch (NostraHealthAIException e) {
                throw new CompletionException(e);
            }
        }, client.getExecutorService());
    }

    /**
     * Get drug verification statistics asynchronously.
     */
    public CompletableFuture<Map<String, Object>> getStatsAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getStats();
            } catch (NostraHealthAIException e) {
                throw new CompletionException(e);
            }
        }, client.getExecutorService());
    }

    /**
     * Delete a drug verification record asynchronously.
     */
    public CompletableFuture<Void> deleteVerificationAsync(String verificationId) {
        return CompletableFuture.runAsync(() -> {
            try {
                deleteVerification(verificationId);
            } catch (NostraHealthAIException e) {
                throw new CompletionException(e);
            }
        }, client.getExecutorService());
    }
}
