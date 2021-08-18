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

import ecommerce.service.client.ProductService;
import ecommerce.service.client.Result;
import ecommerce.service.client.TokenService;
import ecommerce.service.client.UserService;
import ecommerce.service.client.base.Product;
import ecommerce.service.client.base.User;

@RestController
@CrossOrigin
public class ProductController {
	
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Resource
	private TokenService tokenService;

    @Resource
	private ProductService productService;

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
    
    @RequestMapping("/product/create")
	public String create(
			@RequestParam(value = "callback", defaultValue = "") String callback,
			@RequestParam(value = "name", defaultValue = "") String name,
			@RequestParam(value = "price", defaultValue = "") String price,
			@RequestParam(value = "quantity", defaultValue = "") String quantity,
			@RequestParam(value = "category_id", defaultValue = "") String categoryId,
			@RequestParam(value = "status", defaultValue = "") String status,
			@RequestParam(value = "token", defaultValue = "") String token,
			@RequestParam(value = "session_id", defaultValue = "") String sessionId
						) {
    	logger.info(String.format("[%s][%s][%s][%s][%s][%s]", callback, token, sessionId, price, quantity, name));

    	ObjectMapper objectMapper = new ObjectMapper();
    	ObjectNode objectNode = objectMapper.createObjectNode();
    	if (!sessionId.matches("^[0-9a-zA-Z]{32}$")) {
    		return JsonToString(objectNode, callback, "session_id irregularity.");
    	}
    	if (!name.matches("^.{8,128}$")) {
    		return JsonToString(objectNode, callback, "name irregularity.");
    	}
    	if (!price.matches("^\\d{1,20}$")) {
    		return JsonToString(objectNode, callback, "price irregularity.");
    	}
    	if (!categoryId.matches("^\\d{1,11}$")) {
    		return JsonToString(objectNode, callback, "category_id irregularity.");
    	}
    	if (!status.matches("^\\d{1,11}$")) {
    		return JsonToString(objectNode, callback, "status irregularity.");
    	}
    	if (!quantity.matches("^\\-?\\d{1,20}$")) {
    		return JsonToString(objectNode, callback, "quantity irregularity.");
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
    		return JsonToString(objectNode, callback, "user irregularity.");
    	}
    	User user = userResult.getData();
    	Product product = new Product();
    	product.setName(name);
    	product.setPrice(Long.parseLong(price));
    	product.setQuantity(Long.parseLong(quantity));
    	product.setCategoryId(Integer.parseInt(categoryId));
    	product.setStatus(Integer.parseInt(status));

    	Result<Product> newProdResult = productService.saveOrUpdateProduct(product);
    	if (newProdResult.getCode()!=Result.Code.OK.value) {
    		return JsonToString(objectNode, callback, "create product failed.");
    	}
    	try {
			objectNode.put("product", objectMapper.writeValueAsString(newProdResult.getData()));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return JsonToString(user, objectNode, callback, "");
    }
    
    @RequestMapping("/product/modify")
	public String modify(
			@RequestParam(value = "callback", defaultValue = "") String callback,
			@RequestParam(value = "id", defaultValue = "") String id,
			@RequestParam(value = "name", defaultValue = "") String name,
			@RequestParam(value = "price", defaultValue = "") String price,
			@RequestParam(value = "category_id", defaultValue = "") String categoryId,
			@RequestParam(value = "status", defaultValue = "") String status,
			@RequestParam(value = "token", defaultValue = "") String token,
			@RequestParam(value = "session_id", defaultValue = "") String sessionId
						) {
    	logger.info(String.format("[%s][%s][%s][%s][%s][%s]", callback, token, sessionId, id, price, name));

    	ObjectMapper objectMapper = new ObjectMapper();
    	ObjectNode objectNode = objectMapper.createObjectNode();
    	if (!sessionId.matches("^[0-9a-zA-Z]{32}$")) {
    		return JsonToString(objectNode, callback, "session_id irregularity.");
    	}
    	if (!id.matches("^\\d{1,20}$")) {
    		return JsonToString(objectNode, callback, "id irregularity.");
    	}
    	if (!name.matches("^.{8,128}$")) {
    		return JsonToString(objectNode, callback, "name irregularity.");
    	}
    	if (!price.matches("^\\d{1,20}$")) {
    		return JsonToString(objectNode, callback, "price irregularity.");
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
    		return JsonToString(objectNode, callback, "user irregularity.");
    	}
    	
    	Result<Product> prodResult = productService.getProductById(Long.parseLong(id));
    	if (prodResult.getCode()!=Result.Code.OK.value) {
    		return JsonToString(objectNode, callback, "product not exist.");
    	}
    	
    	Product product = prodResult.getData();
    	product.setName(name);
    	product.setPrice(Long.parseLong(price));

    	Result<Product> updateProdResult = productService.saveOrUpdateProduct(product);
    	if (updateProdResult.getCode()!=Result.Code.OK.value) {
    		return JsonToString(objectNode, callback, "modify product failed.");
    	}
    	try {
			objectNode.put("product", objectMapper.writeValueAsString(updateProdResult.getData()));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return JsonToString(userResult.getData(), objectNode, callback, "");
    }

    @RequestMapping("/product/remove")
	public String remove(
			@RequestParam(value = "callback", defaultValue = "") String callback,
			@RequestParam(value = "id", defaultValue = "") String id,
			@RequestParam(value = "token", defaultValue = "") String token,
			@RequestParam(value = "session_id", defaultValue = "") String sessionId
						) {
    	logger.info(String.format("[%s][%s][%s][%s]", callback, token, sessionId, id));

    	ObjectMapper objectMapper = new ObjectMapper();
    	ObjectNode objectNode = objectMapper.createObjectNode();
    	if (!sessionId.matches("^[0-9a-zA-Z]{32}$")) {
    		return JsonToString(objectNode, callback, "session_id irregularity.");
    	}
    	if (!id.matches("^\\d{1,20}$")) {
    		return JsonToString(objectNode, callback, "id irregularity.");
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
    		return JsonToString(objectNode, callback, "user irregularity.");
    	}
    	Result<Boolean> prodResult = productService.removeProductById(Long.parseLong(id));
    	if (prodResult.getCode()!=Result.Code.OK.value||!prodResult.getData()) {
    		return JsonToString(objectNode, callback, "remove product failed.");
    	}
		return JsonToString(userResult.getData(), objectNode, callback, "");
    }

    @RequestMapping("/product/increQuantity")
	public String increQuantity(
			@RequestParam(value = "callback", defaultValue = "") String callback,
			@RequestParam(value = "id", defaultValue = "") String id,
			@RequestParam(value = "quantity", defaultValue = "") String quantity,
			@RequestParam(value = "token", defaultValue = "") String token,
			@RequestParam(value = "session_id", defaultValue = "") String sessionId
						) {
    	logger.info(String.format("[%s][%s][%s][%s][%s]", callback, token, sessionId, id, quantity));

    	ObjectMapper objectMapper = new ObjectMapper();
    	ObjectNode objectNode = objectMapper.createObjectNode();
    	if (!sessionId.matches("^[0-9a-zA-Z]{32}$")) {
    		return JsonToString(objectNode, callback, "session_id irregularity.");
    	}
    	if (!id.matches("^\\d{1,20}$")) {
    		return JsonToString(objectNode, callback, "id irregularity.");
    	}
    	if (!quantity.matches("^\\-?\\d{1,20}$")) {
    		return JsonToString(objectNode, callback, "quantity irregularity.");
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
    		return JsonToString(objectNode, callback, "user irregularity.");
    	}
    	
    	Result<Long> prodResult = productService.increQuantity(Long.parseLong(id), Long.parseLong(quantity));
    	if (prodResult.getCode()!=Result.Code.OK.value) {
    		return JsonToString(objectNode, callback, "incre quantity for product failed.");
    	}
		objectNode.put("quantity", prodResult.getData());
		return JsonToString(userResult.getData(), objectNode, callback, "");
    }

    @RequestMapping("/product/query")
	public String query(
			@RequestParam(value = "callback", defaultValue = "") String callback,
			@RequestParam(value = "token", defaultValue = "") String token,
			@RequestParam(value = "session_id", defaultValue = "") String sessionId
						) {
    	logger.info(String.format("[%s][%s][%s]", callback, token, sessionId));

    	ObjectMapper objectMapper = new ObjectMapper();
    	ObjectNode objectNode = objectMapper.createObjectNode();
    	if (!sessionId.matches("^[0-9a-zA-Z]{32}$")) {
    		return JsonToString(objectNode, callback, "session_id irregularity.");
    	}
    	if (!token.matches("^token_[0-9a-zA-Z]{32}$")) {
    		return JsonToString(objectNode, callback, "token irregularity.");
    	}
    	Result<User> userResult = userService.getUserBySessionId(sessionId);
    	if (userResult.getCode()!=Result.Code.OK.value) {
    		return JsonToString(userResult.getData(), objectNode, callback, "user irregularity.");
    	}
    	User user = userResult.getData();

    	Result<List<Product>> queryResult = productService.queryProducts(new Product(), 0, 100);
    	if (queryResult.getCode()!=Result.Code.OK.value) {
    		return JsonToString(objectNode, callback, "products not exist.");
    	}
    	
		try {
			objectNode.put("products", objectMapper.writeValueAsString(queryResult.getData()));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return JsonToString(user, objectNode, callback, "");
    }

}
