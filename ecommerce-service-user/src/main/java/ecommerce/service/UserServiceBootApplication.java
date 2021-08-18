package ecommerce.service;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

//import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
//@EnableEurekaClient

@SpringBootApplication
@ComponentScan(basePackages = {"ecommerce.service"})
@MapperScan(basePackages="ecommerce.service.user", annotationClass=Mapper.class)
public class UserServiceBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceBootApplication.class, args);
    }

}
