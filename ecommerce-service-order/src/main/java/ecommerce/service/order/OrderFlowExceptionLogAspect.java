package ecommerce.service.order;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Aspect
@Component
@Order(1)
public class OrderFlowExceptionLogAspect {
	private static final Logger logger = LoggerFactory.getLogger(OrderFlowExceptionLogAspect.class);

	@Pointcut("execution(* *..UserController.addUser())")
	public void declareJoinPointExpression() {
	}
	
	@Around("declareJoinPointExpression()")
	public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
		try {
			return joinPoint.proceed(joinPoint.getArgs());
		} catch (Throwable throwable) {
			logger.error(getParameters(((MethodSignature) joinPoint.getSignature()).getMethod(), joinPoint.getArgs()));
			logger.error(String.valueOf(joinPoint.getSignature()), throwable);
			throw throwable;
		}
	
	}

	private String getParameters(Method method, Object[] args) {
		List<Object> argList = new ArrayList<>();
		Parameter[] parameters = method.getParameters();
		for (int i = 0; i < parameters.length; i++) {
			RequestBody requestBody = parameters[i].getAnnotation(RequestBody.class);
			if (requestBody != null) {
				argList.add(args[i].toString());
			}
			RequestParam requestParam = parameters[i].getAnnotation(RequestParam.class);
			if (requestParam != null) {
				Map<String, Object> map = new HashMap<>();
				String key = parameters[i].getName();
				if (!StringUtils.isEmpty(requestParam.value())) {
					key = requestParam.value();
				}
				map.put(key, args[i]);
				argList.add(map);
			}
		}
		try {
			return new ObjectMapper().writeValueAsString(argList);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return "";
		}
	}
}
