package ecommerce.service.order;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;

import ecommerce.service.client.ProductService;
import ecommerce.service.client.TokenService;
import ecommerce.service.client.UserService;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!local")
public class ServiceClientsForOrder {

	@Value("${ecommerce.service.product}")
	String productServiceName;
	@Value("${ecommerce.service.token}")
	String tokenServiceName;
	@Value("${ecommerce.service.user}")
	String userServiceName;

	@Bean(name = "userServiceClient")
	@Primary
	public HttpInvokerProxyFactoryBean getUserServiceClient(){
		HttpInvokerProxyFactoryBean proxy = new HttpInvokerProxyFactoryBean();
		proxy.setServiceInterface(UserService.class);
		proxy.setServiceUrl(userServiceName);
		return proxy;
	}

	@Bean(name = "tokenServiceClient")
	@Primary
	public HttpInvokerProxyFactoryBean getTokenServiceClient(){
		HttpInvokerProxyFactoryBean proxy = new HttpInvokerProxyFactoryBean();
		proxy.setServiceInterface(TokenService.class);
		proxy.setServiceUrl(tokenServiceName);
		return proxy;
	}
	
	@Bean(name = "productServiceClient")
	@Primary
	public HttpInvokerProxyFactoryBean getProductServiceClient(){
		HttpInvokerProxyFactoryBean proxy = new HttpInvokerProxyFactoryBean();
		proxy.setServiceInterface(ProductService.class);
		proxy.setServiceUrl(productServiceName);
		return proxy;
	}

}
