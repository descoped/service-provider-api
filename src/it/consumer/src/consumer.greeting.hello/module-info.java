/**
 * Two providers. No module requires this one: it is found by service binding, through the uses in consumer.greeting.
 */
module consumer.greeting.hello {
    requires consumer.greeting;

    provides consumer.greeting.GreeterInitializer with
            consumer.greeting.hello.HelloInitializer,
            consumer.greeting.hello.MethodInitializer;
}
