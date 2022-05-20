package ecommerce.service.user;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import ecommerce.service.client.TokenService;
import ecommerce.service.client.UserService;

@Configuration
@EnableWebMvc
public class UserServicesProvider{

	@Bean("/service/tokenService")
	public HttpInvokerServiceExporter provideTokenService(TokenService tokenService) {
		HttpInvokerServiceExporter httpInvokerServiceExporter = new HttpInvokerServiceExporter();
		httpInvokerServiceExporter.setService(tokenService);
		httpInvokerServiceExporter.setServiceInterface(TokenService.class);
		httpInvokerServiceExporter.afterPropertiesSet();
		return httpInvokerServiceExporter;
	}
	@Bean("/service/userService")
	public HttpInvokerServiceExporter provideUserService(UserService userService) {
		HttpInvokerServiceExporter httpInvokerServiceExporter = new HttpInvokerServiceExporter();
		httpInvokerServiceExporter.setService(userService);
		httpInvokerServiceExporter.setServiceInterface(UserService.class);
		httpInvokerServiceExporter.afterPropertiesSet();
		return httpInvokerServiceExporter;
	}
}
