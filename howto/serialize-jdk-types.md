# How to Serialize JDK Types (Time, Duration, Collections)

In Apache Flink, POJO fields using `LocalDateTime`, `LocalDate`, `LocalTime`, `Duration`, `List`, or `Map` default to **Kryo serialization** (which is slower, less space-efficient, and risky for state schema evolution).

Flinkboot provides built-in, optimized `TypeInfoFactory` classes and **automatically registers them globally at startup** to enable native Flink serialization without requiring boilerplate annotations.

---

## 1. Available Factories

| Field Type | Flinkboot Factory Class | Serializer / Underlying Type |
| :--- | :--- | :--- |
| `java.time.LocalDateTime` | `LocalDateTimeTypeInfoFactory` | `Types.LOCAL_DATE_TIME` |
| `java.time.LocalDate` | `LocalDateTypeInfoFactory` | `Types.LOCAL_DATE` |
| `java.time.LocalTime` | `LocalTimeTypeInfoFactory` | `Types.LOCAL_TIME` |
| `java.time.Duration` | `DurationTypeInfoFactory` | Custom 12-byte `DurationSerializer` |
| `java.util.List<E>` | `ListTypeInfoFactory` | `Types.LIST(elementType)` |
| `java.util.Map<K, V>` | `MapTypeInfoFactory` | `Types.MAP(keyType, valueType)` |

---

## 2. Automatic Registration (Default Behavior)

When you initialize Flinkboot via `Flinkboot.initialize(args)`, all default factories are automatically registered in Flink's `TypeExtractor`.

This means standard POJOs work out of the box **without any `@TypeInfo` annotations**:

```java
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class UserEvent {

    public String userId;
    public LocalDateTime eventTime;        // Automatically resolved natively
    public Duration duration;              // Automatically resolved natively
    public List<Duration> sessionDurations; // Automatically resolved natively (even nested!)
    public Map<String, Integer> metrics;   // Automatically resolved natively

    public UserEvent() {}
}
```

---

## 3. Disabling Automatic Registration

If you want full explicit control or want to avoid global `TypeExtractor` registrations, you can disable automatic registration:

### Via Command Line
```bash
flink run MyJobJar.jar --flinkboot-disable-typeinfo-registration
```

### Via Environment Variable
```bash
export FLINKBOOT_DISABLE_TYPEINFO_REGISTRATION=true
flink run MyJobJar.jar
```

When disabled, you must explicitly annotate each POJO field with `@TypeInfo`:

```java
import io.github.sekelenao.flinkboot.core.api.typing.collection.ListTypeInfoFactory;
import io.github.sekelenao.flinkboot.core.api.typing.collection.MapTypeInfoFactory;
import io.github.sekelenao.flinkboot.core.api.typing.time.DurationTypeInfoFactory;
import io.github.sekelenao.flinkboot.core.api.typing.time.LocalDateTimeTypeInfoFactory;
import org.apache.flink.api.common.typeinfo.TypeInfo;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class UserEvent {

    public String userId;

    @TypeInfo(LocalDateTimeTypeInfoFactory.class)
    public LocalDateTime eventTime;

    @TypeInfo(DurationTypeInfoFactory.class)
    public Duration duration;

    @TypeInfo(ListTypeInfoFactory.class)
    public List<String> tags;

    @TypeInfo(MapTypeInfoFactory.class)
    public Map<String, Integer> metrics;

    public UserEvent() {}
}
```

---

## 4. Edge Cases and Nuances

### A. Concrete Implementations for `List` and `Map`
Flink's built-in `ListSerializer` and `MapSerializer` instantiate `java.util.ArrayList` and `java.util.HashMap` upon deserialization.
* If your POJO uses `java.util.List` or `java.util.Map`, it will be deserialized as an `ArrayList` or `HashMap`.
* If your application relies on specific implementations (such as `java.util.TreeMap` for sorted keys or immutable collections), you must use a custom serializer.

### B. Nested Generic Types when Autoregistration is Disabled
In Java and Flink, the `@TypeInfo` annotation cannot be placed directly on generic type arguments (e.g. `List<@TypeInfo(...) Duration>`).
* **With Autoregistration (Default)**: `List<Duration>` and `Map<String, LocalDate>` work automatically without Kryo because `Duration` is registered globally.
* **Without Autoregistration**: To serialize `List<Duration>` natively without Kryo, you must create a dedicated container factory (e.g., `DurationListTypeInfoFactory extends TypeInfoFactory<List<Duration>>`) and annotate the field with `@TypeInfo(DurationListTypeInfoFactory.class)`.

### C. Custom Domain Classes in Collections
If you have a collection of custom DTOs (e.g. `List<MyItem>`), annotate `MyItem` at the class level:
```java
@TypeInfo(MyItemTypeInfoFactory.class)
public class MyItem { ... }
```
Flink's `TypeExtractor` will automatically find the class-level annotation when resolving `List<MyItem>`.

### D. Nullability
Flink's native serializers for collections and time types properly support `null` values within POJO fields.

---

## 5. POJO Compliance Validation

To verify that your POJOs are properly configured and do not fall back to Kryo serialization, validate them in your tests using `FlinkbootTest.assertPojo()` from `flinkboot-test`:

```java
import io.github.sekelenao.flinkboot.test.api.FlinkbootTest;
import org.junit.jupiter.api.Test;

class UserEventTest {

    @Test
    void testPojoCompliance() {
        FlinkbootTest.assertPojo(UserEvent.class);
    }
}
```
