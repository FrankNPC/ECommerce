package ecommerce.rest.controller;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ecommerce.service.client.Result;
import ecommerce.service.client.TokenService;
import ecommerce.service.client.UserService;
import ecommerce.service.client.base.User;

@RestController
@CrossOrigin
public class TokenController {
	
    private static final Logger logger = LoggerFactory.getLogger(TokenController.class);

    @Resource
	private TokenService tokenService;

    @Resource
	private UserService userService;

    @RequestMapping("/token/get")
	public String get(
			@RequestParam(value = "callback", defaultValue = "") String callback,
			@RequestParam(value = "session_id", defaultValue = "") String sessionId
			) {
    	logger.info(String.format("[%s][%s]", callback, sessionId));

    	ObjectMapper objectMapper = new ObjectMapper();
    	ObjectNode objectNode = objectMapper.createObjectNode();

    	if (!sessionId.isEmpty()&&sessionId.matches("^[a-zA-Z0-9]{32}$")) {
        	Result<User> userResult = userService.getUserBySessionId(sessionId);
        	if (userResult.getCode()==Result.Code.OK.value) {
            	Result<String> getTokenResult = tokenService.getTokenByUserId(userResult.getData().getId());
        		objectNode.put("token", getTokenResult.getData());
        	}else {
            	Result<String> getTokenResult = tokenService.getToken();
            	if (getTokenResult.getCode()==Result.Code.OK.value) {
            		objectNode.put("token", getTokenResult.getData());
            	}
        	}
    	}else {
        	Result<String> getTokenResult = tokenService.getToken();
        	if (getTokenResult.getCode()==Result.Code.OK.value) {
        		objectNode.put("token", getTokenResult.getData());
        	}
    	}
    	
    	if (callback.isEmpty()) {
    		return objectNode.toString();
    	}else {
    		return callback+"("+objectNode.toString()+")";
    	}
    }
    
}
