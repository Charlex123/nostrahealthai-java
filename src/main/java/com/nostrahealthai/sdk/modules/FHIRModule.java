package com.nostrahealthai.sdk.modules;

import com.nostrahealthai.sdk.NostraHealthAI;
import com.nostrahealthai.sdk.exceptions.NostraHealthAIException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * FHIR R4 module for healthcare interoperability.
 *
 * <p>Supports patient data management, observations, conditions, medications,
 * bundle export for hospital integration, and advanced search.</p>
 */
public class FHIRModule {
    private final NostraHealthAI client;

    public FHIRModule(NostraHealthAI client) {
        this.client = client;
    }

    /**
     * Get comprehensive patient summary (all FHIR resources).
     */
    public Map<String, Object> getPatientSummary() throws NostraHealthAIException {
        Map<String, Object> result = client.get("/api/v1/fhir/patient/summary");
        return NostraHealthAI.getDataMap(result);
    }

    /**
     * Get observations (vital signs, lab results).
     *
     * @param category Optional category filter (e.g., "vital-signs", "laboratory")
     */
    public List<Map<String, Object>> getObservations(String category) throws NostraHealthAIException {
        String endpoint = "/api/v1/fhir/observations";
        if (category != null) endpoint += "?category=" + category;

        Map<String, Object> result = client.get(endpoint);
        return NostraHealthAI.getDataList(result);
    }

    /**
     * Get all observations.
     */
    public List<Map<String, Object>> getObservations() throws NostraHealthAIException {
        return getObservations(null);
    }

    /**
     * Get conditions (diagnoses).
     */
    public List<Map<String, Object>> getConditions() throws NostraHealthAIException {
        Map<String, Object> result = client.get("/api/v1/fhir/conditions");
        return NostraHealthAI.getDataList(result);
    }

    /**
     * Get medication statements.
     */
    public List<Map<String, Object>> getMedications() throws NostraHealthAIException {
        Map<String, Object> result = client.get("/api/v1/fhir/medications");
        return NostraHealthAI.getDataList(result);
    }

    /**
     * Get allergy intolerances.
     */
    public List<Map<String, Object>> getAllergies() throws NostraHealthAIException {
        Map<String, Object> result = client.get("/api/v1/fhir/allergies");
        return NostraHealthAI.getDataList(result);
    }

    /**
     * Get document references (prescriptions, medical records).
     *
     * @param type Optional document type filter
     */
    public List<Map<String, Object>> getDocuments(String type) throws NostraHealthAIException {
        String endpoint = "/api/v1/fhir/documents";
        if (type != null) endpoint += "?type=" + type;

        Map<String, Object> result = client.get(endpoint);
        return NostraHealthAI.getDataList(result);
    }

    /**
     * Get all documents.
     */
    public List<Map<String, Object>> getDocuments() throws NostraHealthAIException {
        return getDocuments(null);
    }

    /**
     * Search FHIR records with advanced filters.
     *
     * @param params Search parameters (resourceType, category, status, startDate, endDate, limit)
     */
    public List<Map<String, Object>> search(Map<String, Object> params) throws NostraHealthAIException {
        Map<String, Object> result = client.postJson("/api/v1/fhir/search", params);
        return NostraHealthAI.getDataList(result);
    }

    /**
     * Get FHIR records by resource type.
     *
     * @param resourceType FHIR resource type (Patient, Observation, Condition, etc.)
     * @param limit        Optional maximum results
     */
    public List<Map<String, Object>> getByResourceType(String resourceType, Integer limit)
            throws NostraHealthAIException {
        String endpoint = "/api/v1/fhir/" + resourceType;
        if (limit != null) endpoint += "?limit=" + limit;

        Map<String, Object> result = client.get(endpoint);
        return NostraHealthAI.getDataList(result);
    }

    /**
     * Get FHIR records by resource type.
     */
    public List<Map<String, Object>> getByResourceType(String resourceType) throws NostraHealthAIException {
        return getByResourceType(resourceType, null);
    }

    /**
     * Get all FHIR records for the current user.
     */
    public Map<String, Object> getAllRecords(Integer limit, String lastDocId) throws NostraHealthAIException {
        StringBuilder endpoint = new StringBuilder("/api/v1/fhir");
        String sep = "?";
        if (limit != null) { endpoint.append(sep).append("limit=").append(limit); sep = "&"; }
        if (lastDocId != null) { endpoint.append(sep).append("lastDocId=").append(lastDocId); }

        return client.get(endpoint.toString());
    }

    /**
     * Get all FHIR records.
     */
    public Map<String, Object> getAllRecords() throws NostraHealthAIException {
        return getAllRecords(null, null);
    }

    /**
     * Get a specific FHIR record by ID.
     */
    public Map<String, Object> getRecord(String recordId) throws NostraHealthAIException {
        Map<String, Object> result = client.get("/api/v1/fhir/" + recordId);
        return NostraHealthAI.getDataMap(result);
    }

