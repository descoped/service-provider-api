package io.descoped.service.provider.api.test;

import io.descoped.service.provider.api.ProviderInitializer;

/** Test service with no providers at all. */
public interface UnregisteredInitializer extends ProviderInitializer<String> {
}
