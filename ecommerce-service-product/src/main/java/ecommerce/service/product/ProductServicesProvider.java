package ecommerce.service.product;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import ecommerce.service.client.ProductService;

@Configuration
@EnableWebMvc
public class ProductServicesProvider{
	
	@Bean("/service/productService")
	public HttpInvokerServiceExporter provideProductService(ProductService productService) {
		HttpInvokerServiceExporter httpInvokerServiceExporter = new HttpInvokerServiceExporter();
		httpInvokerServiceExporter.setService(productService);
		httpInvokerServiceExporter.setServiceInterface(ProductService.class);
		httpInvokerServiceExporter.afterPropertiesSet();
		return httpInvokerServiceExporter;
	}

}
