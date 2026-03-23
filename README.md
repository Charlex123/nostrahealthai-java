# NostraHealthAI Java SDK

Official Java SDK for the **NostraHealthAI Medical AI Platform**.

## Requirements

- Java 11+
- Maven or Gradle

## Installation

### Maven

```xml
<dependency>
    <groupId>com.nostrahealthai</groupId>
    <artifactId>sdk</artifactId>
    <version>2.0.0</version>
</dependency>
```

### Gradle

```groovy
implementation 'com.nostrahealthaiai:sdk:2.0.0'
```

## Quick Start

```java
import com.nostrahealthai.sdk.NostraHealthAI;
import com.nostrahealthai.sdk.exceptions.NostraHealthAIException;
import java.io.File;
import java.util.Map;

public class Example {
    public static void main(String[] args) throws Exception {
        NostraHealthAI nostra = new NostraHealthAI.Builder()
            .apiKey("your-api-key")
            .build();

        // Medical Chat
        Map<String, Object> response = nostra.chat("What are the symptoms of diabetes?");
        System.out.println(response.get("response"));

        // Skin Analysis
        String jobId = nostra.skin().analyze(new File("skin_image.jpg"));
        Map<String, Object> result = nostra.skin().waitForCompletion(jobId);

        // Eye Diagnosis
        String eyeJobId = nostra.eye().analyze(new File("eye_image.jpg"));
        Map<String, Object> eyeResult = nostra.eye().waitForCompletion(eyeJobId);

        // Drug Verification
        Map<String, Object> verification = nostra.drug().verify("Aspirin", "Bayer");

        // FHIR Records
        Map<String, Object> summary = nostra.fhir().getPatientSummary();

        // Subscription & Usage
        Map<String, Object> plans = nostra.subscriptions().getPlans();
        Map<String, Object> usage = nostra.subscriptions().getAiUsage();
    }
}
```

## Modules

| Module | Access | Description |
|--------|--------|-------------|
| Medical Chat | `nostra.chat()` | AI-powered medical conversations |
| Skin Analysis | `nostra.skin()` | Skin infection detection |
| Eye Diagnosis | `nostra.eye()` | Eye condition analysis |
| Wound Healing | `nostra.wound()` | Wound tracking and analysis |
| Drug Verification | `nostra.drug()` | Drug authenticity verification |
| FHIR R4 | `nostra.fhir()` | Healthcare interoperability |
| Subscriptions | `nostra.subscriptions()` | Plan management and usage |

## Building

```bash
cd sdk/java
mvn clean install
```

## License

MIT License
