package ecommerce.rest.controller;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ecommerce.service.client.Result;
import ecommerce.service.client.TokenService;
import ecommerce.service.client.UserService;
import ecommerce.service.client.base.User;

@RestController
@CrossOrigin
public class UserController {
	
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Resource
	private TokenService tokenService;

    @Resource
	private UserService userService;

    private String JsonToString(User user, ObjectNode objectNode, String callback, String message) {
    	if (!message.isEmpty()) {
        	objectNode.put("message", message);
    	}
    	if (user!=null) {
        	Result<String> getTokenResult = tokenService.getTokenByUserId(user.getId());
    		objectNode.put("token", getTokenResult.getData());
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
    
    private String JsonToString(ObjectNode objectNode, String callback, String message) {
    	if (!message.isEmpty()) {
        	objectNode.put("message", message);
    	}
    	if (callback.isEmpty()) {
    		return objectNode.toString();
    	}else {
    		return callback+"("+objectNode.toString()+")";
    	}
    }
    
    @RequestMapping("/user/create")
	public String create(
			@RequestParam(value = "callback", defaultValue = "") String callback,
			@RequestParam(value = "username", defaultValue = "") String username,
			@RequestParam(value = "password", defaultValue = "") String password,
			@RequestParam(value = "token", defaultValue = "") String token
						) {
    	logger.info(String.format("[%s][%s][%s][%s]", callback, token, username, password));

    	ObjectMapper jsonBuilder = new ObjectMapper();
		ObjectNode objectNode = jsonBuilder.createObjectNode();
    	if (!username.matches("^[a-zA-Z0-9_]{8,32}$")) {
    		return JsonToString(objectNode, callback, "username irregularity.");
    	}
    	if (!password.matches("^[a-zA-Z0-9_]{32}$")) {
    		return JsonToString(objectNode, callback, "password irregularity.");
    	}
    	if (!token.matches("^token_[0-9a-zA-Z]{32}$")) {
    		return JsonToString(objectNode, callback, "token irregularity.");
    	}
    	
    	Result<Boolean> tokenResult = tokenService.verifyToken(token);
    	if (tokenResult.getCode()!=Result.Code.OK.value) {
    		return JsonToString(objectNode, callback, "token invalid.");
    	}

    	Result<User> userResult = userService.getUserByName(username);
    	if (userResult.getCode()==Result.Code.OK.value) {
    		return JsonToString(objectNode, callback, "user exist.");
    	}
    	
    	User user = new User();
    	user.setUsername(username);
    	user.setPassword(password);

    	Result<User> newUserResult = userService.saveOrUpdateUser(user);
    	if (newUserResult.getCode()!=Result.Code.OK.value) {
    		return JsonToString(objectNode, callback, "create user failed.");
    	}
    	user = newUserResult.getData();

		objectNode.put("session_id", user.getSessionId());
		return JsonToString(user, objectNode, callback, "");
    }
    
    @RequestMapping("/user/login")
	public String login(
			@RequestParam(value = "callback", defaultValue = "") String callback,
			@RequestParam(value = "username", defaultValue = "") String username,
			@RequestParam(value = "password", defaultValue = "") String password,
			@RequestParam(value = "token", defaultValue = "") String token
						) {
    	logger.info(String.format("[%s][%s][%s][%s]", callback, token, username, password));

    	ObjectMapper objectMapper = new ObjectMapper();
    	ObjectNode objectNode = objectMapper.createObjectNode();
    	if (!username.matches("^[a-zA-Z0-9_]{8,32}$")) {
    		return JsonToString(objectNode, callback, "username irregularity.");
    	}
    	if (!password.matches("^[a-zA-Z0-9]{32}$")) {
    		return JsonToString(objectNode, callback, "password irregularity.");
    	}
    	if (!token.matches("^token_[0-9a-zA-Z]{32}$")) {
    		return JsonToString(objectNode, callback, "token irregularity.");
    	}
    	
    	Result<Boolean> tokenResult = tokenService.verifyToken(token);
    	if (tokenResult.getCode()!=Result.Code.OK.value) {
    		return JsonToString(objectNode, callback, "token invalid.");
    	}

    	Result<User> userResult = userService.getUserByName(username);
    	if (userResult.getCode()!=Result.Code.OK.value||!password.equals(userResult.getData().getPassword())) {
    		return JsonToString(objectNode, callback, "user not exist.");
    	}
    	
    	User user = userResult.getData();

		objectNode.put("session_id", user.getSessionId());
		return JsonToString(user, objectNode, callback, "");
    }

    @RequestMapping("/user/reset")
	public String reset(
			@RequestParam(value = "callback", defaultValue = "") String callback,
			@RequestParam(value = "session_id", defaultValue = "") String sessionId,
			@RequestParam(value = "token", defaultValue = "") String token
						) {
    	logger.info(String.format("[%s][%s][%s]", callback, token, sessionId));
    	
    	ObjectMapper objectMapper = new ObjectMapper();
    	ObjectNode objectNode = objectMapper.createObjectNode();
    	if (!sessionId.matches("^[a-zA-Z0-9]{32}$")) {
    		return JsonToString(objectNode, callback, "session_id irregularity.");
    	}
    	if (!token.matches("^token_[0-9a-zA-Z]{32}$")) {
    		return JsonToString(objectNode, callback, "token irregularity.");
    	}
    	
    	Result<Boolean> tokenResult = tokenService.verifyToken(token);
    	if (tokenResult.getCode()!=Result.Code.OK.value) {
    		return JsonToString(objectNode, callback, "token invalid.");
    	}

    	Result<User> userResult = userService.getUserBySessionId(sessionId);
    	if (userResult.getCode()!=Result.Code.OK.value) {
    		return JsonToString(objectNode, callback, "user not exist.");
    	}
    	User user = userResult.getData();
    	user.setSessionId(null);

    	Result<User> updateUserResult = userService.saveOrUpdateUser(user);
    	if (updateUserResult.getCode()!=Result.Code.OK.value) {
    		return JsonToString(objectNode, callback, "reset user failed.");
    	}

		objectNode.put("session_id", user.getSessionId());
		return JsonToString(user, objectNode, callback, "");
    }

    @RequestMapping("/user/modify")
	public String modify(
			@RequestParam(value = "callback", defaultValue = "") String callback,
			@RequestParam(value = "password", defaultValue = "") String password,
			@RequestParam(value = "session_id", defaultValue = "") String sessionId,
			@RequestParam(value = "token", defaultValue = "") String token
						) {
    	logger.info(String.format("[%s][%s][%s][%s]", callback, token, sessionId, password));

    	ObjectMapper objectMapper = new ObjectMapper();
    	ObjectNode objectNode = objectMapper.createObjectNode();
    	if (!sessionId.matches("^[a-zA-Z0-9]{32}$")) {
    		return JsonToString(objectNode, callback, "session_id irregularity.");
    	}
    	if (!password.matches("^[a-zA-Z0-9]{32}$")) {
    		return JsonToString(objectNode, callback, "password irregularity.");
    	}
    	if (!token.matches("^token_[0-9a-zA-Z]{32}$")) {
    		return JsonToString(objectNode, callback, "token irregularity.");
    	}
    	
    	Result<Boolean> tokenResult = tokenService.verifyToken(token);
    	if (tokenResult.getCode()!=Result.Code.OK.value) {
    		return JsonToString(objectNode, callback, "token invalid.");
    	}

    	Result<User> userResult = userService.getUserBySessionId(sessionId);
    	if (userResult.getCode()!=Result.Code.OK.value) {
    		return JsonToString(objectNode, callback, "user not exist.");
    	}
    	User user = userResult.getData();
    	user.setPassword(password);

    	Result<User> updateUserResult = userService.saveOrUpdateUser(user);
    	if (updateUserResult.getCode()!=Result.Code.OK.value) {
    		return JsonToString(objectNode, callback, "modify user failed.");
    	}

		return JsonToString(user, objectNode, callback, "");
    }

    @RequestMapping("/user/remove")
	public String remove(
			@RequestParam(value = "callback", defaultValue = "") String callback,
			@RequestParam(value = "session_id", defaultValue = "") String sessionId,
			@RequestParam(value = "token", defaultValue = "") String token
						) {
    	logger.info(String.format("[%s][%s][%s]", callback, token, sessionId));

    	ObjectMapper objectMapper = new ObjectMapper();
    	ObjectNode objectNode = objectMapper.createObjectNode();
    	objectNode.put("code", -1);
    	if (!token.matches("^token_[0-9a-zA-Z]{32}$")) {
    		return JsonToString(objectNode, callback, "token irregularity.");
    	}
    	if (!sessionId.matches("^[a-zA-Z0-9]{32}$")) {
    		return JsonToString(objectNode, callback, "session_id irregularity.");
    	}
    	
    	Result<Boolean> tokenResult = tokenService.verifyToken(token);
    	if (tokenResult.getCode()!=Result.Code.OK.value) {
    		return JsonToString(objectNode, callback, "token invalid.");
    	}

    	Result<User> userResult = userService.getUserBySessionId(sessionId);
    	if (userResult.getCode()!=Result.Code.OK.value) {
    		return JsonToString(objectNode, callback, "user not exist.");
    	}
    	User user = userResult.getData();
    	
    	Result<Boolean> removeUserResult = userService.removeUserById(user);
    	if (removeUserResult.getCode()!=Result.Code.OK.value||!removeUserResult.getData()) {
    		return JsonToString(objectNode, callback, "remove user failed.");
    	}

		return JsonToString(user, objectNode, callback, "");
    }
    
    @RequestMapping("/user/query")
	public String query(
			@RequestParam(value = "callback", defaultValue = "") String callback,
			@RequestParam(value = "token", defaultValue = "") String token
						) {
    	logger.info(String.format("[%s][%s]", callback, token));

    	ObjectMapper objectMapper = new ObjectMapper();
    	ObjectNode objectNode = objectMapper.createObjectNode();
    	if (!token.matches("^token_[0-9a-zA-Z]{32}$")) {
    		return JsonToString(objectNode, callback, "token irregularity.");
    	}

    	Result<List<User>> queryResult = userService.queryUsers(new User(), 0, 100);
    	if (queryResult.getCode()!=Result.Code.OK.value) {
    		return JsonToString(objectNode, callback, "users not exist.");
    	}
    	
		try {
			objectNode.put("users", objectMapper.writeValueAsString(queryResult.getData()));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return JsonToString(objectNode, callback, "");
    }


}
