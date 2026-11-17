/**
 * The tests, as a module of their own that uses the library the way an application module does: it requires the
 * library, and declares uses and provides for its services. Open, so that JUnit can reach the test classes.
 */
open module io.descoped.service.provider.api.test {
    requires io.descoped.service.provider.api;
    requires org.junit.jupiter.api;

    uses io.descoped.service.provider.api.test.GreeterInitializer;
    uses io.descoped.service.provider.api.test.DuplicateInitializer;
    uses io.descoped.service.provider.api.test.UnnamedInitializer;
    uses io.descoped.service.provider.api.test.UnregisteredInitializer;

    provides io.descoped.service.provider.api.test.GreeterInitializer with
            io.descoped.service.provider.api.test.HelloInitializer,
            io.descoped.service.provider.api.test.HiInitializer;
    provides io.descoped.service.provider.api.test.DuplicateInitializer with
            io.descoped.service.provider.api.test.DuplicateInitializer.First,
            io.descoped.service.provider.api.test.DuplicateInitializer.Second;
    provides io.descoped.service.provider.api.test.UnnamedInitializer with
            io.descoped.service.provider.api.test.UnnamedInitializer.Anonymous;
}
