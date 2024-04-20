package com.coding.fullstack.ssolserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 通过域名 ssoserver.com 访问
 */
@SpringBootApplication
public class FullstackSsoServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FullstackSsoServerApplication.class, args);
    }

}
