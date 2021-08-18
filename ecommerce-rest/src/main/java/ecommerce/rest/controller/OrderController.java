package ecommerce.rest.controller;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

import ecommerce.service.client.OrderService;
import ecommerce.service.client.Result;
import ecommerce.service.client.TokenService;
import ecommerce.service.client.UserService;
import ecommerce.service.client.base.Order;
import ecommerce.service.client.base.ShopCartOrder;
import ecommerce.service.client.base.User;

@RestController
@CrossOrigin
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
	
    @Resource
	private TokenService tokenService;

    @Resource
	private OrderService orderService;

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
    
    @RequestMapping("/order/create")
	public String create(
			@RequestParam(value = "callback", defaultValue = "") String callback,
			@RequestParam(value = "session_id", defaultValue = "") String sessionId,
			@RequestParam(value = "productIds", defaultValue = "") String productIds,
			@RequestParam(value = "quantities", defaultValue = "") String quantities,
			@RequestParam(value = "token", defaultValue = "") String token
						) {
    	logger.info(String.format("[%s][%s][%s][%s][%s]", callback, token, sessionId, productIds, quantities));
    
    	ObjectMapper objectMapper = new ObjectMapper();
    	ObjectNode objectNode = objectMapper.createObjectNode();
    	if (!sessionId.matches("^[0-9a-zA-Z]{32}$")) {
    		return JsonToString(objectNode, callback, "session_id irregularity.");
    	}
    	if (!productIds.matches("^\\d+[0-9,]{1,20}$")) {
    		return JsonToString(objectNode, callback, "productIds irregularity.");
    	}
    	if (!quantities.matches("^\\d+[0-9,]{1,20}$")) {
    		return JsonToString(objectNode, callback, "quantities irregularity.");
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
    	
    	List<Long> prodIdList = Arrays.stream(productIds.split(",")).map(str->Long.parseLong(str)).collect(Collectors.toList());
    	List<Long> quantiList = Arrays.stream(quantities.split(",")).map(str->Long.parseLong(str)).collect(Collectors.toList());

    	ShopCartOrder shopCartOrder = new ShopCartOrder();
    	shopCartOrder.setToken(token);
    	shopCartOrder.setUserId(user.getId());
    	shopCartOrder.setProductIds(prodIdList);
    	shopCartOrder.setQuantities(quantiList);
    	Result<Boolean> createOrderResult = orderService.createOrder(shopCartOrder);
    	if (createOrderResult.getCode()!=Result.Code.OK.value||!createOrderResult.getData()) {
    		return JsonToString(user, objectNode, callback, "create order failed.");
    	}
		return JsonToString(user, objectNode, callback, "");
    }

    @RequestMapping("/order/query")
	public String query(
			@RequestParam(value = "callback", defaultValue = "") String callback,
			@RequestParam(value = "session_id", defaultValue = "") String sessionId,
			@RequestParam(value = "productId", defaultValue = "") String productId,
			@RequestParam(value = "status", defaultValue = "") String status,
			@RequestParam(value = "token", defaultValue = "") String token
						) {
    	logger.info(String.format("[%s][%s][%s][%s][%s]", callback, token, sessionId, productId, status));
    
    	ObjectMapper objectMapper = new ObjectMapper();
    	ObjectNode objectNode = objectMapper.createObjectNode();
    	if (!sessionId.matches("^[0-9a-zA-Z]{32}$")) {
    		return JsonToString(objectNode, callback, "session_id irregularity.");
    	}
    	if (!productId.isEmpty()&&!productId.matches("^{1,20}$")) {
    		return JsonToString(objectNode, callback, "productId irregularity.");
    	}
    	if (!status.isEmpty()&&!status.matches("^{1,11}$")) {
    		return JsonToString(objectNode, callback, "status irregularity.");
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
    	
    	Order orderExample = new Order();
    	orderExample.setProductId(productId.isEmpty()?null:Long.parseLong(productId));
    	orderExample.setStatus(status.isEmpty()?null:Integer.parseInt(status));
    	orderExample.setUserId(user.getId());
    	Result<List<Order>> queryOrderResult = orderService.queryOrders(orderExample, 0, 100);
    	if (queryOrderResult.getCode()!=Result.Code.OK.value) {
    		return JsonToString(user, objectNode, callback, "query order failed.");
    	}
    	try {
			objectNode.put("orders", objectMapper.writeValueAsString(queryOrderResult.getData()));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return JsonToString(user, objectNode, callback, "");
    }

    @RequestMapping("/order/get")
	public String get(
			@RequestParam(value = "callback", defaultValue = "") String callback,
			@RequestParam(value = "session_id", defaultValue = "") String sessionId,
			@RequestParam(value = "id", defaultValue = "") String id,
			@RequestParam(value = "token", defaultValue = "") String token
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
    	User user = userResult.getData();
    	
    	Result<Order> orderResult = orderService.getOrderByIdAndUserId(Long.parseLong(id), user.getId());
    	if (orderResult.getCode()!=Result.Code.OK.value) {
    		return JsonToString(user, objectNode, callback, "get order failed.");
    	}
    	try {
			objectNode.put("order", objectMapper.writeValueAsString(orderResult.getData()));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return JsonToString(user, objectNode, callback, "");
    }

    @RequestMapping("/order/payment")
	public String payment(
			@RequestParam(value = "callback", defaultValue = "") String callback,
			@RequestParam(value = "id", defaultValue = "") String id,
			@RequestParam(value = "external_order_id", defaultValue = "") String externalOrderId,
			@RequestParam(value = "token", defaultValue = "") String token
						) {
    	logger.info(String.format("[%s][%s][%s][%s]", callback, token, externalOrderId, id));

    	ObjectMapper objectMapper = new ObjectMapper();
    	ObjectNode objectNode = objectMapper.createObjectNode();
    	if (!id.matches("^\\d{1,20}$")) {
    		return JsonToString(objectNode, callback, "id irregularity.");
    	}
    	if (!externalOrderId.matches("^[0-9a-zA-Z]{32}$")) {
    		return JsonToString(objectNode, callback, "externalOrderId irregularity.");
    	}
    	if (!token.matches("^token_[0-9a-zA-Z]{32}$")) {
    		return JsonToString(objectNode, callback, "token irregularity.");
    	}
    	Result<Boolean> tokenResult = tokenService.verifyToken(token);
    	if (tokenResult.getCode()!=Result.Code.OK.value) {
    		return JsonToString(objectNode, callback, "token invalid.");
    	}
    	
    	Result<Order> orderResult = orderService.getOrderById(Long.parseLong(id));
    	if (orderResult.getCode()!=Result.Code.OK.value||orderResult.getData().getStatus()!=Order.Status.Actived.value) {
    		return JsonToString(objectNode, callback, "order can't pay.");
    	}
    	Order order = orderResult.getData();
    	order.setExternalOrderId(externalOrderId);
    	Result<Boolean> payResult = orderService.payment(order);
    	if (payResult.getCode()!=Result.Code.OK.value||!payResult.getData()) {
    		return JsonToString(objectNode, callback, "pay order failed.");
    	}
		return JsonToString(objectNode, callback, "");
    }

    @RequestMapping("/order/refund")
	public String refund(
			@RequestParam(value = "callback", defaultValue = "") String callback,
			@RequestParam(value = "session_id", defaultValue = "") String sessionId,
			@RequestParam(value = "id", defaultValue = "") String id,
			@RequestParam(value = "token", defaultValue = "") String token
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
    	User user = userResult.getData();

    	Result<Order> orderResult = orderService.getOrderByIdAndUserId(Long.parseLong(id), user.getId());
    	if (orderResult.getCode()!=Result.Code.OK.value) {
    		return JsonToString(user, objectNode, callback, "get order failed.");
    	}
    	
    	Result<Boolean> refundResult = orderService.refund(orderResult.getData());
    	if (refundResult.getCode()!=Result.Code.OK.value||!refundResult.getData()) {
    		return JsonToString(user, objectNode, callback, "refund order failed.");
    	}
		return JsonToString(user, objectNode, callback, "");
    }
    @RequestMapping("/order/close")
	public String close(
			@RequestParam(value = "callback", defaultValue = "") String callback,
			@RequestParam(value = "session_id", defaultValue = "") String sessionId,
			@RequestParam(value = "id", defaultValue = "") String id,
			@RequestParam(value = "token", defaultValue = "") String token
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
    	User user = userResult.getData();

    	Result<Order> orderResult = orderService.getOrderByIdAndUserId(Long.parseLong(id), user.getId());
    	if (orderResult.getCode()!=Result.Code.OK.value) {
    		return JsonToString(user, objectNode, callback, "get order failed.");
    	}
    	
    	Result<Boolean> closeResult = orderService.close(orderResult.getData());
    	if (closeResult.getCode()!=Result.Code.OK.value||!closeResult.getData()) {
    		return JsonToString(user, objectNode, callback, "close order failed.");
    	}
		return JsonToString(user, objectNode, callback, "");
    }
    
}
