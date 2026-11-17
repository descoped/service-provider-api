package io.descoped.service.provider.api;

/** Test service whose two providers share a name. */
public interface DuplicateInitializer extends ProviderInitializer<String> {

    @ProviderName("same")
    final class First implements DuplicateInitializer {
        public First() {
            // ServiceLoader needs a public no-argument constructor
        }

        @Override
        public String initialize(java.util.Map<String, String> configuration) {
            return "first";
        }
    }

    @ProviderName("same")
    final class Second implements DuplicateInitializer {
        public Second() {
            // ServiceLoader needs a public no-argument constructor
        }

        @Override
        public String initialize(java.util.Map<String, String> configuration) {
            return "second";
        }
    }
}
