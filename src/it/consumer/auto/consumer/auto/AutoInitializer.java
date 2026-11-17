package consumer.auto;

import consumer.greeting.GreeterInitializer;
import io.descoped.service.provider.api.ProviderName;

import java.util.Map;

/** A provider in a jar without module-info: an automatic module on the module path, registered by META-INF/services. */
@ProviderName("auto")
public final class AutoInitializer implements GreeterInitializer {

    /** Creates the provider; an automatic module needs a public no-argument constructor. */
    public AutoInitializer() {
    }

    @Override
    public String initialize(Map<String, String> configuration) {
        return "from an automatic module";
    }
}
