package com.coding.fullstack.sso.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 通过域名 client1.com 访问 通过域名 client2.com 访问
 */
@SpringBootApplication
public class FullstackSsoClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(FullstackSsoClientApplication.class, args);
    }

}
