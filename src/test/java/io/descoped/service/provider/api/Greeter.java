package io.descoped.service.provider.api;

import java.util.Map;

/** What the test providers create. */
public record Greeter(String greeting, Map<String, String> configuration) {
}
