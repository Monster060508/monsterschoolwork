package com.enterprise.sales;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("com.enterprise.sales.mapper")
@ComponentScan(basePackages = {"com.enterprise.sales", "com.enterprise.sales.config"})
public class SalesManagementApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SalesManagementApplication.class, args);
    }
}