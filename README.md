# Flinkboot

> **Speed & Safety by design.** Zero-boilerplate configuration that fails fast to keep your Flink pipelines running.

[![Java](https://img.shields.io/badge/Java_11-%23ED8B00.svg?logo=openjdk&logoColor=white)](https://docs.oracle.com/en/java/javase/11/docs/api/index.html)
[![Flink](https://img.shields.io/badge/Flink_1.20-%23E6526F.svg?logo=apacheflink&logoColor=white)](https://flink.apache.org/)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.sekelenao/flinkboot-core?label=Maven%20central&logo=apachemaven&logoColor=white&color=E6526F&labelColor=E6526F)](https://central.sonatype.com/artifact/io.github.sekelenao/flinkboot-core)

### 📖 How-To Guides
- [How-To Index](howto/README.md)

---

## What is Flinkboot?

**Flinkboot** is a lightweight, high-performance configuration utility designed to get your Apache Flink applications up and running in seconds. It unifies command-line arguments, environment variables, and hierarchical YAML configuration files into a single, validated Java model with **zero boilerplate**.

By combining clean Java configuration POJOs and Jakarta Bean Validation (JSR-380), Flinkboot allows you to configure your jobs instantly while guaranteeing **fail-early safety**: typos, missing keys, or out-of-range parameters are caught on the JobManager immediately at startup, preventing jobs from failing mid-execution on the cluster.

---

## Key Features

- **Instant Setup** — Define your configuration as a simple Java class and load it with a single line of code.
- **Fail-Fast Validation** — Catch typos and invalid values at startup, before Flink resources are allocated.
- **Fail-Safe Merge Semantics** — By default, Flinkboot prevents accidental overrides during merges (throws an exception on key conflicts) unless explicit permission is granted.
- **Layered Overrides & List Merging** — Optionally allow property overrides (`--flinkboot-configuration-override`) and list merging/appending (`--flinkboot-configuration-list-merging`) when resolving multiple configurations.
- **Flexible Sources** — Automatically resolves configuration file locations from command-line options (`-flinkboot-configurations`) or environment variables (`FLINKBOOT_CONFIGURATIONS`).

---

## Quick Start Example

Define your configuration as an immutable Java class with Jakarta validation constraints:

```java
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public final class JobConfig {

    @NotBlank 
    private final String jobName;

    @Min(1) 
    private final int parallelism;

    @JsonCreator
    public JobConfig(
        @JsonProperty("jobName") String jobName,
        @JsonProperty("parallelism") int parallelism
    ) {
        this.jobName = jobName;
        this.parallelism = parallelism;
    }

    public String jobName() { return jobName; }
    public int parallelism() { return parallelism; }
}
```

Then, load and validate your configuration with a single line of code in your job's main class:

```java
import io.github.sekelenao.flinkboot.core.api.Flinkboot;

public class UserActivityJob {
    public static void main(String[] args) throws Exception {
        // Load, merge, and validate configurations in one step
        JobConfig config = Flinkboot.initialize(args).configuration(JobConfig.class);

        System.out.println("Running Flink Job: " + config.jobName());
    }
}
```

---

## Command Line Usage

To run your job with custom configuration locations and options:
```bash
flink run -c MyJobJar.jar \
  -flinkboot-configurations file:/etc/configs/base-config.yaml,file:/etc/configs/prod-config.yaml \
  --flinkboot-configuration-override
```

For advanced CLI options, environment variables configuration, and detailed merging semantics, refer to the [How-To Index](howto/README.md).

---

## Contributing & Development

Flinkboot is built using Maven. To run tests locally:
```bash
mvn clean test
```

*Not affiliated with the Apache Software Foundation or the Apache Flink project.*