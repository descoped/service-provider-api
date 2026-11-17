/**
 * The service. It declares uses, so that provider modules no other module requires are resolved at startup.
 */
module consumer.greeting {
    requires transitive io.descoped.service.provider.api;

    exports consumer.greeting;

    uses consumer.greeting.GreeterInitializer;
}
