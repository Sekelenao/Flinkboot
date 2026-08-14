# How to Load Resources (`Resource`)

Flinkboot provides a lightweight, unified, and zero-boilerplate `Resource` API in `flinkboot-core` to load files and assets across heterogeneous sources (classpath and file system) with a uniform URI syntax.

---

## 1. Overview

In Apache Flink streaming pipelines, applications frequently need to load side-inputs and configuration assets such as:
* Static lookup tables and reference datasets (JSON, CSV).
* Serialization schemas (Avro `.avsc`, JSON Schema, Protobuf).
* Security assets, certificates, and TLS keystores/truststores (JKS, PKCS12, PEM).
* Custom SQL scripts and templates.

The `Resource` API unifies these access patterns into a single interface with stateless, fail-fast stream access.

---

## 2. Maven Dependencies

Import the Flinkboot BOM in your `<dependencyManagement>` and add `flinkboot-core` in your `pom.xml`:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.sekelenao</groupId>
            <artifactId>flinkboot</artifactId>
            <version>${flinkboot.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <!-- Flinkboot Core -->
    <dependency>
        <groupId>io.github.sekelenao</groupId>
        <artifactId>flinkboot-core</artifactId>
    </dependency>
</dependencies>
```

---

## 3. Supported URI Schemes & Syntax

Use `Resource.of(location)` to instantiate a resource. Every location **must explicitly specify a valid scheme prefix**:

| Scheme Prefix | Target Source | Example |
|:---|:---|:---|
| `classpath:<path>` | JAR classpath resources | `Resource.of("classpath:schemas/user.avsc")` |
| `resource:<path>` | Alias for classpath resources | `Resource.of("resource:lookup-table.json")` |
| `file:<path>` | Local file system paths (absolute or relative) | `Resource.of("file:/etc/secrets/keystore.p12")` |

> [!NOTE]
> * Prefix matching is **case-insensitive** (`Classpath:`, `FILE:`, `resource:`).
> * Multiple leading slashes in classpath URIs (e.g. `classpath:///data.csv`) are automatically normalized.
> * Windows drive letters (e.g. `file:C:/configs/app.yaml`) and UNC network paths (e.g. `file://server/share/file.yaml`) are fully supported.

---

## 4. Reading Resources

`Resource.inputStream()` provides a new, independent `InputStream` on each invocation. Always consume resources within a **`try-with-resources`** block to ensure streams and file descriptors are properly closed:

```java
import io.github.sekelenao.flinkboot.core.api.resource.Resource;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ResourceExample {

    public static void main(String[] args) throws Exception {
        // Resolve a classpath resource
        Resource resource = Resource.of("classpath:lookup-table.json");

        // Read stream content safely
        try (InputStream in = resource.inputStream()) {
            String content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            System.out.println("Loaded content: " + content);
        }
    }
}
```

---

## 5. Usage in Apache Flink Pipelines

### A. Initialization on the JobManager (Startup)
You can load reference files or configurations directly in your `main` method before building the pipeline:

```java
public class MyFlinkJob {
    public static void main(String[] args) throws Exception {
        Flinkboot boot = Flinkboot.initialize(args);

        // Load reference lookup schema
        Resource schemaResource = Resource.of("classpath:schemas/event-schema.json");
        try (InputStream in = schemaResource.inputStream()) {
            // Initialize schema parser
        }

        // Build execution environment
        StreamExecutionEnvironment env = boot.executionEnvironment(...);
        // ...
        env.execute("My Streaming Job");
    }
}
```

### B. Initialization on TaskManagers (`RichFunction.open`)
When parallel TaskManagers need to load local volume mounts or classpath resources at task startup, initialize the `Resource` within the operator's `open()` lifecycle method:

```java
import io.github.sekelenao.flinkboot.core.api.resource.Resource;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.functions.RichMapFunction;

import java.io.InputStream;

public class LookupEnrichmentFunction extends RichMapFunction<Event, EnrichedEvent> {

    private transient LookupTable lookupTable;

    @Override
    public void open(OpenContext openContext) throws Exception {
        // Load mounted lookup file locally on each TaskManager
        Resource resource = Resource.of("file:/etc/podinfo/lookup-rules.json");
        try (InputStream in = resource.inputStream()) {
            this.lookupTable = LookupTable.parse(in);
        }
    }

    @Override
    public EnrichedEvent map(Event value) {
        return lookupTable.enrich(value);
    }
}
```

> [!CAUTION]
> `Resource` instances are **not `Serializable`**. Do NOT assign a `Resource` to an operator field without `transient`, as Flink's closure serializer will fail with a `NotSerializableException`. Always instantiate `Resource` locally inside `open()` or `main()`.

---

## 6. Exception Handling

All exceptions thrown by the `Resource` API inherit from `FlinkbootException`:

| Exception | Root Cause |
|:---|:---|
| `ResourceNotFoundException` | The file does not exist on disk or cannot be found on the classpath. |
| `ResourceAccessException` | The target location is a directory, inaccessible due to I/O permissions, or has an invalid path syntax. |
| `UnrecognizedResourceException` | The location string is null, empty/blank, or does not specify a supported scheme prefix (`classpath:`, `resource:`, `file:`). |

Example handling:

```java
try {
    Resource resource = Resource.of("file:/invalid/path/config.yaml");
    try (InputStream in = resource.inputStream()) {
        // Process stream
    }
} catch (ResourceNotFoundException e) {
    System.err.println("File not found: " + e.getMessage());
} catch (ResourceAccessException e) {
    System.err.println("Failed to access resource: " + e.getMessage());
} catch (UnrecognizedResourceException e) {
    System.err.println("Invalid resource scheme: " + e.getMessage());
}
```
