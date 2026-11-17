package io.descoped.service.provider.api;

/**
 * No provider, or more than one, has the requested name; or a provider is not annotated with {@link ProviderName}.
 * A provider that cannot be loaded or instantiated is reported by {@link java.util.ServiceLoader} as a
 * {@link java.util.ServiceConfigurationError} instead.
 */
public class ProviderException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception.
     *
     * @param message what went wrong
     */
    public ProviderException(String message) {
        super(message);
    }
}
