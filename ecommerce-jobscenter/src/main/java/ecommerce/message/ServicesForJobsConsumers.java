package ecommerce.message;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;

import ecommerce.service.client.OrderService;
import ecommerce.service.client.ProductService;

@Configuration
@Profile("!local")
public class ServicesForJobsConsumers {

    @Value("${ecommerce.service.product}")
    String productServiceName;
    @Value("${ecommerce.service.order}")
    String orderServiceName;

	@Bean(name = "productServiceClient")
	@Primary
    public HttpInvokerProxyFactoryBean getProductServiceClient(){
        HttpInvokerProxyFactoryBean proxy = new HttpInvokerProxyFactoryBean();
        proxy.setServiceInterface(ProductService.class);
        proxy.setServiceUrl(productServiceName);
        return proxy;
    }

	@Bean(name = "orderServiceClient")
	@Primary
    public HttpInvokerProxyFactoryBean getOrderServiceClient(){
        HttpInvokerProxyFactoryBean proxy = new HttpInvokerProxyFactoryBean();
        proxy.setServiceInterface(OrderService.class);
        proxy.setServiceUrl(orderServiceName);
        return proxy;
    }

}
