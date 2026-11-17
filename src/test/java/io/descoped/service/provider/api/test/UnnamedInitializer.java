package io.descoped.service.provider.api.test;

import io.descoped.service.provider.api.ProviderInitializer;
import io.descoped.service.provider.api.ProviderName;

/** Test service with a provider that has no @ProviderName. */
public interface UnnamedInitializer extends ProviderInitializer<String> {

    final class Anonymous implements UnnamedInitializer {
        public Anonymous() {
            // ServiceLoader needs a public no-argument constructor
        }

        @Override
        public String initialize(java.util.Map<String, String> configuration) {
            return "anonymous";
        }
    }
}
