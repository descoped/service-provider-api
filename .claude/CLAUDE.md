# CLAUDE.md

Guidance for Claude Code in this repository.

## What this is

`io.descoped.spi:service-provider-api`, JPMS module `io.descoped.service.provider.api`: selects a
`java.util.ServiceLoader` provider by the name in its `@ProviderName` annotation and initializes it from a
`Map<String, String>`. Owned by Descoped. `ProviderConfigurator` does the work, with `ProviderInitializer` (the
service supertype), `@ProviderName` and `ProviderException`. `README.md` is the user documentation and must stay true
when behaviour changes.

## Invariants (never violate)

- **Java 17 baseline** (`maven.compiler.release` 17): every class file is Java 17 whatever JDK builds it. CI builds
  and tests on JDK 17 only, in one job (`ci.yml`). Nothing above Java 17 in main, test or `src/it` code.
- **JPMS first.** Exported: `io.descoped.service.provider.api` only, no `opens`. The module requires nothing beyond
  `java.base` (`jdeps --check` in the smoke test).
- **Java service-loading standards.** Providers are found with `java.util.ServiceLoader` only: `provides` on the module
  path, `META-INF/services` on the class path. No class-path scanning, no reflection beyond reading the annotation
  from `ServiceLoader.Provider.type()`. Only the chosen provider is instantiated. `configure(Class, ...)` declares the
  use with `Module.addUses`, which lets the library load the service but does not resolve modules: provider modules
  that nothing `requires` are resolved only through a `uses` in some module, normally the one that defines the
  service. Documentation must not say otherwise. `configure(ServiceLoader, ...)` leaves the loader to the caller.
  Providers in automatic modules (`META-INF/services`) are supported and need a public no-argument constructor.
- **No runtime dependencies** and no logging. Adding a dependency is the owner's decision.
- **Always the latest stable version** of every library, plugin and action. Pre-releases only on request. Old and
  outdated code and configuration is removed, not carried along.
- **Publishing.** Merges to `master` publish a SNAPSHOT to the Central Portal snapshot repository (`ci.yml`, step
  "Publish SNAPSHOT") with the repository secrets `CENTRAL_TOKEN_USER` / `CENTRAL_TOKEN_PASSWORD`; the step fails when
  they are missing. Releases go through the Central Portal (profiles `central,sign`), only with the owner's approval.
  CI never publishes a release. Maven 3.10 sends a server's credentials only to the hosts it knows for that id, and
  the id `central` is bound to Maven Central, so the server `central` must list `https://central.sonatype.com` in
  `repositoryOrigins`: in `.github/maven-settings.xml` for CI, and in `~/.m2/settings.xml` for deploys by hand.

## Testing

- Unit tests (JUnit 6) are a module of their own, `io.descoped.service.provider.api.test`
  (`src/test/java/module-info.java`), in package `io.descoped.service.provider.api.test`. It requires the library
  and declares `uses` and `provides` for its test services, as an application module does, so surefire and the IDE
  run the tests on the module path and they reach only the exported API. It is `open` for JUnit only. Never put tests
  in the library's package (a split package) or register test providers in `META-INF/services` (a named module
  ignores it).
- `src/it/consumer/run.sh` covers the module path: `consumer.greeting` defines the service and declares `uses`;
  `consumer.greeting.hello` provides it (one provider through a static `provider()` method) and no module requires
  it; `greeting-auto.jar` is an automatic module with a provider in `META-INF/services`; `consumer` requires only
  `consumer.greeting` and has no `uses`. It runs again with the application as an automatic module (started with
  `--add-modules consumer.greeting`), then on the class path.

## Maven Wrapper

- `./mvnw` pins Maven (`.mvn/wrapper/maven-wrapper.properties`, type `only-script`, so no jar in the repository) and
  checks the download against `distributionSha256Sum`. CI runs `./mvnw`. When Maven is bumped, by Renovate or by hand,
  set `distributionSha256Sum` to the SHA-256 of the new `apache-maven-<version>-bin.zip`, after checking that zip
  against the `.sha512` Maven publishes next to it. A stale checksum fails the build, it never skips the check.

## Commands

```sh
./mvnw verify                    # compile with -Xlint:all -Werror, tests (JUnit 6)
./mvnw verify javadoc:javadoc    # plus Javadoc with doclint, warnings fail the build
src/it/consumer/run.sh           # after package: named modules use the jar, module path and class path
./mvnw verify -P coverage        # JaCoCo report in target/site/jacoco
./mvnw deploy -P central,secrets # SNAPSHOT to the Central Portal by hand (the token comes from ~/.mavenrc;
                                 # ~/.m2/settings.xml lists https://central.sonatype.com in repositoryOrigins)
```
