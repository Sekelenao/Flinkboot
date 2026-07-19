# How to Avoid Classpath and Dependency Conflicts in Production

When deploying Apache Flink jobs to a production cluster, classpath conflicts (especially with **Jackson** and **Log4j**) are one of the most common causes of silent startup crashes. 

Flink uses a `child-first` classloader strategy by default, meaning it attempts to load classes from the user's fat JAR before falling back to Flink's parent classloader. If the user's JAR contains overlapping or incompatible versions of libraries already provided by Flink, it can lead to `LinkageError`, `NoSuchMethodError`, or class casting exceptions.

Flinkboot is designed as a **good classpath citizen**—it declares Flink as `provided` to avoid polluting your cluster dependencies, but transitively bundles Jackson in `compile` scope to simplify your packaging. However, you must still configure your job's packaging correctly.

---

## Best Practice 1: Mark Cluster Dependencies as `provided`

Any dependency that is already provided in the Flink cluster's `lib/` directory **must** be marked with `<scope>provided</scope>` in your root `pom.xml`. 

This includes:
* **Flink Core APIs**: `flink-streaming-java`, `flink-clients`, `flink-runtime`, etc.
* **Flink Connectors** (if installed on the cluster): `flink-connector-kafka`, `flink-connector-files`, etc.

```xml
<dependencies>
    <!-- Flink Core (Provided by the cluster) -->
    <dependency>
        <groupId>org.apache.flink</groupId>
        <artifactId>flink-streaming-java</artifactId>
        <version>${flink.version}</version>
        <scope>provided</scope>
    </dependency>

    <!-- Flinkboot (Must be bundled inside your JAR) -->
    <dependency>
        <groupId>io.github.sekelenao</groupId>
        <artifactId>flinkboot-kafka</artifactId>
        <version>${flinkboot.version}</version>
    </dependency>
</dependencies>
```

---

## Best Practice 2: Shading and Relocating Jackson

Flink's runtime internally uses Jackson and bundles its own shaded versions. Because Flinkboot transitively includes Jackson in `compile` scope, standard Jackson will automatically be packaged into your fat JAR. To prevent conflicts with the Flink runtime version, you must relocate (shade) it.

The safest solution is to **relocate (shade)** Jackson classes into a unique namespace inside your fat JAR using the `maven-shade-plugin`.

### Recommended Maven Shade Configuration

Add the following configuration to your application's `pom.xml` build section:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version>3.5.1</version>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>shade</goal>
                    </goals>
                    <configuration>
                        <createDependencyReducedPom>false</createDependencyReducedPom>
                        <shadedArtifactAttached>false</shadedArtifactAttached>
                        
                        <!-- Relocate Jackson to prevent version clashes with Flink runtime -->
                        <!-- You can customize the shadedPattern namespace to match your project's packaging if preferred -->
                        <relocations>
                            <relocation>
                                <pattern>com.fasterxml.jackson</pattern>
                                <shadedPattern>io.github.sekelenao.flinkboot.shaded.jackson</shadedPattern>
                            </relocation>
                        </relocations>
                        
                        <!-- Exclude signatures to prevent SecurityException -->
                        <filters>
                            <filter>
                                <artifact>*:*</artifact>
                                <excludes>
                                    <exclude>META-INF/*.SF</exclude>
                                    <exclude>META-INF/*.DSA</exclude>
                                    <exclude>META-INF/*.RSA</exclude>
                                </excludes>
                            </filter>
                        </filters>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

---

## Best Practice 3: Handle Logging Correctly (Log4j)

Flink's runtime comes with an integrated Log4j setup. Bundling log4j configuration files or log4j implementations (like `log4j-core` or log4j binding bridges) in your fat JAR can corrupt Flink's logging or cause runtime initialization crashes.

### Guidelines for Logging:
1. **Never bundle logging implementations** (such as `log4j-core`, `logback-classic`) in your fat JAR. Always set their scope to `provided` or `test`.
2. **Only depend on the SLF4J API** in your code:
   ```xml
   <dependency>
       <groupId>org.slf4j</groupId>
       <artifactId>slf4j-api</artifactId>
       <version>1.7.36</version>
       <scope>provided</scope>
   </dependency>
   ```
3. Let the Flink cluster runtime bind SLF4J to its own Log4j implementation.