    /**
     * Update a FHIR record.
     */
    public Map<String, Object> updateRecord(String recordId, Map<String, Object> resource)
            throws NostraHealthAIException {
        Map<String, Object> body = new HashMap<>();
        body.put("resource", resource);

        Map<String, Object> result = client.putJson("/api/v1/fhir/" + recordId, body);
        return NostraHealthAI.getDataMap(result);
    }

    /**
     * Delete a FHIR record (soft delete).
     */
    public void deleteRecord(String recordId) throws NostraHealthAIException {
        client.deleteRequest("/api/v1/fhir/" + recordId);
    }

    /**
     * Archive a FHIR record.
     */
    public void archiveRecord(String recordId) throws NostraHealthAIException {
        client.postJson("/api/v1/fhir/" + recordId + "/archive", new HashMap<>());
    }

    /**
     * Export patient data as FHIR Bundle.
     *
     * @param resourceTypes Optional list of resource types to include
     */
    public Map<String, Object> exportBundle(List<String> resourceTypes) throws NostraHealthAIException {
        String endpoint = "/api/v1/fhir/export";
        if (resourceTypes != null && !resourceTypes.isEmpty()) {
            endpoint += "?types=" + String.join(",", resourceTypes);
        }

        Map<String, Object> result = client.get(endpoint);
        return NostraHealthAI.getDataMap(result);
    }

    /**
     * Export all patient data as FHIR Bundle.
     */
    public Map<String, Object> exportBundle() throws NostraHealthAIException {
        return exportBundle(null);
    }

    // =========================================================================
    // ASYNC METHODS
    // =========================================================================

    /**
     * Get comprehensive patient summary asynchronously.
     */
    public CompletableFuture<Map<String, Object>> getPatientSummaryAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getPatientSummary();
            } catch (NostraHealthAIException e) {
                throw new CompletionException(e);
            }
        }, client.getExecutorService());
    }

    /**
     * Get observations asynchronously.
     */
    public CompletableFuture<List<Map<String, Object>>> getObservationsAsync(String category) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getObservations(category);
            } catch (NostraHealthAIException e) {
                throw new CompletionException(e);
            }
        }, client.getExecutorService());
    }

    /**
     * Get all observations asynchronously.
     */
    public CompletableFuture<List<Map<String, Object>>> getObservationsAsync() {
        return getObservationsAsync(null);
    }

    /**
     * Get conditions asynchronously.
     */
    public CompletableFuture<List<Map<String, Object>>> getConditionsAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getConditions();
            } catch (NostraHealthAIException e) {
                throw new CompletionException(e);
            }
        }, client.getExecutorService());
    }

    /**
     * Get medications asynchronously.
     */
    public CompletableFuture<List<Map<String, Object>>> getMedicationsAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getMedications();
            } catch (NostraHealthAIException e) {
                throw new CompletionException(e);
            }
        }, client.getExecutorService());
    }

    /**
     * Get allergies asynchronously.
     */
    public CompletableFuture<List<Map<String, Object>>> getAllergiesAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getAllergies();
            } catch (NostraHealthAIException e) {
                throw new CompletionException(e);
            }
        }, client.getExecutorService());
    }

    /**
     * Get all FHIR records asynchronously.
     */
    public CompletableFuture<Map<String, Object>> getAllRecordsAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getAllRecords();
            } catch (NostraHealthAIException e) {
                throw new CompletionException(e);
            }
        }, client.getExecutorService());
    }

    /**
     * Get a specific FHIR record by ID asynchronously.
     */
    public CompletableFuture<Map<String, Object>> getRecordAsync(String recordId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getRecord(recordId);
            } catch (NostraHealthAIException e) {
                throw new CompletionException(e);
            }
        }, client.getExecutorService());
    }

    /**
     * Search FHIR records asynchronously.
     */
    public CompletableFuture<List<Map<String, Object>>> searchAsync(Map<String, Object> params) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return search(params);
            } catch (NostraHealthAIException e) {
                throw new CompletionException(e);
            }
        }, client.getExecutorService());
    }

    /**
     * Export patient data as FHIR Bundle asynchronously.
     */
    public CompletableFuture<Map<String, Object>> exportBundleAsync(List<String> resourceTypes) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return exportBundle(resourceTypes);
            } catch (NostraHealthAIException e) {
                throw new CompletionException(e);
            }
        }, client.getExecutorService());
    }

    /**
     * Export all patient data as FHIR Bundle asynchronously.
     */
    public CompletableFuture<Map<String, Object>> exportBundleAsync() {
        return exportBundleAsync(null);
    }

    /**
     * Delete a FHIR record asynchronously.
     */
    public CompletableFuture<Void> deleteRecordAsync(String recordId) {
        return CompletableFuture.runAsync(() -> {
            try {
                deleteRecord(recordId);
            } catch (NostraHealthAIException e) {
                throw new CompletionException(e);
            }
        }, client.getExecutorService());
    }
}
