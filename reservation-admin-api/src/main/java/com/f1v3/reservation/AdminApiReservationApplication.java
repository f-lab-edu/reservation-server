package com.f1v3.reservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication(scanBasePackages = {
        "com.f1v3.reservation.admin",
        "com.f1v3.reservation.common",
        "com.f1v3.reservation.auth"
})
public class AdminApiReservationApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminApiReservationApplication.class, args);
    }

}
