# How to Avoid Classpath and Dependency Conflicts in Production

When deploying Apache Flink jobs to a production cluster, classpath conflicts (especially with **Jackson** and **Log4j**) are one of the most common causes of silent startup crashes. 

Flink uses a `child-first` classloader strategy by default, meaning it attempts to load classes from the user's fat JAR before falling back to Flink's parent classloader. If the user's JAR contains overlapping or incompatible versions of libraries already provided by Flink, it can lead to `LinkageError`, `NoSuchMethodError`, or class casting exceptions.

Flinkboot is designed as a **good classpath citizen**—it declares Flink as `provided` to avoid polluting your cluster dependencies, but transitively bundles Jackson in `compile` scope to simplify your packaging. However, you must still configure your job's packaging correctly.

---

## Best Practice 1: Import Flinkboot BOM and Mark Cluster Dependencies as `provided`

Any dependency that is already provided in the Flink cluster's `lib/` directory (like Flink Core APIs and Connectors) must not be packaged inside your Fat JAR.

By importing `flinkboot` in your `<dependencyManagement>` section (BOM pattern), you get managed versions **and** automatic `provided` scopes for all Flink dependencies. You don't need to specify `<version>` or `<scope>` manually for Flink components!

> [!NOTE]
> For the complete table of all managed dependencies, versions, and pre-configured scopes, see [Flinkboot BOM & Managed Dependencies](bom-managed-dependencies.md).

```xml
<dependencyManagement>
    <dependencies>
        <!-- Flinkboot BOM -->
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
    <!-- Flink Core (Provided by cluster - version & scope provided managed by BOM) -->
    <dependency>
        <groupId>org.apache.flink</groupId>
        <artifactId>flink-streaming-java</artifactId>
    </dependency>

    <!-- Flink Clients (Required for running/debugging jobs locally in IDE) -->
    <dependency>
        <groupId>org.apache.flink</groupId>
        <artifactId>flink-clients</artifactId>
    </dependency>

    <!-- Flinkboot Core (Bundled inside your Fat JAR) -->
    <dependency>
        <groupId>io.github.sekelenao</groupId>
        <artifactId>flinkboot-core</artifactId>
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
            <version>${maven-shade-plugin.version}</version>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>shade</goal>
                    </goals>
                    <configuration>
                        <createDependencyReducedPom>false</createDependencyReducedPom>
                        <shadedArtifactAttached>false</shadedArtifactAttached>
                        
                        <!-- Relocate Jackson & com.fasterxml to prevent version clashes with Flink runtime -->
                        <relocations>
                            <relocation>
                                <pattern>com.fasterxml</pattern>
                                <shadedPattern>io.github.sekelenao.flinkboot.shaded.fasterxml</shadedPattern>
                            </relocation>
                        </relocations>
                        
                        <!-- Exclude signatures, module descriptors, and MRJAR entries -->
                        <filters>
                            <filter>
                                <artifact>*:*</artifact>
                                <excludes>
                                    <exclude>META-INF/*.SF</exclude>
                                    <exclude>META-INF/*.DSA</exclude>
                                    <exclude>META-INF/*.RSA</exclude>
                                    <exclude>module-info.class</exclude>
                                    <exclude>META-INF/versions/**</exclude>
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

> [!TIP]
> For a complete, production-tested `pom.xml` build configuration including shade relocations, filters, and Main-Class entry, refer to the [Flinkboot Quickstart repository](https://github.com/Sekelenao/Flinkboot-Quickstart).

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
   </dependency>
   ```
3. Let the Flink cluster runtime bind SLF4J to its own Log4j implementation.
