package io.descoped.service.provider.api.test;

import io.descoped.service.provider.api.ProviderName;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@ProviderName("hello")
public final class HelloInitializer implements GreeterInitializer {

    /** How many instances were created; atomic, because providers are created concurrently. */
    static final AtomicInteger INSTANCES = new AtomicInteger();

    public HelloInitializer() {
        INSTANCES.incrementAndGet();
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
