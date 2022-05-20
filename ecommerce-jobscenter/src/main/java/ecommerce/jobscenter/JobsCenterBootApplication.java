package ecommerce.jobscenter;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import ecommerce.message.order.ShopCartOrderMessageConsumer;
import ecommerce.message.product.ProductMessageConsumer;

@SpringBootApplication
@ComponentScan(basePackages = { "ecommerce.message", "ecommerce.service" })
@Configuration
@ConfigurationProperties(prefix = "spring")
public class JobsCenterBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobsCenterBootApplication.class, args);
	}

	private Map<String, String> consumer = new HashMap<>();

	public Map<String, String> getKafkaConsumer() {
		return consumer;
	}

	public void setKafkaConsumer(Map<String, String> kafka) {
		this.consumer = kafka;
	}

	@Bean
	public ProductMessageConsumer productMessageSubscriber() throws Exception {
		return new ProductMessageConsumer(new HashMap<>(consumer));
	}

	@Bean
	public ShopCartOrderMessageConsumer shopCartOrderMessageSubscriber() throws Exception {
		return new ShopCartOrderMessageConsumer(new HashMap<>(consumer));
	}

}
