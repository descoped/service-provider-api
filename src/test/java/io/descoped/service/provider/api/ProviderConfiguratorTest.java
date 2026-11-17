package io.descoped.service.provider.api;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProviderConfiguratorTest {

    private static final Map<String, String> HELLO = Map.of("name", "World", "punctuation", "!");

    @Test
    void selectsTheNamedProviderAndInitializesIt() {
        Greeter greeter = ProviderConfigurator.configure(GreeterInitializer.class, "hello", HELLO);
        assertEquals("Hello World!", greeter.greeting());
    }

    @Test
    void selectsAProviderThatRequiresNoKeys() {
        Greeter greeter = ProviderConfigurator.configure(GreeterInitializer.class, "hi", Map.of());
        assertEquals("Hi", greeter.greeting());
    }

    @Test
    void instantiatesOnlyTheChosenProvider() {
        int hi = HiInitializer.instances;
        int hello = HelloInitializer.instances;
        ProviderConfigurator.configure(GreeterInitializer.class, "hello", HELLO);
        assertEquals(hi, HiInitializer.instances);
        assertEquals(hello + 1, HelloInitializer.instances);
    }

    @Test
    void passesOptionalKeysAndAnUnmodifiableCopy() {
        Map<String, String> configuration = new HashMap<>(HELLO);
        configuration.put("optional", "yes");
        Greeter greeter = ProviderConfigurator.configure(GreeterInitializer.class, "hello", configuration);
        configuration.put("name", "changed");
        assertEquals(Map.of("name", "World", "punctuation", "!", "optional", "yes"), greeter.configuration());
        assertThrows(UnsupportedOperationException.class, () -> greeter.configuration().put("x", "y"));
    }

    @Test
    void acceptsAServiceLoader() {
        ServiceLoader<GreeterInitializer> loader = ServiceLoader.load(GreeterInitializer.class);
        assertEquals("Hi", ProviderConfigurator.configure(loader, "hi", Map.of()).greeting());
        assertEquals("Hello World!", ProviderConfigurator.configure(loader, "hello", HELLO).greeting());
    }

    @Test
    void rejectsMissingRequiredKeys() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> ProviderConfigurator.configure(GreeterInitializer.class, "hello", Map.of("other", "x")));
        assertTrue(e.getMessage().contains("[name, punctuation]") || e.getMessage().contains("[punctuation, name]"),
                e.getMessage());
    }

    @Test
    void rejectsAnUnknownNameAndListsTheKnownOnes() {
        ProviderException e = assertThrows(ProviderException.class,
                () -> ProviderConfigurator.configure(GreeterInitializer.class, "howdy", Map.of()));
        assertEquals("No provider named 'howdy'; available: [hello, hi]", e.getMessage());
    }

    @Test
    void rejectsAServiceWithoutProviders() {
        ProviderException e = assertThrows(ProviderException.class,
                () -> ProviderConfigurator.configure(UnregisteredInitializer.class, "any", Map.of()));
        assertEquals("No provider named 'any'; available: []", e.getMessage());
    }

    @Test
    void rejectsTwoProvidersWithTheSameName() {
        ProviderException e = assertThrows(ProviderException.class,
                () -> ProviderConfigurator.configure(DuplicateInitializer.class, "same", Map.of()));
        assertTrue(e.getMessage().startsWith("More than one provider named 'same'"), e.getMessage());
        assertTrue(e.getMessage().contains("DuplicateInitializer$First"), e.getMessage());
        assertTrue(e.getMessage().contains("DuplicateInitializer$Second"), e.getMessage());
    }

    @Test
    void rejectsAProviderWithoutAName() {
        ProviderException e = assertThrows(ProviderException.class,
                () -> ProviderConfigurator.configure(UnnamedInitializer.class, "anonymous", Map.of()));
        assertTrue(e.getMessage().contains("UnnamedInitializer$Anonymous"), e.getMessage());
    }

    @Test
    void rejectsBlankAndNullArguments() {
        assertThrows(IllegalArgumentException.class,
                () -> ProviderConfigurator.configure(GreeterInitializer.class, " ", Map.of()));
        assertThrows(NullPointerException.class,
                () -> ProviderConfigurator.configure((Class<GreeterInitializer>) null, "hi", Map.of()));
        assertThrows(NullPointerException.class,
                () -> ProviderConfigurator.configure((ServiceLoader<GreeterInitializer>) null, "hi", Map.of()));
        assertThrows(NullPointerException.class,
                () -> ProviderConfigurator.configure(GreeterInitializer.class, null, Map.of()));
        assertThrows(NullPointerException.class,
                () -> ProviderConfigurator.configure(GreeterInitializer.class, "hi", null));
        Map<String, String> nullValue = new HashMap<>();
        nullValue.put("name", null);
        assertThrows(NullPointerException.class,
                () -> ProviderConfigurator.configure(GreeterInitializer.class, "hi", nullValue));
    }

    @Test
    void isThreadSafe() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(8);
        try {
            Callable<String> task = () -> ProviderConfigurator.configure(GreeterInitializer.class, "hello", HELLO)
                    .greeting();
            List<Future<String>> results = executor.invokeAll(java.util.Collections.nCopies(200, task));
            for (Future<String> result : results) {
                assertEquals("Hello World!", result.get());
            }
        } finally {
            executor.shutdown();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    @Test
    void subsetKeepsPrefixedKeysWithThePrefixRemoved() {
        Map<String, String> configuration = new LinkedHashMap<>();
        configuration.put("a", "A");
        configuration.put("a.b.last", "LAST");
        configuration.put("a.ba.BA", "BA");
        configuration.put("a.b.first", "FIRST");
        configuration.put("a.b.", "PREFIX ONLY");
        configuration.put("a.bZ", "bZ");
        configuration.put("a.b.middle", "MIDDLE");
        configuration.put("c", "C");
        Map<String, String> subset = ProviderConfigurator.subset(configuration, "a.b.");
        assertEquals(List.of("last", "first", "middle"), List.copyOf(subset.keySet()));
        assertEquals(Map.of("first", "FIRST", "middle", "MIDDLE", "last", "LAST"), subset);
        assertThrows(UnsupportedOperationException.class, () -> subset.put("x", "y"));
    }

    @Test
    void subsetRejectsNull() {
        assertThrows(NullPointerException.class, () -> ProviderConfigurator.subset(null, "a."));
        assertThrows(NullPointerException.class, () -> ProviderConfigurator.subset(Map.of(), null));
        Map<String, String> nullValue = new HashMap<>();
        nullValue.put("a.b", null);
        assertThrows(NullPointerException.class, () -> ProviderConfigurator.subset(nullValue, "a."));
    }
}
