package com.example.obinspection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ObInspectionApplication {

    public static void main(String[] args) {
        SpringApplication.run(ObInspectionApplication.class, args);
    }
}
