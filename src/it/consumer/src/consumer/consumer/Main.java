package consumer;

import consumer.greeting.GreeterInitializer;
import io.descoped.service.provider.api.ProviderConfigurator;
import io.descoped.service.provider.api.ProviderException;

import java.util.Map;

/**
 * Uses the library as an application would: select by name, configure from a prefixed subset, fail on a wrong name.
 * The providers live in modules this one does not require: consumer.greeting.hello (explicit) and greeting.auto
 * (automatic). They are found because consumer.greeting declares uses for the service.
 */
public final class Main {

    private Main() {
    }

    /**
     * Runs the checks.
     *
     * @param args {@code module}, {@code automatic} or {@code classpath}
     */
    public static void main(String[] args) {
        String mode = args.length > 0 ? args[0] : "?";
        boolean modulePath = !mode.equals("classpath");
        Map<String, String> configuration = Map.of("greeter.name", "World", "other", "x");

        String hello = ProviderConfigurator.configure(GreeterInitializer.class, "hello",
                ProviderConfigurator.subset(configuration, "greeter."));
        check("Hello World".equals(hello), "hello provider: " + hello);

        String auto = ProviderConfigurator.configure(GreeterInitializer.class, "auto", Map.of());
        check("from an automatic module".equals(auto), "automatic module provider: " + auto);

        if (modulePath) {
            String method = ProviderConfigurator.configure(GreeterInitializer.class, "method", Map.of());
            check("from provider()".equals(method), "provider() method: " + method);
        }

        try {
            ProviderConfigurator.configure(GreeterInitializer.class, "missing", Map.of());
            check(false, "unknown name rejected");
        } catch (ProviderException expected) {
            String available = modulePath ? "[auto, hello, method]" : "[auto, hello]";
            check(expected.getMessage().endsWith("available: " + available), expected.getMessage());
        }

        Module module = ProviderConfigurator.class.getModule();
        check(modulePath == module.isNamed(), "module mode");
        String library = module.isNamed() ? "module " + module.getName() : "on the class path";
        System.out.println("OK (" + mode + "): library " + library + ", application " + Main.class.getModule());
    }

    private static void check(boolean condition, String what) {
        if (!condition) {
            throw new AssertionError(what);
        }
    }
}
