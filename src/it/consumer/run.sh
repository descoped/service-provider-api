#!/usr/bin/env bash
# JPMS consumer smoke test, run after `mvn package`. Uses the built jar the way a modular application with drop-in
# providers does:
#
# - consumer.greeting        the service; declares `uses`, as the module that owns a service should
# - consumer.greeting.hello  two providers (`provides`; one through a static provider() method); no module requires it
# - greeting-auto.jar        a provider in a jar without module-info (an automatic module), from META-INF/services
# - consumer                 the application; requires only consumer.greeting, no `uses`
#
# Runs on the module path, where service binding resolves both provider modules; then with the application as an
# automatic module (a jar without module-info), started with --add-modules for the service module; then on the class
# path, where META-INF/services registers the providers instead.
set -euo pipefail

root=$(cd "$(dirname "$0")/../../.." && pwd)
it="$root/src/it/consumer"
out="$root/target/it-consumer"
cd "$root"

version=$(sed -n 's/^version=//p' target/maven-archiver/pom.properties)
jar="target/service-provider-api-$version.jar"

rm -rf "$out"
mkdir -p "$out/classes" "$out/auto"
javac --release 17 -Xlint:all -Werror --module-path "$jar" --module-source-path "$it/src" \
    -d "$out/classes" $(find "$it/src" -name '*.java')
cp -R "$it/src/consumer.greeting.hello/META-INF" "$out/classes/consumer.greeting.hello/"

javac --release 17 -Xlint:all -Werror --class-path "$jar:$out/classes/consumer.greeting" \
    -d "$out/auto" $(find "$it/auto" -name '*.java')
cp -R "$it/auto/META-INF" "$out/auto/"
auto="$out/greeting-auto.jar"
jar --create --file "$auto" -C "$out/auto" .

echo "== library module descriptor"
jar --describe-module --file "$jar"

echo "== jdeps: requires that are missing or unused"
jdeps --multi-release 17 --module-path "$jar" --check io.descoped.service.provider.api

echo "== automatic provider module"
jar --describe-module --file "$auto"

echo "== module path"
java --module-path "$jar:$out/classes:$auto" --module consumer/consumer.Main module

echo "== module path, the application as an automatic module"
app="$out/consumer-app.jar"
jar --create --file "$app" -C "$out/classes/consumer" consumer
java --module-path "$jar:$out/classes/consumer.greeting:$out/classes/consumer.greeting.hello:$auto:$app" \
    --add-modules consumer.greeting --module consumer.app/consumer.Main automatic

echo "== class path"
java --class-path "$jar:$out/classes/consumer.greeting:$out/classes/consumer.greeting.hello:$out/classes/consumer:$auto" \
    consumer.Main classpath
