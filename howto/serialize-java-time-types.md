# How to Serialize JDK Time & Math Types (`java.time.*`, `java.math.*`)
 
Apache Flink does not register default `TypeInfoFactory` mappings for modern Java Date/Time classes (`Instant`, `LocalDateTime`, `LocalDate`, `LocalTime`) and Math types (`BigDecimal`, `BigInteger`). Without custom factories, Flink's `TypeExtractor` falls back to **Kryo serialization**, which is slow, space-inefficient, and prone to state migration issues in production.

Flinkboot provides built-in, optimized `TypeInfoFactory` classes to enable native Flink serialization for these types.

---

## 1. Available Factories

The factories are organized into subpackages under `io.github.sekelenao.flinkboot.core.api.typing`:

### Time Types (`io.github.sekelenao.flinkboot.core.api.typing.time`)

| Java 8 Type | Flinkboot Factory Class | Under the hood Flink Type |
| :--- | :--- | :--- |
| `java.time.Instant` | `InstantTypeInfoFactory` | `Types.INSTANT` |
| `java.time.LocalDateTime` | `LocalDateTimeTypeInfoFactory` | `Types.LOCAL_DATE_TIME` |
| `java.time.LocalDate` | `LocalDateTypeInfoFactory` | `Types.LOCAL_DATE` |
| `java.time.LocalTime` | `LocalTimeTypeInfoFactory` | `Types.LOCAL_TIME` |

### Math Types (`io.github.sekelenao.flinkboot.core.api.typing.math`)

| Java Type | Flinkboot Factory Class | Under the hood Flink Type |
| :--- | :--- | :--- |
| `java.math.BigDecimal` | `BigDecimalTypeInfoFactory` | `Types.BIG_DEC` |
| `java.math.BigInteger` | `BigIntegerTypeInfoFactory` | `Types.BIG_INT` |

---

## 2. Usage in POJO Classes

To use these factories, annotate your POJO fields using Flink's `@TypeInfo` annotation:

```java
import io.github.sekelenao.flinkboot.core.api.typing.time.InstantTypeInfoFactory;
import io.github.sekelenao.flinkboot.core.api.typing.time.LocalDateTypeInfoFactory;
import io.github.sekelenao.flinkboot.core.api.typing.time.LocalDateTimeTypeInfoFactory;
import io.github.sekelenao.flinkboot.core.api.typing.time.LocalTimeTypeInfoFactory;
import io.github.sekelenao.flinkboot.core.api.typing.math.BigDecimalTypeInfoFactory;
import io.github.sekelenao.flinkboot.core.api.typing.math.BigIntegerTypeInfoFactory;
import org.apache.flink.api.common.typeinfo.TypeInfo;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class TransactionEvent {

    public String transactionId;

    @TypeInfo(BigDecimalTypeInfoFactory.class)
    public BigDecimal amount;

    @TypeInfo(BigIntegerTypeInfoFactory.class)
    public BigInteger transactionCount;

    // Forces Flink to use optimized InstantSerializer instead of Kryo
    @TypeInfo(InstantTypeInfoFactory.class)
    public Instant timestamp;

    @TypeInfo(LocalDateTimeTypeInfoFactory.class)
    public LocalDateTime processingDateTime;

    @TypeInfo(LocalDateTypeInfoFactory.class)
    public LocalDate businessDate;

    @TypeInfo(LocalTimeTypeInfoFactory.class)
    public LocalTime systemTime;

    // Public zero-arg constructor required for Flink POJO
    public TransactionEvent() {}
}
```

---

## 3. Why is this necessary?

### Kryo Fallback is a Performance Bottleneck
When Flink encounters a type it doesn't natively support (like `java.time.LocalDateTime` in a POJO), it serializes it using Kryo:
* Kryo writes class metadata into each record, drastically increasing serialization size.
* Kryo relies on Java reflection, which is slow.
* State backends (like RocksDB) will store serialized Kryo structures, making state size larger.

### State Schema Evolution
If you change your class layout or update Flink versions, Kryo-serialized state is notoriously difficult to migrate and often leads to serialization incompatibility errors. Using native Flink serializers via `TypeInfoFactory` ensures safe state schema evolution.

---

## 4. Verification with Flinkboot Test

To verify that your POJOs are correctly configured and do not fall back to Kryo, write a compliance test using `FlinkbootTest.assertPojo()` from the `flinkboot-test` module:

```java
import io.github.sekelenao.flinkboot.test.api.FlinkbootTest;
import org.junit.jupiter.api.Test;

class TransactionEventTest {

    @Test
    void testPojoCompliance() {
        // Will fail if any java.time.* or java.math.* field is missing its @TypeInfo annotation
        FlinkbootTest.assertPojo(TransactionEvent.class);
    }
}
```
