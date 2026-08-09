# How to Serialize Common JDK Types (Time, Duration, Collections)

Apache Flink does not register default `TypeInfoFactory` mappings for modern Java Date/Time classes (`Instant`, `LocalDateTime`, `LocalDate`, `LocalTime`), `Duration`, or generic Collection interfaces (`List`, `Map`) in POJO fields. 

Without custom factories, Flink's `TypeExtractor` falls back to **Kryo serialization**, which is slow, space-inefficient, and prone to state migration issues in stateful streaming applications.

Flinkboot provides built-in, optimized `TypeInfoFactory` classes to enable native Flink serialization for these types.

---

## 1. Available Factories

The following factories are available in Flinkboot:

### Time & Duration Types (`io.github.sekelenao.flinkboot.core.api.typing.time`)

| JDK Type | Flinkboot Factory Class | Under the hood Flink Type |
| :--- | :--- | :--- |
| `java.time.Instant` | `InstantTypeInfoFactory` | `Types.INSTANT` |
| `java.time.LocalDateTime` | `LocalDateTimeTypeInfoFactory` | `Types.LOCAL_DATE_TIME` |
| `java.time.LocalDate` | `LocalDateTypeInfoFactory` | `Types.LOCAL_DATE` |
| `java.time.LocalTime` | `LocalTimeTypeInfoFactory` | `Types.LOCAL_TIME` |
| `java.time.Duration` | `DurationTypeInfoFactory` | Custom (12-byte: long seconds + int nanos) |

### Collection Types (`io.github.sekelenao.flinkboot.core.api.typing.collection`)

| JDK Type | Flinkboot Factory Class | Under the hood Flink Type |
| :--- | :--- | :--- |
| `java.util.List<E>` | `ListTypeInfoFactory` | `Types.LIST(elementType)` |
| `java.util.Map<K, V>` | `MapTypeInfoFactory` | `Types.MAP(keyType, valueType)` |

---

## 2. Usage in POJO Classes

To use these factories, annotate your POJO fields using Flink's `@TypeInfo` annotation. Flinkboot's factories will automatically resolve generic parameters for collections:

```java
import io.github.sekelenao.flinkboot.core.api.typing.time.*;
import io.github.sekelenao.flinkboot.core.api.typing.collection.*;
import org.apache.flink.api.common.typeinfo.TypeInfo;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class UserSessionEvent {

    public String userId;

    // Forces Flink to use optimized InstantSerializer instead of Kryo
    @TypeInfo(InstantTypeInfoFactory.class)
    public Instant loginTimestamp;

    // Forces Flink to use optimized LocalDateTimeSerializer instead of Kryo
    @TypeInfo(LocalDateTimeTypeInfoFactory.class)
    public LocalDateTime lastActivityTime;

    // Forces Flink to use optimized custom 12-byte DurationSerializer instead of Kryo
    @TypeInfo(DurationTypeInfoFactory.class)
    public Duration sessionDuration;

    // Forces Flink to use optimized ListSerializer instead of Kryo
    @TypeInfo(ListTypeInfoFactory.class)
    public List<String> pageViews;

    // Forces Flink to use optimized MapSerializer instead of Kryo
    @TypeInfo(MapTypeInfoFactory.class)
    public Map<String, Integer> actionCounts;

    // Public zero-arg constructor required for Flink POJO
    public UserSessionEvent() {}
}
```

---

## 3. Why is this necessary?

### Kryo Fallback is a Performance Bottleneck
When Flink encounters a type it doesn't natively support in a POJO (like `java.time.Duration` or raw interfaces like `List`), it falls back to Kryo:
* Kryo writes class metadata into each record, drastically increasing serialization size.
* Kryo relies on Java reflection, which is slow.
* State backends (like RocksDB) will store serialized Kryo structures, causing state sizes to bloat.

### State Schema Evolution
If you change your class layout or update Flink versions, Kryo-serialized state is notoriously difficult to migrate and often leads to serialization incompatibility errors. Using native Flink serializers via `TypeInfoFactory` (like `Types.LIST`, `Types.MAP`, etc.) ensures safe state schema evolution.

---

## 4. Verification with Flinkboot Test

To verify that your POJOs are correctly configured and do not fall back to Kryo, write a compliance test using `FlinkbootTest.assertPojo()` from the `flinkboot-test` module:

```java
import io.github.sekelenao.flinkboot.test.api.FlinkbootTest;
import org.junit.jupiter.api.Test;

class UserSessionEventTest {

    @Test
    void testPojoCompliance() {
        // Will fail if any JDK time, duration, or collection field is missing its @TypeInfo annotation
        FlinkbootTest.assertPojo(UserSessionEvent.class);
    }
}
```
