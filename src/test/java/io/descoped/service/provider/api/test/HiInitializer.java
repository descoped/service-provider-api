package io.descoped.service.provider.api.test;

import io.descoped.service.provider.api.ProviderName;

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
