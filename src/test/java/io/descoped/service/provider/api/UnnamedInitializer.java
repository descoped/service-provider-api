package io.descoped.service.provider.api;

/** Test service with a provider that has no @ProviderName. */
public interface UnnamedInitializer extends ProviderInitializer<String> {

    final class Anonymous implements UnnamedInitializer {
        public Anonymous() {
        }

        @Override
        public String initialize(java.util.Map<String, String> configuration) {
            return "anonymous";
        }
    }
}
