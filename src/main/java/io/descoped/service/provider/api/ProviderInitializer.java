package io.descoped.service.provider.api;

import java.util.Map;
import java.util.Set;

/**
 * A service, in the {@link java.util.ServiceLoader} sense, that creates a configured instance of {@code T}.
 *
 * <p>Declare a service interface that extends this one, and register each implementation as a provider of it, with
 * {@code provides} in {@code module-info.java} for the module path and a {@code META-INF/services} file for the class
 * path or an automatic module. Annotate each implementation with {@link ProviderName}. The module that defines the
 * service interface declares {@code uses} for it, so that provider modules no other module requires are resolved
 * when the application starts. A provider needs a public no-argument constructor; in a
 * module with {@code provides} it may have a public static {@code provider()} method instead, as {@link java.util.ServiceLoader}
 * describes. The annotation then still goes on the class named in {@code provides}.
 *
 * <pre>{@code
 * public interface StorageInitializer extends ProviderInitializer<Storage> {
 * }
 *
 * @ProviderName("memory")
 * public final class MemoryStorageInitializer implements StorageInitializer {
 *     public Set<String> configurationKeys() {
 *         return Set.of("capacity");
 *     }
 *
 *     public Storage initialize(Map<String, String> configuration) {
 *         return new MemoryStorage(Integer.parseInt(configuration.get("capacity")));
 *     }
 * }
 * }</pre>
 *
 * @param <T> the type this provider creates
 */
public interface ProviderInitializer<T> {

    /**
     * The configuration keys this provider requires. {@link ProviderConfigurator} checks that each is present before
     * it calls {@link #initialize(Map)}. Optional keys are not listed.
     *
     * @return the required keys; empty by default
     */
    default Set<String> configurationKeys() {
        return Set.of();
    }

    /**
     * Creates the configured instance.
     *
     * @param configuration an unmodifiable map that holds every required key, and any optional ones
     * @return the instance
     */
    T initialize(Map<String, String> configuration);
}
