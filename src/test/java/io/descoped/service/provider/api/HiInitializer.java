package io.descoped.service.provider.api;

import java.util.Map;

/** Requires no keys. */
@ProviderName("hi")
public final class HiInitializer implements GreeterInitializer {

    static volatile int instances;

    public HiInitializer() {
        instances++;
    }

    @Override
    public Greeter initialize(Map<String, String> configuration) {
        return new Greeter("Hi", configuration);
    }
}
