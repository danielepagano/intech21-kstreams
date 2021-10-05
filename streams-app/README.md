## Prerequisites

- JDK 11.0.11
- Maven 3 (`choco install maven` on Windows)
  - If you use Windows, ensure your maven `.m2\settings.xml` (in your home directory) equals to or contains the
    following:

```xml

<settings xmlns="http://maven.apache.org/SETTINGS/1.2.0">
  <profiles>
    <profile>
      <id>os-properties</id>
      <properties>
        <os.detected.name>windows</os.detected.name>
        <os.detected.arch>x86_64</os.detected.arch>
        <os.detected.classifier>win64</os.detected.classifier>
      </properties>
    </profile>
  </profiles>

  <activeProfiles>
    <activeProfile>os-properties</activeProfile>
  </activeProfiles>
</settings>
```

## Running Locally

- Note that Maven generates sources from proto files, so you will need to `mvn compile` at least the first time 