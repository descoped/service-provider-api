package io.descoped.service.provider.api.test;

import java.util.Map;

/** What the test providers create. */
public record Greeter(String greeting, Map<String, String> configuration) {
}
