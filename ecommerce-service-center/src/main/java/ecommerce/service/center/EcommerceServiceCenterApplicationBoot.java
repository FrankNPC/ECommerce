package ecommerce.service.center;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class EcommerceServiceCenterApplicationBoot {

    public static void main(String[] args) {
        SpringApplication.run(EcommerceServiceCenterApplicationBoot.class, args);
    }
}