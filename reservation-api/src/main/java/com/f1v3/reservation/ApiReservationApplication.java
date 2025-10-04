package com.f1v3.reservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ApiReservationApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiReservationApplication.class, args);
    }

}
