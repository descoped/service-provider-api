# Releasing

Releases are made **only by the owner, or on his explicit word**, and go only to Maven Central. A SNAPSHOT never goes
to Central: the Portal limits monthly publishing, and the `central` profile refuses a SNAPSHOT version.

SNAPSHOTs go only to Descoped's AWS CodeArtifact repository, read publicly at `https://maven.descoped.io/`, for
development across repositories. CI's `snapshot` job publishes them after a green build:

- a push to master publishes the current `X.Y.Z-SNAPSHOT`;
- a run by hand on another branch (`gh workflow run ci.yml --ref <branch>`) publishes `X.Y.Z-<branch>-SNAPSHOT`, so
  it never replaces master's;
- a pull request or a tag publishes nothing.

The POM has no `<distributionManagement>`, so `deploy` without a target fails, and a release version can never go
to maven.descoped.io. The job names the target on its command line (`-DaltSnapshotDeploymentRepository`, SNAPSHOTs
only) and adds the sources. The publishing endpoint is in no file of this repository: CI assumes the role from the
organization secret `CODEARTIFACT_SNAPSHOTS_ROLE_ARN`, looks up the endpoint, and writes a `settings.xml` with the
token bound to the endpoint's origin; the account and the endpoint are masked in the log.

A release is two things, in this order:

1. **Maven Central**: `io.descoped.spi:service-provider-api` with sources, Javadoc and GPG signatures. Signed and
   uploaded from the owner's machine; CI never publishes to Central.
2. **The GitHub release** for the tag `v<version>`, for example `v3.0.2`, made once the version is on Central.

The tag is the version with a `v`, from 3.0.2 on. Earlier releases keep their tags: `3.0.0`, `3.0.1`, and
`service-provider-api-2.0.0`.

## Before a release

- `master` is green: CI, the JPMS smoke test and SonarCloud.
- `~/.m2/settings.xml` has the server `central` with the Central Portal token and
  `<repositoryOrigins>https://central.sonatype.com</repositoryOrigins>`. Maven 3.10 only sends a server's credentials
  to the hosts it knows for that id. The owner's `secrets` profile (`-P secrets`) loads the token.
- GPG signs with its default key, and the key's public part is on a key server Central checks
  (`keys.openpgp.org` or `keyserver.ubuntu.com`).

## The steps

### 1. Prepare: version, tag

```sh
./mvnw release:prepare
```

It asks for the release version (`3.0.2`), the tag (`v3.0.2`) and the next development version
(`3.0.3-SNAPSHOT`). It builds and tests, commits the release version, tags it, commits the next SNAPSHOT and pushes
both commits and the tag. The tag push runs CI on the tag.

### 2. Perform: sign and upload to Central

```sh
./mvnw release:perform -P secrets
```

It checks out the tag into `target/checkout` and runs `deploy -P central,sign`: the artifacts are signed with your
GPG key (gpg-agent asks for the passphrase) and the bundle is uploaded to the Central Portal. It is not published:
`autoPublish=false`, and the upload waits until the Portal has validated it. The log ends with the deployment id and
`has been validated`.

Prepare and perform can run in one command, in batch mode with the versions given:
`./mvnw -B release:prepare release:perform -P secrets -DreleaseVersion=3.0.2 -Dtag=v3.0.2
-DdevelopmentVersion=3.0.3-SNAPSHOT`.

Any JDK 17 or newer builds the release; the class files are Java 17 either way. The JDK's major version goes into
`Build-Jdk-Spec` in the jars' manifests, and reproducing the jars needs the same JDK (the same patch version if a hash
differs), so note `java -version` before the release: the GitHub release names it in full.

### 3. Publish on Central

In the Central Portal, under Deployments, check the deployment (the jar, sources, Javadoc, POM, their signatures and
checksums) and press Publish. Publishing is final: a version on Central can never be changed or removed. It is on
`repo1.maven.org` after it is synced, usually within half an hour.

### 4. Publish the GitHub release

Once the version is on `repo1.maven.org`:

```sh
gh release create v3.0.2 --verify-tag --title 3.0.2 --generate-notes --notes-start-tag 3.0.1
```

`--notes-start-tag` names the previous release's tag; from the second `v` release on, `--generate-notes` finds it by
itself. Add to the notes what changed for users and the JDK the release was built on, with its full version (from
`java -version`, for example `27.0.1`).

Then update the version in the README's Usage section, in a pull request.

## If something goes wrong

- **`release:prepare` fails before it pushes**: `./mvnw release:rollback`, fix, and prepare again.
- **`release:prepare` fails after it pushed the tag**: don't roll back. If nothing was uploaded to the Portal yet, fix
  forward: run `release:perform -P secrets` on the tag (`-Dtag=v<version>`), or, if the tag itself is wrong, delete
  it on GitHub and locally (`git push --delete origin v<version>`, `git tag -d v<version>`), revert the two release
  commits in a pull request and prepare again. Once a deployment for the version is published, the version is
  final: fix in the next one.
- **`release:perform` fails, or the Portal rejects the deployment**: drop the deployment in the Portal, if there is one,
  fix, and run `release:perform -P secrets` again. `release.properties` still points at the tag, so it builds the same
  tag; without it, name the tag: `-Dtag=v<version>`. The tag was tested by `release:prepare`, so a rerun can skip the
  tests: `-Darguments=-DskipTests`. Nothing is public until you press Publish.
- **The upload fails with `401`**: the token was not sent. Check the `central` server and its `repositoryOrigins` in
  `settings.xml`, that the run had `-P secrets`, and that the token is still valid.
