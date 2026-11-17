package io.descoped.service.provider.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TreeSet;

/**
 * Selects the {@link ServiceLoader} provider with a given {@link ProviderName} and initializes it from a configuration
 * map.
 *
 * <pre>{@code
 * Storage storage = ProviderConfigurator.configure(StorageInitializer.class, "memory", Map.of("capacity", "100"));
 * }</pre>
 *
 * <p>A provider is chosen by the annotation on its class, through {@link ServiceLoader.Provider#type()}, so only the
 * chosen provider is instantiated. All methods are thread-safe, except that a {@link ServiceLoader} passed in must not
 * be used by another thread at the same time.
 */
public final class ProviderConfigurator {

    private ProviderConfigurator() {
    }

    /**
     * Loads the providers of {@code service} with {@link ServiceLoader#load(Class)} (the thread context class loader,
     * and the named modules of the boot layer), selects the one named {@code providerName} and initializes it.
     *
     * <p>This module declares its use of {@code service} at run time, with {@link Module#addUses(Class)}, so that it
     * may load it. That does not resolve modules: on the module path, a provider module that no module {@code requires}
     * is resolved at startup only if some module declares {@code uses} for the service, usually the module that
     * defines it (or with {@code --add-modules}). Providers are registered with {@code provides} in a named module,
     * and with {@code META-INF/services} in an automatic module or on the class path.
     *
     * @param service       the service interface
     * @param providerName  the {@link ProviderName} of the provider to use
     * @param configuration the configuration passed to the provider; see {@link #subset(Map, String)}
     * @param <R>           the type the provider creates
     * @param <T>           the service type
     * @return what the provider's {@link ProviderInitializer#initialize(Map)} returns
     * @throws ProviderException                   if no provider, or more than one, is named {@code providerName}, or
     *                                             a provider is not annotated with {@link ProviderName}
     * @throws IllegalArgumentException            if {@code providerName} is blank, or the configuration lacks a key
     *                                             the provider requires
     * @throws NullPointerException                if an argument, or a key or value in {@code configuration}, is
     *                                             {@code null}
     * @throws java.util.ServiceConfigurationError if a provider cannot be loaded or instantiated
     */
    public static <R, T extends ProviderInitializer<R>> R configure(Class<T> service, String providerName,
                                                                    Map<String, String> configuration) {
        Objects.requireNonNull(service, "service");
        ProviderConfigurator.class.getModule().addUses(service);
        return configure(ServiceLoader.load(service), providerName, configuration);
    }

    /**
     * Selects the provider named {@code providerName} from {@code loader} and initializes it. Use this when the
     * providers come from a particular class loader or module layer; the module that creates the loader declares
     * {@code uses} as usual.
     *
     * @param loader        the providers to choose from
     * @param providerName  the {@link ProviderName} of the provider to use
     * @param configuration the configuration passed to the provider; see {@link #subset(Map, String)}
     * @param <R>           the type the provider creates
     * @param <T>           the service type
     * @return what the provider's {@link ProviderInitializer#initialize(Map)} returns
     * @throws ProviderException                   if no provider, or more than one, is named {@code providerName}, or
     *                                             a provider is not annotated with {@link ProviderName}
     * @throws IllegalArgumentException            if {@code providerName} is blank, or the configuration lacks a key
     *                                             the provider requires
     * @throws NullPointerException                if an argument, or a key or value in {@code configuration}, is
     *                                             {@code null}
     * @throws java.util.ServiceConfigurationError if a provider cannot be loaded or instantiated
     */
    public static <R, T extends ProviderInitializer<R>> R configure(ServiceLoader<T> loader, String providerName,
                                                                    Map<String, String> configuration) {
        Objects.requireNonNull(loader, "loader");
        Objects.requireNonNull(providerName, "providerName");
        Map<String, String> config = Map.copyOf(Objects.requireNonNull(configuration, "configuration"));
        if (providerName.isBlank()) {
            throw new IllegalArgumentException("providerName is blank");
        }

        List<ServiceLoader.Provider<T>> matches = new ArrayList<>();
        Set<String> names = new TreeSet<>();
        loader.stream().forEach(provider -> {
            Class<?> type = provider.type();
            ProviderName name = type.getAnnotation(ProviderName.class);
            if (name == null) {
                throw new ProviderException("Provider " + type.getName() + " is not annotated with @"
                        + ProviderName.class.getSimpleName());
            }
            names.add(name.value());
            if (providerName.equals(name.value())) {
                matches.add(provider);
            }
        });
        if (matches.isEmpty()) {
            throw new ProviderException("No provider named '" + providerName + "'; available: " + names);
        }
        if (matches.size() > 1) {
            List<String> types = new ArrayList<>();
            matches.forEach(p -> types.add(p.type().getName()));
            throw new ProviderException("More than one provider named '" + providerName + "': " + types);
        }

        T initializer = matches.get(0).get();
        Set<String> missing = new LinkedHashSet<>(initializer.configurationKeys());
        missing.removeAll(config.keySet());
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("Provider '" + providerName + "' is missing configuration: " + missing);
        }
        return initializer.initialize(config);
    }

    /**
     * The entries whose key starts with {@code prefix}, with the prefix removed from the key. A key equal to the
     * prefix is left out. For example, prefix {@code "storage."} turns {@code storage.capacity=100} into
     * {@code capacity=100} and leaves out {@code storagex=1} and {@code other=2}.
     *
     * @param configuration the whole configuration
     * @param prefix        the prefix, usually ending with a separator such as {@code "."}
     * @return an unmodifiable map, in the iteration order of {@code configuration}
     * @throws NullPointerException if an argument, or a key or value in {@code configuration}, is {@code null}
     */
    public static Map<String, String> subset(Map<String, String> configuration, String prefix) {
        Objects.requireNonNull(configuration, "configuration");
        Objects.requireNonNull(prefix, "prefix");
        Map<String, String> result = new LinkedHashMap<>();
        configuration.forEach((key, value) -> {
            Objects.requireNonNull(key, "configuration key");
            Objects.requireNonNull(value, key);
            if (key.startsWith(prefix) && key.length() > prefix.length()) {
                result.put(key.substring(prefix.length()), value);
            }
        });
        return Collections.unmodifiableMap(result);
    }
}
