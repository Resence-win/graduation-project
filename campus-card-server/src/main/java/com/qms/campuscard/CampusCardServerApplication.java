package com.qms.campuscard;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.qms.campuscard.mapper")
public class CampusCardServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusCardServerApplication.class, args);
    }
}

