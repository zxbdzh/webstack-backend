package com.zxb.webstackbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class WebstackBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebstackBackendApplication.class, args);
    }

}
