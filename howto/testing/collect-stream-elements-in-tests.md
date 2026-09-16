# How to Collect Stream Elements in Tests

Flinkboot provides the thread-safe `CollectingSink<T>` utility in `flinkboot-test` to collect elements emitted by Apache Flink streams during tests.

---

## 1. Overview

`CollectingSink<T>` is an in-memory Flink sink designed for tests. Add it to a `DataStream`, execute the job, and inspect the collected elements after the execution finishes.

The public API consists of:
* `CollectingSink.create()` creates a new sink.
* `sink.elements()` returns an immutable snapshot of the collected elements.
* `sink.clear()` removes the elements collected by that sink.

The sink safely collects elements from parallel sink subtasks during local Flink test execution.

---

## 2. Maven Dependencies

Import the Flinkboot BOM in your `<dependencyManagement>` and add `flinkboot-test` in your `pom.xml`:

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
    <!-- Flinkboot Test Utilities -->
    <dependency>
        <groupId>io.github.sekelenao</groupId>
        <artifactId>flinkboot-test</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- Flink Local Test Execution -->
    <dependency>
        <groupId>org.apache.flink</groupId>
        <artifactId>flink-clients</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

`flink-clients` provides the local executor used by `StreamExecutionEnvironment` when the test calls `env.execute()`.

---

## 3. Collecting Stream Elements

Create the sink, add it to the stream with `addSink(sink)`, and execute the environment before reading the collected elements:

```java
import io.github.sekelenao.flinkboot.test.api.CollectingSink;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StreamProcessingTest {

    @Test
    @DisplayName("Should collect elements emitted by a Flink stream")
    void shouldCollectStreamElements() throws Exception {
        var env = StreamExecutionEnvironment.getExecutionEnvironment();
        var sink = CollectingSink.<String>create();

        env.fromElements("event-1", "event-2")
                .addSink(sink)
                .setParallelism(2);
        env.execute();

        var elements = sink.elements();

        assertAll(
            () -> assertEquals(2, elements.size()),
            () -> assertTrue(elements.containsAll(List.of("event-1", "event-2")))
        );
    }
}
```

The sink supports parallel execution, but Flink does not guarantee a global element order across parallel sink subtasks. Assert the number and contents of the collected elements without relying on their order.

---

## 4. Immutable Snapshots and Clearing the Sink

Each call to `elements()` returns an immutable snapshot of the elements collected at that moment.

A snapshot remains unchanged if more elements are collected later or if `clear()` is called.

Use `clear()` when you want to reuse a sink without previously collected elements:

```java
var snapshot = sink.elements();

sink.clear();

assertAll(
    () -> assertEquals(2, snapshot.size()),
    () -> assertTrue(sink.elements().isEmpty())
);
```
