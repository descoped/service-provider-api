package io.descoped.service.provider.api;

import java.util.Map;
import java.util.Set;

@ProviderName("hello")
public final class HelloInitializer implements GreeterInitializer {

    static volatile int instances;

    public HelloInitializer() {
        instances++;
    }

    @Override
    public Set<String> configurationKeys() {
        return Set.of("name", "punctuation");
    }

    @Override
    public Greeter initialize(Map<String, String> configuration) {
        return new Greeter("Hello " + configuration.get("name") + configuration.get("punctuation"), configuration);
    }
}
