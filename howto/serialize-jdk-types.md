# How to Serialize JDK Types (Time, Duration, Collections)

In Apache Flink, POJO fields using `LocalDateTime`, `LocalDate`, `LocalTime`, `Duration`, `List`, or `Map` default to **Kryo serialization** (which is slower, less space-efficient, and risky for state evolution).

Flinkboot provides optimized `TypeInfoFactory` classes to enable native Flink serialization for these types.

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

## 2. Usage in POJO Classes

Annotate your POJO fields with Flink's `@TypeInfo` annotation:

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

## 3. POJO Compliance Validation

To ensure your POJOs never fall back to Kryo serialization, validate them in your unit tests with `FlinkbootTest.assertPojo()`:

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
