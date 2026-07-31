# How to Assert Flink POJO Compliance

Flinkboot provides a test utility to ensure your data classes comply with Apache Flink's strict POJO serialization requirements.

---

## 1. What is Flink POJO Compliance?

Apache Flink uses an optimized serializer (`PojoSerializer`) for data serialization within pipelines. When Flink recognizes a class as a valid POJO, it can perform direct field access and serialize/deserialize elements much faster than falling back to general-purpose serializers like **Kryo**.

If your class is **not** recognized as a Flink POJO:
* Flink will fall back to Kryo serialization (which is significantly slower and less space-efficient).
* Flink will not be able to use key-selection on nested fields (e.g. `keyBy("fieldName")`).

### Flink's POJO Requirements:
To be recognized as a POJO by Flink's `TypeExtractor`, a class must meet the following criteria:
1. The class must be **public** and standalone (or a `public static` nested class, not an inner class).
2. It must have a **public zero-argument constructor** (default constructor).
3. All fields must be either:
   * **public** (non-final), or
   * have **public getter and setter** methods following the JavaBean naming convention (e.g. `getField()` and `setField(...)`).
4. **No Generic/Kryo Fallback Fields**: No fields (or nested fields) may fall back to `GenericTypeInfo` (Kryo fallback serialization). `FlinkbootTest.assertPojo()` inspects fields to ensure pure native Flink POJO serialization.

---

## 2. Maven Dependencies

To write compliance tests, import the Flinkboot BOM in your `<dependencyManagement>` and add `flinkboot-test` alongside Flink APIs in your `pom.xml`:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.sekelenao</groupId>
            <artifactId>flinkboot</artifactId>
            <version>0.1.0-1.20</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <!-- Flinkboot Test Utility -->
    <dependency>
        <groupId>io.github.sekelenao</groupId>
        <artifactId>flinkboot-test</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- Flink Streaming API (Provided by Flink cluster) -->
    <dependency>
        <groupId>org.apache.flink</groupId>
        <artifactId>flink-streaming-java</artifactId>
    </dependency>
</dependencies>
```

---

## 3. Usage in JUnit 5

Use `FlinkbootTest.assertPojo(Class<?> clazz)` to verify your model classes:

### Example Data Class

```java
public class UserActivity {
    private String userId;
    private long timestamp;

    // Public zero-argument constructor (Required)
    public UserActivity() {}

    public UserActivity(String userId, long timestamp) {
        this.userId = userId;
        this.timestamp = timestamp;
    }

    // Public Getters and Setters (Required)
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
```

### Writing the Test

Create a test class in your `src/test/java` directory:

```java
import io.github.sekelenao.flinkboot.test.api.FlinkbootTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserActivityTest {

    @Test
    @DisplayName("UserActivity should comply with Flink POJO serialization rules")
    void testPojoCompliance() {
        FlinkbootTest.assertPojo(UserActivity.class);
    }
}
```

If the class violates any of Flink's requirements or contains fields falling back to Kryo serialization, the assertion fails immediately with a descriptive error message indicating the exact path of the invalid field (e.g., `UserActivity.timestamp`).

---

## 4. Special Field Types (`java.time.*` and `Optional`)

### Java 8 Date/Time Types (`LocalDateTime`, `OffsetDateTime`, etc.)
By default, Flink's `TypeExtractor` treats unannotated `java.time.*` fields as `GenericTypeInfo` (Kryo fallback). To ensure strict POJO compliance with `assertPojo()`:
- Annotate the field with Flink's `@TypeInfo` providing a custom `TypeInfoFactory`:
  ```java
  public class CustomEvent {
      @TypeInfo(LocalDateTimeTypeInfoFactory.class)
      public LocalDateTime eventTime;
  }
  ```
- Or use natively supported types like `Instant`, `java.sql.Timestamp`, or `long` epoch milliseconds.

### `java.util.Optional`
`java.util.Optional` lacks a public zero-argument constructor and is not a Flink POJO. Fields typed as `Optional<T>` fall back to Kryo and will cause `assertPojo()` to fail. Prefer nullable fields or Flink's `@Nullable` annotation on POJO fields.

