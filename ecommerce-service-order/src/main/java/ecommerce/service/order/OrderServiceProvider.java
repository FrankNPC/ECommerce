package ecommerce.service.order;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import ecommerce.service.client.OrderService;
import ecommerce.service.order.client.OrderFlowService;

@Configuration
@EnableWebMvc
public class OrderServiceProvider{
	
	@Bean("/service/orderService")
	public HttpInvokerServiceExporter provideProductService(OrderService orderService) {
		HttpInvokerServiceExporter httpInvokerServiceExporter = new HttpInvokerServiceExporter();
		httpInvokerServiceExporter.setService(orderService);
		httpInvokerServiceExporter.setServiceInterface(OrderService.class);
		httpInvokerServiceExporter.afterPropertiesSet();
		return httpInvokerServiceExporter;
    }

	@Bean("/service/orderFlowService")
	public HttpInvokerServiceExporter orderService(OrderFlowService orderFlowService) {
		HttpInvokerServiceExporter httpInvokerServiceExporter = new HttpInvokerServiceExporter();
		httpInvokerServiceExporter.setService(orderFlowService);
		httpInvokerServiceExporter.setServiceInterface(OrderFlowService.class);
		httpInvokerServiceExporter.afterPropertiesSet();
		return httpInvokerServiceExporter;
    }
	
}
