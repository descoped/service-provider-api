package consumer.greeting.hello;

import consumer.greeting.GreeterInitializer;
import io.descoped.service.provider.api.ProviderName;

import java.util.Map;

/** A provider created by a static provider() method, which only the module path supports. */
@ProviderName("method")
public final class MethodInitializer implements GreeterInitializer {

    private MethodInitializer() {
    }

    /**
     * Creates the provider.
     *
     * @return the provider
     */
    public static MethodInitializer provider() {
        return new MethodInitializer();
    }

    @Override
    public String initialize(Map<String, String> configuration) {
        return "from provider()";
    }
}
