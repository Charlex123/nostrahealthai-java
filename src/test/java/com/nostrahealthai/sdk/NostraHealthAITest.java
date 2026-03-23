package com.nostrahealthai.sdk;

import com.nostrahealthai.sdk.exceptions.NostraHealthAIException;
import com.nostrahealthai.sdk.modules.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for NostraHealthAI SDK.
 * Tests SDK construction, module initialization, and error handling.
 */
class NostraHealthAITest {

    @Test
    void testBuilderRequiresApiKey() {
        assertThrows(IllegalArgumentException.class, () ->
            new NostraHealthAI.Builder().build()
        );
    }

    @Test
    void testBuilderWithEmptyApiKey() {
        assertThrows(IllegalArgumentException.class, () ->
            new NostraHealthAI.Builder().apiKey("").build()
        );
    }

    @Test
    void testBuilderCreatesClient() {
        NostraHealthAI client = new NostraHealthAI.Builder()
            .apiKey("test-api-key")
            .build();
        assertNotNull(client);
    }

    @Test
    void testBuilderWithCustomBaseUrl() {
        NostraHealthAI client = new NostraHealthAI.Builder()
            .apiKey("test-api-key")
            .baseUrl("https://custom.api.com")
            .build();
        assertNotNull(client);
    }

    @Test
    void testBuilderWithTimeout() {
        NostraHealthAI client = new NostraHealthAI.Builder()
            .apiKey("test-api-key")
            .timeout(30)
            .build();
        assertNotNull(client);
    }

    @Test
    void testModulesInitialized() {
        NostraHealthAI client = new NostraHealthAI.Builder()
            .apiKey("test-api-key")
            .build();

        assertNotNull(client.skin(), "Skin module should be initialized");
        assertNotNull(client.eye(), "Eye module should be initialized");
        assertNotNull(client.wound(), "Wound module should be initialized");
        assertNotNull(client.drug(), "Drug module should be initialized");
        assertNotNull(client.fhir(), "FHIR module should be initialized");
        assertNotNull(client.subscriptions(), "Subscription module should be initialized");
    }

    @Test
    void testSkinModuleType() {
        NostraHealthAI client = new NostraHealthAI.Builder()
            .apiKey("test-api-key")
            .build();
        assertInstanceOf(SkinInfectionModule.class, client.skin());
    }

    @Test
    void testEyeModuleType() {
        NostraHealthAI client = new NostraHealthAI.Builder()
            .apiKey("test-api-key")
            .build();
        assertInstanceOf(EyeDiagnosisModule.class, client.eye());
    }

    @Test
    void testWoundModuleType() {
        NostraHealthAI client = new NostraHealthAI.Builder()
            .apiKey("test-api-key")
            .build();
        assertInstanceOf(WoundHealingModule.class, client.wound());
    }

    @Test
    void testDrugModuleType() {
        NostraHealthAI client = new NostraHealthAI.Builder()
            .apiKey("test-api-key")
            .build();
        assertInstanceOf(DrugVerificationModule.class, client.drug());
    }

    @Test
    void testFhirModuleType() {
        NostraHealthAI client = new NostraHealthAI.Builder()
            .apiKey("test-api-key")
            .build();
        assertInstanceOf(FHIRModule.class, client.fhir());
    }

    @Test
    void testSubscriptionModuleType() {
        NostraHealthAI client = new NostraHealthAI.Builder()
            .apiKey("test-api-key")
            .build();
        assertInstanceOf(SubscriptionModule.class, client.subscriptions());
    }

    @Test
    void testMimeTypeDetection() {
        assertEquals("image/png", NostraHealthAI.getMimeType(new java.io.File("test.png")).toString());
        assertEquals("image/jpeg", NostraHealthAI.getMimeType(new java.io.File("test.jpg")).toString());
        assertEquals("image/jpeg", NostraHealthAI.getMimeType(new java.io.File("test.jpeg")).toString());
        assertEquals("application/pdf", NostraHealthAI.getMimeType(new java.io.File("test.pdf")).toString());
        assertEquals("audio/mpeg", NostraHealthAI.getMimeType(new java.io.File("test.mp3")).toString());
        assertEquals("audio/webm", NostraHealthAI.getMimeType(new java.io.File("test.webm")).toString());
        assertEquals("application/octet-stream", NostraHealthAI.getMimeType(new java.io.File("test.xyz")).toString());
    }

    @Test
    void testExceptionProperties() {
        NostraHealthAIException ex = new NostraHealthAIException("Test error", 400, null);
        assertEquals("Test error", ex.getMessage());
        assertEquals(400, ex.getStatusCode());
        assertNull(ex.getResponse());
    }

    @Test
    void testExceptionWithoutStatusCode() {
        NostraHealthAIException ex = new NostraHealthAIException("Network error");
        assertEquals("Network error", ex.getMessage());
        assertNull(ex.getStatusCode());
        assertNull(ex.getResponse());
    }
}
