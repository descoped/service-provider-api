package io.descoped.service.provider.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The name a provider is selected by. It goes on the class that is registered as the provider: the class named in
 * {@code provides ... with ...} in {@code module-info.java}, or in a {@code META-INF/services} file on the class path.
 * Names are compared exactly, and must be unique among the providers of one service.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ProviderName {

    /**
     * The provider's name.
     *
     * @return the name, not blank
     */
    String value();
}
