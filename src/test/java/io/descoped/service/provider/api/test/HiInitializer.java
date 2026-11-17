package io.descoped.service.provider.api.test;

import io.descoped.service.provider.api.ProviderName;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/** Requires no keys. */
@ProviderName("hi")
public final class HiInitializer implements GreeterInitializer {

    /** How many instances were created; atomic, because providers are created concurrently. */
    static final AtomicInteger INSTANCES = new AtomicInteger();

    public HiInitializer() {
        INSTANCES.incrementAndGet();
    }

    @Override
    public Greeter initialize(Map<String, String> configuration) {
        return new Greeter("Hi", configuration);
    }
}
