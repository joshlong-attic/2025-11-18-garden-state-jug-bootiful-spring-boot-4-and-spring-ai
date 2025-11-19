package com.example.adoptions;

import org.springframework.beans.factory.BeanRegistrar;
import org.springframework.beans.factory.BeanRegistry;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.resilience.annotation.ConcurrencyLimit;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.registry.ImportHttpServices;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

@Import(MyBeanRegistrar.class)
@EnableResilientMethods
@Configuration
@ImportHttpServices(CatFactsClient.class)
class CatsConfiguration {


}

class MyBeanRegistrar implements BeanRegistrar {

    @Override
    public void register(BeanRegistry registry, Environment env) {
        registry.registerBean(MyApplicationRunner.class);
        registry.registerBean(MyApplicationRunner.class,
                spec -> spec
                        .supplier(supplierContext -> new MyApplicationRunner()));
    }
}

class MyApplicationRunner implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        IO.println("hi!");
    }
}


// https://www.catfacts.net/api

record CatFact(String fact) {
}

record CatFacts(Collection<CatFact> facts) {
}

interface CatFactsClient {

    @GetExchange("https://www.catfacts.net/api/")
    CatFacts facts();
}

@Controller
@ResponseBody
class CatFactsController {

    private final CatFactsClient catFactsClient;

    private final AtomicInteger counter = new AtomicInteger(0);

    CatFactsController(CatFactsClient catFactsClient) {
        this.catFactsClient = catFactsClient;
    }

    @ConcurrencyLimit(10)
    @Retryable(includes = {IllegalStateException.class},
            maxAttempts = 4)
    @GetMapping("/cats/facts")
    Collection<CatFact> facts() {
        if (this.counter.incrementAndGet() < 4) {
            IO.println("oops!");
            throw new IllegalStateException();
        }
        IO.println("facts!");
        return this.catFactsClient.facts().facts();
    }
}
