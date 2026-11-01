<p align="center">
  <a href="https://flinkboot.com">
    <img src=".github/assets/icon.svg" width="200" alt="Flinkboot logo">
  </a>
</p>

<h1 align="center">
  <a href="https://flinkboot.com">Flinkboot</a>
</h1>

<p align="center">
  <strong>The Bootstrapping & Reliability Framework for Apache Flink.</strong>
</p>

[![Java](https://img.shields.io/badge/Java_11%2B-%23ED8B00.svg?logo=openjdk&logoColor=white)](https://docs.oracle.com/en/java/javase/11/docs/api/index.html)
[![Flink](https://img.shields.io/badge/Flink_1.20-%236f42c1.svg?logo=apacheflink&logoColor=white)](https://flink.apache.org/)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.sekelenao/flinkboot-core?label=Maven%20central&logo=apachemaven&logoColor=white&color=007ec6&labelColor=007ec6)](https://central.sonatype.com/artifact/io.github.sekelenao/flinkboot-core)
![Tests](https://raw.githubusercontent.com/Sekelenao/Flinkboot/badges/Tests.svg)
![Coverage](https://raw.githubusercontent.com/Sekelenao/Flinkboot/badges/Coverage.svg)
![Branches](https://raw.githubusercontent.com/Sekelenao/Flinkboot/badges/Branches.svg)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=Sekelenao_Flinkboot&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=Sekelenao_Flinkboot)

Flinkboot eliminates operational boilerplate and prevents runtime failures before stream pipelines reach TaskManagers:

- **Fail-Fast Configuration**: Validates parameters at launch (Jakarta Validation, YAML, environment variables) so misconfigured jobs never hit the cluster.
- **Zero-Boilerplate Runtime**: Bootstraps pre-tuned `StreamExecutionEnvironment` instances (RocksDB, checkpoints, restart strategies, local Web UI) in a single call.
- **Zero-Kryo Assertions**: Enforces POJO compliance in unit tests (`assertThat(Event.class).isPojo()`) to eliminate silent fallbacks to slow Kryo serialization.

Learn more on **[flinkboot.com](https://flinkboot.com)**

---

## Quick Example

Declare your runtime environment in YAML:

```yaml
# job-configuration.yaml
name: "StreamJob"
environment:
  execution:
    parallelism: 2
  checkpointing:
    interval: "PT10S"
    mode: "EXACTLY_ONCE"
  local-web-ui:
    enabled: true
    port: 8080
```

Bootstrap and run your pipeline:

```java
public class MyFlinkJob {
    public static void main(String[] args) throws Exception {
        Flinkboot boot = Flinkboot.initialize(args);
        JobProperties job = boot.configuration(JobProperties.class);

        StreamExecutionEnvironment env = boot.executionEnvironment(job);

        env.fromElements("Hello", "Flinkboot").print();
        env.execute(job.name());
    }
}
```

---

## Documentation & References

* **[Documentation](https://flinkboot.com)** — Official documentation and production guides.
* **[Quickstart Template](https://github.com/Sekelenao/Flinkboot-Quickstart)** — Standalone runnable project with simulated streams and live Web UI.
* **[Contributing Guide](CONTRIBUTING.md)** — Guidelines for reporting issues, submitting pull requests, and coding standards.
* **[Changelog](CHANGELOG.md)** — Release notes and user-facing change history.

---

## Contributors

Every bug fix, test addition, and architectural improvement from the community makes Flinkboot more reliable for everyone running Flink in production.

Sincere appreciation to all contributors who have shaped and strengthened this codebase.

<!-- CONTRIBUTORS-START -->
<p align="left">
  <a href="https://github.com/Sekelenao"><img src="https://github.com/Sekelenao.png?size=64" width="64" height="64" alt="Sekelenao" title="Sekelenao (72 merged PRs)" style="border-radius: 50%; margin: 2px;" /></a>
  <a href="https://github.com/LouisDeconinck"><img src="https://github.com/LouisDeconinck.png?size=64" width="64" height="64" alt="LouisDeconinck" title="LouisDeconinck (8 merged PRs)" style="border-radius: 50%; margin: 2px;" /></a>
  <a href="https://github.com/Aaqibhafeezkhan"><img src="https://github.com/Aaqibhafeezkhan.png?size=64" width="64" height="64" alt="Aaqibhafeezkhan" title="Aaqibhafeezkhan (5 merged PRs)" style="border-radius: 50%; margin: 2px;" /></a>
  <a href="https://github.com/be-student"><img src="https://github.com/be-student.png?size=64" width="64" height="64" alt="be-student" title="be-student (4 merged PRs)" style="border-radius: 50%; margin: 2px;" /></a>
  <a href="https://github.com/bkalika"><img src="https://github.com/bkalika.png?size=64" width="64" height="64" alt="bkalika" title="bkalika (4 merged PRs)" style="border-radius: 50%; margin: 2px;" /></a>
  <a href="https://github.com/kasapdev"><img src="https://github.com/kasapdev.png?size=64" width="64" height="64" alt="kasapdev" title="kasapdev (3 merged PRs)" style="border-radius: 50%; margin: 2px;" /></a>
  <a href="https://github.com/Renan-Bacheschi"><img src="https://github.com/Renan-Bacheschi.png?size=64" width="64" height="64" alt="Renan-Bacheschi" title="Renan-Bacheschi (3 merged PRs)" style="border-radius: 50%; margin: 2px;" /></a>
  <a href="https://github.com/timothytkim"><img src="https://github.com/timothytkim.png?size=64" width="64" height="64" alt="timothytkim" title="timothytkim (3 merged PRs)" style="border-radius: 50%; margin: 2px;" /></a>
  <a href="https://github.com/fhenrique77"><img src="https://github.com/fhenrique77.png?size=64" width="64" height="64" alt="fhenrique77" title="fhenrique77 (2 merged PRs)" style="border-radius: 50%; margin: 2px;" /></a>
  <a href="https://github.com/FrodyGr"><img src="https://github.com/FrodyGr.png?size=64" width="64" height="64" alt="FrodyGr" title="FrodyGr (2 merged PRs)" style="border-radius: 50%; margin: 2px;" /></a>
  <a href="https://github.com/Pallavi-p-h"><img src="https://github.com/Pallavi-p-h.png?size=64" width="64" height="64" alt="Pallavi-p-h" title="Pallavi-p-h (2 merged PRs)" style="border-radius: 50%; margin: 2px;" /></a>
  <a href="https://github.com/PHJ2000"><img src="https://github.com/PHJ2000.png?size=64" width="64" height="64" alt="PHJ2000" title="PHJ2000 (2 merged PRs)" style="border-radius: 50%; margin: 2px;" /></a>
</p>
<!-- CONTRIBUTORS-END -->

---

*Apache®, Apache Flink®, Apache Kafka®, and Apache Fluss™ are trademarks of the Apache Software Foundation. Flinkboot is an independent open-source project and is not affiliated with, endorsed by, or sponsored by the Apache Software Foundation.*

