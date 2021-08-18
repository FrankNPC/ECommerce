package ecommerce.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

//import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
//@EnableEurekaClient

@SpringBootApplication
@ComponentScan(basePackages = {"ecommerce.rest", "ecommerce.service"})
public class RestBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestBootApplication.class, args);
    }

}
