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

---

## 2. Maven Dependency

To write compliance tests, import the Flinkboot Test utility in your `pom.xml` with `test` scope:

```xml
<dependency>
    <groupId>io.github.sekelenao</groupId>
    <artifactId>flinkboot-test</artifactId>
    <scope>test</scope>
</dependency>
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
import io.github.sekelenao.flinkboot.test.FlinkbootTest;
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

If the class violates any of Flink's requirements, the assertion fails immediately with a descriptive error message explaining what needs to be fixed.
