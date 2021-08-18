package ecommerce.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import ecommerce.service.product.RedisLock;
import ecommerce.service.product.dao.HbaseTemplate;

//import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
//@EnableEurekaClient

@SpringBootApplication
@ComponentScan(basePackages = {"ecommerce.service"})
@Configuration
@ConfigurationProperties(prefix = "spring")
@MapperScan(basePackages="ecommerce.service.product", annotationClass=Mapper.class)
public class ProductServiceBootApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ProductServiceBootApplication.class, args);
    }

    private Map<String, String> hbase = new HashMap<>();
    public Map<String, String> getHbase() {
        return hbase;
    }
    public void setHbase(Map<String, String> config) {
        this.hbase = config;
    }
    @Bean
	@Primary
    public HbaseTemplate getHbaseTemplate() {
        return new HbaseTemplate(hbase);
	}
    
    private Map<String, Object> kafka = new HashMap<>();
    public Map<String, Object> getKafkaProducer() {
        return kafka;
    }
    public void setKafkaProducer(Map<String, String> config) {
        this.kafka.putAll(config);
    }
    
    @Bean
	@Primary
    public KafkaTemplate<String, String> getKafkaTemplate() {
        return new KafkaTemplate<String, String>(new DefaultKafkaProducerFactory<String, String>(kafka));
    }
    
    @Value("${server.nodeid}")
    String serverNodeId;
    @Bean
	@Primary
    public RedisLock getRedisLock() {
        return new RedisLock(Long.parseLong(serverNodeId));
	}
}
