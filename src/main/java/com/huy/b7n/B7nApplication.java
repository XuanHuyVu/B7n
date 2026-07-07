package com.huy.b7n;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class B7nApplication {

    public static void main(String[] args) {
        SpringApplication.run(B7nApplication.class, args);
    }

}
