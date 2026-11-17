package consumer.greeting.hello;

import consumer.greeting.GreeterInitializer;
import io.descoped.service.provider.api.ProviderName;

import java.util.Map;
import java.util.Set;

/** A provider with a public constructor, on the module path and the class path. */
@ProviderName("hello")
public final class HelloInitializer implements GreeterInitializer {

    /** Creates the provider. */
    public HelloInitializer() {
    }

    @Override
    public Set<String> configurationKeys() {
        return Set.of("name");
    }

    @Override
    public String initialize(Map<String, String> configuration) {
        return "Hello " + configuration.get("name");
    }
}
