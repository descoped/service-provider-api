# Service Provider API

![Build Status](https://img.shields.io/github/actions/workflow/status/descoped/service-provider-api/ci.yml?branch=master)
![Latest Tag](https://img.shields.io/github/v/tag/descoped/service-provider-api)
[![Maven Central](https://img.shields.io/badge/dynamic/xml?url=https%3A%2F%2Frepo1.maven.org%2Fmaven2%2Fio%2Fdescoped%2Fspi%2Fservice-provider-api%2Fmaven-metadata.xml&query=%2F%2Frelease&label=maven%20central&color=blue)](https://central.sonatype.com/artifact/io.descoped.spi/service-provider-api)
![Renovate](https://img.shields.io/badge/renovate-enabled-brightgreen.svg)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=descoped_service-provider-api&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=descoped_service-provider-api)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=descoped_service-provider-api&metric=coverage)](https://sonarcloud.io/summary/new_code?id=descoped_service-provider-api)

Selects a `java.util.ServiceLoader` provider by name and initializes it from a configuration map. Providers are
registered the standard way: `provides` in `module-info.java`, or a `META-INF/services` file on the class path.

- Java 17 or newer; a named JPMS module, `io.descoped.service.provider.api`, that also works on the class path.
- No runtime dependencies, nothing logged.
- Only the chosen provider is instantiated: the name is read from the provider class through
  `ServiceLoader.Provider.type()`.
- Works with named modules, automatic modules and the class path, for the library, the service and the providers.

```xml
<dependency>
    <groupId>io.descoped.spi</groupId>
    <artifactId>service-provider-api</artifactId>
    <version>3.0.0</version>
</dependency>
```

## Usage

Declare a service that extends `ProviderInitializer`, and implement it once per provider:

```java
public interface StorageInitializer extends ProviderInitializer<Storage> {
}

@ProviderName("memory")
public final class MemoryStorageInitializer implements StorageInitializer {

    public MemoryStorageInitializer() {
    }

    @Override
    public Set<String> configurationKeys() {          // required keys; optional ones are not listed
        return Set.of("capacity");
    }

    @Override
    public Storage initialize(Map<String, String> configuration) {
        return new MemoryStorage(Integer.parseInt(configuration.get("capacity")));
    }
}
```

The module that defines the service declares `uses` for it. That is what makes the JVM resolve provider modules
that nothing `requires`, so providers can be dropped onto the module path:

```java
module my.storage {
    requires transitive io.descoped.service.provider.api;
    exports my.storage;
    uses my.storage.StorageInitializer;
}
```

Register each provider for the module path, the class path, or both:

```java
module my.storage.memory {
    requires transitive io.descoped.service.provider.api;
    provides my.storage.StorageInitializer with my.storage.memory.MemoryStorageInitializer;
}
```

```
# META-INF/services/my.storage.StorageInitializer
my.storage.memory.MemoryStorageInitializer
```

A jar without `module-info.java` needs only the `META-INF/services` file: on the module path it is an automatic module,
and the JDK reads that file as its `provides`.

Select it by name:

```java
Map<String, String> configuration = Map.of("storage.provider", "memory", "storage.capacity", "100");

Storage storage = ProviderConfigurator.configure(
        StorageInitializer.class,
        configuration.get("storage.provider"),
        ProviderConfigurator.subset(configuration, "storage."));   // {provider=memory, capacity=100}
```

The provider receives an unmodifiable copy of the configuration, with every required key and any others.

- **Constructor or `provider()` method.** A provider needs a public no-argument constructor. In a module with
  `provides` it may have a public static `provider()` method instead, as `ServiceLoader` allows; on the class path
  and in an automatic module it may not. The `@ProviderName` annotation goes on
  the class named in `provides` or in `META-INF/services`.
- **Your own `ServiceLoader`.** For providers in a particular class loader or module layer, pass a loader:
  `ProviderConfigurator.configure(ServiceLoader.load(layer, StorageInitializer.class), "memory", configuration)`. The
  module that creates the loader declares `uses`, as usual.

### Modules

| Your setup | What to do |
|---|---|
| The service module declares `uses` | Nothing. Provider modules and provider jars (automatic modules) on the module path are found without being required. |
| No module declares `uses` | Only provider modules (explicit or automatic) that some module `requires` are found. Add `uses` to the service module, or start with `--add-modules <provider module>`. |
| Your application is an automatic module (no `module-info.java`) | Start with `--add-modules <service module>` (or `ALL-MODULE-PATH`). An automatic module resolves the other automatic modules, provider jars included, but no explicit module: neither the service module nor, through its `requires transitive`, this library. Once added, the service module's `uses` binds the explicit provider modules. |
| Everything on the class path | Nothing. `META-INF/services` registers the providers. |

`configure(Class, ...)` calls `Module.addUses`, so this library needs no `uses` of its own for your service; that is
permission to load it, not resolution of provider modules.

### Errors

| Exception | When |
|---|---|
| `ProviderException` | No provider, or more than one, has the name. Or a provider of the service has no `@ProviderName`. The message lists the names that are available. |
| `IllegalArgumentException` | The name is blank, or the configuration lacks a key from `configurationKeys()`. |
| `NullPointerException` | An argument, or a key or value in the configuration, is `null`. |
| `ServiceConfigurationError` | `ServiceLoader` cannot load or instantiate a provider. |

## Migrating from 2.x

3.0.0 is a breaking release.

| 2.x | 3.0.0 |
|---|---|
| Java 11 | Java 17 |
| `configure(configuration, providerId, Class)` | `configure(Class, providerName, configuration)` |
| `configure(prefix, configuration, providerId, Class)` | `configure(Class, providerName, subset(configuration, prefix))` |
| `ProviderInitializer.providerId()`, checked against `@ProviderName` | Removed: `@ProviderName` alone names a provider |
| `configurationKeys()` must be implemented | A default method that returns an empty set |
| `initialize` gets the caller's map | It gets an unmodifiable copy. `null` keys or values are rejected |
| `RuntimeException` for a missing or ambiguous provider | `ProviderException` |
| A provider without `@ProviderName` throws `NullPointerException` | `ProviderException` naming the class |
| The module `opens` its package | No `opens` |
| Not available | `configure(ServiceLoader, ...)` |

## Snapshots

Merges to `master` are published as SNAPSHOTs to the Central Portal snapshot repository:

```xml
<repositories>
    <repository>
        <id>central-portal-snapshots</id>
        <url>https://central.sonatype.com/repository/maven-snapshots/</url>
        <releases><enabled>false</enabled></releases>
        <snapshots><enabled>true</enabled></snapshots>
    </repository>
</repositories>
```

## Building

Build with JDK 17 or newer. `./mvnw` (the Maven Wrapper) downloads the pinned Maven, 3.10.1, and checks its SHA-256;
a Maven of your own works too if it is 3.6.3 or newer (not Maven 4 yet).

Builds are reproducible from the next release on: the jars from a release tag are byte for byte the ones on Maven
Central. Build with the same major JDK as the release (`Build-Jdk-Spec` in the jar's manifest; if a hash differs, try
the same patch version) from an LF checkout (`.gitattributes` enforces it):
`git checkout <tag> && ./mvnw -P central package -DskipTests`, then compare the SHA-256 of `target/*.jar` (main, sources
and javadoc) with the `.sha256` files on repo1.maven.org. The `.asc` signatures are not reproducible; check them with
`gpg --verify` instead.

```sh
./mvnw verify                 # compile with -Xlint:all -Werror, tests (JUnit 6)
./mvnw verify javadoc:javadoc # plus Javadoc with doclint; warnings fail the build
src/it/consumer/run.sh        # after package: named modules use the jar, on the module and class path
./mvnw verify -P coverage     # JaCoCo report in target/site/jacoco
```

Licensed under the [Apache License, Version 2.0](LICENSE).
