package ecommerce.service.order;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Test;

import ecommerce.common.StringUtils;
import ecommerce.service.OrderServiceBootApplication;
import ecommerce.service.client.OrderService;
import ecommerce.service.client.ProductService;
import ecommerce.service.client.Result;
import ecommerce.service.client.TokenService;
import ecommerce.service.client.UserService;
import ecommerce.service.client.base.Order;
import ecommerce.service.client.base.Product;
import ecommerce.service.client.base.ShopCartOrder;
import ecommerce.service.client.base.User;
import ecommerce.service.order.client.OrderFlowService;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.test.context.ActiveProfiles;
@ActiveProfiles("local")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=OrderServiceBootApplication.class)
public class OrderFlowServiceTest {

    @Autowired
    private OrderFlowService orderFlowService;

    @Autowired
    private OrderService orderService;

    @Resource
    private UserService userService;

    @Resource
    private TokenService tokenService;

    @Resource
    private ProductService productService;

    @Test
    public void testCreateOrder() throws Exception {
    	ObjectMapper objectMapper = new ObjectMapper();
    	Product product1 = new Product();
    	product1.setName(""+System.currentTimeMillis());
    	product1.setPrice(11111l);
    	product1.setQuantity(11l);
    	product1.setCategoryId(1);
    	product1.setStatus(Product.Status.Actived.value);
    	Result<Product> result1 = productService.saveOrUpdateProduct(product1);
    	System.out.println("saveOrUpdateProduct:"+objectMapper.writeValueAsString(result1));
    	product1 = result1.getData();
    	Product product2 = new Product();
    	product2.setName(""+System.currentTimeMillis());
    	product2.setPrice(222222l);
    	product2.setQuantity(22l);
    	product2.setCategoryId(2);
    	product2.setStatus(Product.Status.Actived.value);
    	Result<Product> result2 = productService.saveOrUpdateProduct(product2);
    	System.out.println("saveOrUpdateProduct:"+objectMapper.writeValueAsString(result2));
    	product2 = result2.getData();

    	Result<String> tokenResult = tokenService.getToken();
    	System.out.println("getToken           :"+objectMapper.writeValueAsString(tokenResult));

    	User user = new User();
    	user.setUsername(""+System.currentTimeMillis());
    	user.setPassword(StringUtils.hex62EncodingWithRandom(32));
    	Result<User> userResult = userService.saveOrUpdateUser(user);
    	System.out.println("saveOrUpdateUser   :"+objectMapper.writeValueAsString(userResult));
    	user = userResult.getData();
    	
    	ShopCartOrder shopCartOrder = new ShopCartOrder();
    	shopCartOrder.setCreateTime(System.currentTimeMillis());
    	shopCartOrder.setUserId(user.getId());
    	shopCartOrder.setToken(tokenResult.getData());
    	shopCartOrder.setProductIds(Stream.of(product1.getId(), product2.getId()).collect(Collectors.toList()));
    	shopCartOrder.setQuantities(Stream.of(1l, 2l).collect(Collectors.toList()));

		Order createOrderResult = orderFlowService.createOrder(shopCartOrder);
    	Assert.assertNotNull(createOrderResult);
    	System.out.println("createOrder        :"+objectMapper.writeValueAsString(createOrderResult));
    	
    	Result<Order> mainOrder = orderService.getOrderById(createOrderResult.getId());
    	Assert.assertNotNull(mainOrder);
    	System.out.println("getOrderById       :"+objectMapper.writeValueAsString(mainOrder));

    	Order orderExample = new Order();
    	orderExample.setUserId(user.getId());
    	orderExample.setParentId(mainOrder.getData().getId());
    	Result<List<Order>> subOrderList = orderService.queryOrders(orderExample, 0, 1000);
    	subOrderList.getData().stream().forEach(order->{
    			try {
    				System.out.println("queryOrders        :"+objectMapper.writeValueAsString(order));
				} catch (Exception e) {
					e.printStackTrace();
				}
    	});
    }
    
    @Test
    public void testPayment() throws Exception {
    	ObjectMapper objectMapper = new ObjectMapper();
    	Product product1 = new Product();
    	product1.setName(""+System.currentTimeMillis());
    	product1.setPrice(11111l);
    	product1.setQuantity(11l);
    	product1.setCategoryId(1);
    	product1.setStatus(Product.Status.Actived.value);
    	Result<Product> result1 = productService.saveOrUpdateProduct(product1);
    	System.out.println("saveOrUpdateProduct:"+objectMapper.writeValueAsString(result1));
    	product1 = result1.getData();
    	Product product2 = new Product();
    	product2.setName(""+System.currentTimeMillis());
    	product2.setPrice(222222l);
    	product2.setQuantity(22l);
    	product2.setCategoryId(2);
    	product2.setStatus(Product.Status.Actived.value);
    	Result<Product> result2 = productService.saveOrUpdateProduct(product2);
    	System.out.println("saveOrUpdateProduct:"+objectMapper.writeValueAsString(result2));
    	product2 = result2.getData();

    	Result<String> tokenResult = tokenService.getToken();
    	System.out.println("getToken           :"+objectMapper.writeValueAsString(tokenResult));

    	User user = new User();
    	user.setUsername(""+System.currentTimeMillis());
    	user.setPassword(StringUtils.hex62EncodingWithRandom(32));
    	Result<User> userResult = userService.saveOrUpdateUser(user);
    	System.out.println("saveOrUpdateUser   :"+objectMapper.writeValueAsString(userResult));
    	user = userResult.getData();
    	
    	ShopCartOrder shopCartOrder = new ShopCartOrder();
    	shopCartOrder.setCreateTime(System.currentTimeMillis());
    	shopCartOrder.setUserId(user.getId());
    	shopCartOrder.setToken(tokenResult.getData());
    	shopCartOrder.setProductIds(Stream.of(product1.getId(), product2.getId()).collect(Collectors.toList()));
    	shopCartOrder.setQuantities(Stream.of(1l, 2l).collect(Collectors.toList()));

		Order createOrder = orderFlowService.createOrder(shopCartOrder);
    	Assert.assertNotNull(createOrder);
    	System.out.println("createOrder        :"+objectMapper.writeValueAsString(createOrder));

    	createOrder.setPaidAmount(createOrder.getReceiptAmount());
    	Order paymentOrder = orderFlowService.payment(createOrder);
    	Assert.assertNotNull(paymentOrder);
    	System.out.println("payment            :"+objectMapper.writeValueAsString(paymentOrder));

    	Order refundOrder = orderFlowService.refund(createOrder);
    	Assert.assertNotNull(refundOrder);
    	System.out.println("payment            :"+objectMapper.writeValueAsString(refundOrder));
    	
    	Result<Order> mainOrder = orderService.getOrderById(createOrder.getId());
    	Assert.assertNotNull(mainOrder);
    	System.out.println("getOrderById       :"+objectMapper.writeValueAsString(mainOrder));
    }
    @Test
    public void testPartialPayment() throws Exception {
    	ObjectMapper objectMapper = new ObjectMapper();
    	Product product1 = new Product();
    	product1.setName(""+System.currentTimeMillis());
    	product1.setPrice(11111l);
    	product1.setQuantity(11l);
    	product1.setCategoryId(1);
    	product1.setStatus(Product.Status.Actived.value);
    	Result<Product> result1 = productService.saveOrUpdateProduct(product1);
    	System.out.println("saveOrUpdateProduct:"+objectMapper.writeValueAsString(result1));
    	product1 = result1.getData();
    	Product product2 = new Product();
    	product2.setName(""+System.currentTimeMillis());
    	product2.setPrice(222222l);
    	product2.setQuantity(22l);
    	product2.setCategoryId(2);
    	product2.setStatus(Product.Status.Actived.value);
    	Result<Product> result2 = productService.saveOrUpdateProduct(product2);
    	System.out.println("saveOrUpdateProduct:"+objectMapper.writeValueAsString(result2));
    	product2 = result2.getData();

    	Result<String> tokenResult = tokenService.getToken();
    	System.out.println("getToken           :"+objectMapper.writeValueAsString(tokenResult));

    	User user = new User();
    	user.setUsername(""+System.currentTimeMillis());
    	user.setPassword(StringUtils.hex62EncodingWithRandom(32));
    	Result<User> userResult = userService.saveOrUpdateUser(user);
    	System.out.println("saveOrUpdateUser   :"+objectMapper.writeValueAsString(userResult));
    	user = userResult.getData();
    	
    	ShopCartOrder shopCartOrder = new ShopCartOrder();
    	shopCartOrder.setCreateTime(System.currentTimeMillis());
    	shopCartOrder.setUserId(user.getId());
    	shopCartOrder.setToken(tokenResult.getData());
    	shopCartOrder.setProductIds(Stream.of(product1.getId(), product2.getId()).collect(Collectors.toList()));
    	shopCartOrder.setQuantities(Stream.of(1l, 2l).collect(Collectors.toList()));

		Order createOrder = orderFlowService.createOrder(shopCartOrder);
    	Assert.assertNotNull(createOrder);
    	System.out.println("createOrder        :"+objectMapper.writeValueAsString(createOrder));

    	Order orderExample = new Order();
    	orderExample.setUserId(user.getId());
    	orderExample.setParentId(createOrder.getId());
    	Result<List<Order>> subOrderList = orderService.queryOrders(orderExample, 0, 1000);
    	Assert.assertNotNull(subOrderList);
    	subOrderList.getData().stream().forEach(order->{
    			try {
    				System.out.println("queryOrders        :"+objectMapper.writeValueAsString(order));
	    			order.setPaidAmount(order.getReceiptAmount());
	    			order = orderFlowService.payment(order);
	    	    	Assert.assertNotNull(order);
	    			System.out.println("payment            :"+objectMapper.writeValueAsString(order));
				} catch (Exception e) {
					e.printStackTrace();
				}
    	});

    	Result<Order> subOrder = orderService.getOrderById(subOrderList.getData().get(0).getId());
    	Assert.assertNotNull(subOrder);
    	System.out.println("getOrderById       :"+objectMapper.writeValueAsString(subOrder));
    	
    	Order refundOrder = orderFlowService.refund(subOrder.getData());
    	Assert.assertNotNull(refundOrder);
    	System.out.println("payment            :"+objectMapper.writeValueAsString(refundOrder));

    	subOrderList = orderService.queryOrders(orderExample, 0, 1000);
    	Assert.assertNotNull(subOrderList);
    	subOrderList.getData().stream().forEach(o->{
	    		try {
					System.out.println("payment            :"+objectMapper.writeValueAsString(o));
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}
			});
    }
    
    @Test
    public void testCompleteOrder() throws Exception {
    	ObjectMapper objectMapper = new ObjectMapper();
    	Product product1 = new Product();
    	product1.setName(""+System.currentTimeMillis());
    	product1.setPrice(11111l);
    	product1.setQuantity(11l);
    	product1.setCategoryId(1);
    	product1.setStatus(Product.Status.Actived.value);
    	Result<Product> result1 = productService.saveOrUpdateProduct(product1);
    	System.out.println("saveOrUpdateProduct:"+objectMapper.writeValueAsString(result1));
    	product1 = result1.getData();
    	Product product2 = new Product();
    	product2.setName(""+System.currentTimeMillis());
    	product2.setPrice(222222l);
    	product2.setQuantity(22l);
    	product2.setCategoryId(2);
    	product2.setStatus(Product.Status.Actived.value);
    	Result<Product> result2 = productService.saveOrUpdateProduct(product2);
    	System.out.println("saveOrUpdateProduct:"+objectMapper.writeValueAsString(result2));
    	product2 = result2.getData();

    	Result<String> tokenResult = tokenService.getToken();
    	System.out.println("getToken           :"+objectMapper.writeValueAsString(tokenResult));

    	User user = new User();
    	user.setUsername(""+System.currentTimeMillis());
    	user.setPassword(StringUtils.hex62EncodingWithRandom(32));
    	Result<User> userResult = userService.saveOrUpdateUser(user);
    	System.out.println("saveOrUpdateUser   :"+objectMapper.writeValueAsString(userResult));
    	user = userResult.getData();
    	
    	ShopCartOrder shopCartOrder = new ShopCartOrder();
    	shopCartOrder.setCreateTime(System.currentTimeMillis());
    	shopCartOrder.setUserId(user.getId());
    	shopCartOrder.setToken(tokenResult.getData());
    	shopCartOrder.setProductIds(Stream.of(product1.getId(), product2.getId()).collect(Collectors.toList()));
    	shopCartOrder.setQuantities(Stream.of(1l, 2l).collect(Collectors.toList()));

		Order createOrder = orderFlowService.createOrder(shopCartOrder);
    	Assert.assertNotNull(createOrder);
    	System.out.println("createOrder        :"+objectMapper.writeValueAsString(createOrder));

    	createOrder.setPaidAmount(createOrder.getReceiptAmount());
    	Order paymentOrder = orderFlowService.payment(createOrder);
    	Assert.assertNotNull(paymentOrder);
    	System.out.println("payment            :"+objectMapper.writeValueAsString(paymentOrder));

    	Result<Order> mainOrder = orderService.getOrderById(createOrder.getId());
    	Assert.assertNotNull(mainOrder);
    	System.out.println("getOrderById       :"+objectMapper.writeValueAsString(mainOrder));
    	
		Order completedOrder = orderFlowService.complete(createOrder);
    	Assert.assertNotNull(completedOrder);
    	System.out.println("complete           :"+objectMapper.writeValueAsString(completedOrder));

    	try {
			completedOrder = orderFlowService.complete(createOrder);
	    	Assert.assertNotNull(completedOrder);
	    	System.out.println("complete           :"+objectMapper.writeValueAsString(completedOrder));
		}catch(Exception e) {
			e.printStackTrace();
		}

    	try {
	    	Order closedOrder = orderFlowService.close(createOrder);
	    	Assert.assertNotNull(closedOrder);
	    	System.out.println("close              :"+objectMapper.writeValueAsString(closedOrder));
		}catch(Exception e) {
			e.printStackTrace();
		}
    	Order orderExample = new Order();
    	orderExample.setUserId(user.getId());
    	orderExample.setParentId(mainOrder.getData().getId());
    	Result<List<Order>> subOrderList = orderService.queryOrders(orderExample, 0, 1000);
    	subOrderList.getData().stream().forEach(order->{
    			try {
    				System.out.println("queryOrders        :"+objectMapper.writeValueAsString(order));
				} catch (Exception e) {
					e.printStackTrace();
				}
    	});
    }
    
    @Test
    public void testPartialCompleteOrder() throws Exception {
    	ObjectMapper objectMapper = new ObjectMapper();
    	Product product1 = new Product();
    	product1.setName(""+System.currentTimeMillis());
    	product1.setPrice(11111l);
    	product1.setQuantity(11l);
    	product1.setCategoryId(1);
    	product1.setStatus(Product.Status.Actived.value);
    	Result<Product> result1 = productService.saveOrUpdateProduct(product1);
    	System.out.println("saveOrUpdateProduct:"+objectMapper.writeValueAsString(result1));
    	product1 = result1.getData();
    	Product product2 = new Product();
    	product2.setName(""+System.currentTimeMillis());
    	product2.setPrice(222222l);
    	product2.setQuantity(22l);
    	product2.setCategoryId(2);
    	product2.setStatus(Product.Status.Actived.value);
    	Result<Product> result2 = productService.saveOrUpdateProduct(product2);
    	System.out.println("saveOrUpdateProduct:"+objectMapper.writeValueAsString(result2));
    	product2 = result2.getData();

    	Result<String> tokenResult = tokenService.getToken();
    	System.out.println("getToken           :"+objectMapper.writeValueAsString(tokenResult));

    	User user = new User();
    	user.setUsername(""+System.currentTimeMillis());
    	user.setPassword(StringUtils.hex62EncodingWithRandom(32));
    	Result<User> userResult = userService.saveOrUpdateUser(user);
    	System.out.println("saveOrUpdateUser   :"+objectMapper.writeValueAsString(userResult));
    	user = userResult.getData();
    	
    	ShopCartOrder shopCartOrder = new ShopCartOrder();
    	shopCartOrder.setCreateTime(System.currentTimeMillis());
    	shopCartOrder.setUserId(user.getId());
    	shopCartOrder.setToken(tokenResult.getData());
    	shopCartOrder.setProductIds(Stream.of(product1.getId(), product2.getId()).collect(Collectors.toList()));
    	shopCartOrder.setQuantities(Stream.of(1l, 2l).collect(Collectors.toList()));

		Order createOrder = orderFlowService.createOrder(shopCartOrder);
    	Assert.assertNotNull(createOrder);
    	System.out.println("createOrder        :"+objectMapper.writeValueAsString(createOrder));

    	Order orderExample = new Order();
    	orderExample.setUserId(user.getId());
    	orderExample.setParentId(createOrder.getId());
    	Result<List<Order>> subOrderList = orderService.queryOrders(orderExample, 0, 1000);

    	Order subPayOrder = subOrderList.getData().get(0);

    	subPayOrder.setPaidAmount(subPayOrder.getReceiptAmount());
    	Order paymentOrder = orderFlowService.payment(subPayOrder);
    	Assert.assertNotNull(paymentOrder);
    	System.out.println("payment            :"+objectMapper.writeValueAsString(paymentOrder));

    	Result<Order> mainOrder = orderService.getOrderById(createOrder.getId());
    	Assert.assertNotNull(mainOrder);
    	System.out.println("getOrderById       :"+objectMapper.writeValueAsString(mainOrder));
    	
    	try {
    		Order completedOrder = orderFlowService.complete(paymentOrder);
	    	Assert.assertNotNull(completedOrder);
	    	System.out.println("complete           :"+objectMapper.writeValueAsString(completedOrder));
		}catch(Exception e) {
			e.printStackTrace();
		}

    	try {
	    	Order closedOrder = orderFlowService.close(paymentOrder);
	    	Assert.assertNotNull(closedOrder);
	    	System.out.println("close              :"+objectMapper.writeValueAsString(closedOrder));
		}catch(Exception e) {
			e.printStackTrace();
		}
    	try {
	    	Order closedOrder = orderFlowService.close(subOrderList.getData().get(1));
	    	Assert.assertNotNull(closedOrder);
	    	System.out.println("close              :"+objectMapper.writeValueAsString(closedOrder));
		}catch(Exception e) {
			e.printStackTrace();
		}
    	
    	subOrderList = orderService.queryOrders(orderExample, 0, 1000);
    	subOrderList.getData().stream().forEach(order->{
    			try {
    				System.out.println("queryOrders        :"+objectMapper.writeValueAsString(order));
				} catch (Exception e) {
					e.printStackTrace();
				}
    	});
    }
    
	@Test
    public void testCloseOrder() throws Exception {
    	ObjectMapper objectMapper = new ObjectMapper();
    	Product product1 = new Product();
    	product1.setName(""+System.currentTimeMillis());
    	product1.setPrice(11111l);
    	product1.setQuantity(11l);
    	product1.setCategoryId(1);
    	product1.setStatus(Product.Status.Actived.value);
    	Result<Product> result1 = productService.saveOrUpdateProduct(product1);
    	System.out.println("saveOrUpdateProduct:"+objectMapper.writeValueAsString(result1));
    	product1 = result1.getData();
    	Product product2 = new Product();
    	product2.setName(""+System.currentTimeMillis());
    	product2.setPrice(222222l);
    	product2.setQuantity(22l);
    	product2.setCategoryId(2);
    	product2.setStatus(Product.Status.Actived.value);
    	Result<Product> result2 = productService.saveOrUpdateProduct(product2);
    	System.out.println("saveOrUpdateProduct:"+objectMapper.writeValueAsString(result2));
    	product2 = result2.getData();

    	Result<String> tokenResult = tokenService.getToken();
    	System.out.println("getToken           :"+objectMapper.writeValueAsString(tokenResult));

    	User user = new User();
    	user.setUsername(""+System.currentTimeMillis());
    	user.setPassword(StringUtils.hex62EncodingWithRandom(32));
    	Result<User> userResult = userService.saveOrUpdateUser(user);
    	System.out.println("saveOrUpdateUser   :"+objectMapper.writeValueAsString(userResult));
    	user = userResult.getData();
    	
    	ShopCartOrder shopCartOrder = new ShopCartOrder();
    	shopCartOrder.setCreateTime(System.currentTimeMillis());
    	shopCartOrder.setUserId(user.getId());
    	shopCartOrder.setToken(tokenResult.getData());
    	shopCartOrder.setProductIds(Stream.of(product1.getId(), product2.getId()).collect(Collectors.toList()));
    	shopCartOrder.setQuantities(Stream.of(1l, 2l).collect(Collectors.toList()));

		Order createOrder = orderFlowService.createOrder(shopCartOrder);
    	Assert.assertNotNull(createOrder);
    	System.out.println("createOrder        :"+objectMapper.writeValueAsString(createOrder));

    	createOrder.setPaidAmount(createOrder.getReceiptAmount());
    	Order paymentOrder = orderFlowService.payment(createOrder);
    	Assert.assertNotNull(paymentOrder);
    	System.out.println("payment            :"+objectMapper.writeValueAsString(paymentOrder));

    	Result<Order> mainOrder = orderService.getOrderById(createOrder.getId());
    	Assert.assertNotNull(mainOrder);
    	System.out.println("getOrderById       :"+objectMapper.writeValueAsString(mainOrder));
    	
		Order completedOrder = orderFlowService.close(createOrder);
    	Assert.assertNotNull(completedOrder);
    	System.out.println("complete           :"+objectMapper.writeValueAsString(completedOrder));

    	try {
			completedOrder = orderFlowService.complete(createOrder);
	    	Assert.assertNotNull(completedOrder);
	    	System.out.println("complete           :"+objectMapper.writeValueAsString(completedOrder));
		}catch(Exception e) {
			e.printStackTrace();
		}

    	try {
	    	Order closedOrder = orderFlowService.close(createOrder);
	    	Assert.assertNotNull(closedOrder);
	    	System.out.println("close              :"+objectMapper.writeValueAsString(closedOrder));
		}catch(Exception e) {
			e.printStackTrace();
		}
    	Order orderExample = new Order();
    	orderExample.setUserId(user.getId());
    	orderExample.setParentId(mainOrder.getData().getId());
    	Result<List<Order>> subOrderList = orderService.queryOrders(orderExample, 0, 1000);
    	subOrderList.getData().stream().forEach(order->{
    			try {
    				System.out.println("queryOrders        :"+objectMapper.writeValueAsString(order));
				} catch (Exception e) {
					e.printStackTrace();
				}
    	});
    }
    
    @Test
    public void testPartialCloseOrder() throws Exception {
    	ObjectMapper objectMapper = new ObjectMapper();
    	Product product1 = new Product();
    	product1.setName(""+System.currentTimeMillis());
    	product1.setPrice(11111l);
    	product1.setQuantity(11l);
    	product1.setCategoryId(1);
    	product1.setStatus(Product.Status.Actived.value);
    	Result<Product> result1 = productService.saveOrUpdateProduct(product1);
    	System.out.println("saveOrUpdateProduct:"+objectMapper.writeValueAsString(result1));
    	product1 = result1.getData();
    	Product product2 = new Product();
    	product2.setName(""+System.currentTimeMillis());
    	product2.setPrice(222222l);
    	product2.setQuantity(22l);
    	product2.setCategoryId(2);
    	product2.setStatus(Product.Status.Actived.value);
    	Result<Product> result2 = productService.saveOrUpdateProduct(product2);
    	System.out.println("saveOrUpdateProduct:"+objectMapper.writeValueAsString(result2));
    	product2 = result2.getData();

    	Result<String> tokenResult = tokenService.getToken();
    	System.out.println("getToken           :"+objectMapper.writeValueAsString(tokenResult));

    	User user = new User();
    	user.setUsername(""+System.currentTimeMillis());
    	user.setPassword(StringUtils.hex62EncodingWithRandom(32));
    	Result<User> userResult = userService.saveOrUpdateUser(user);
    	System.out.println("saveOrUpdateUser   :"+objectMapper.writeValueAsString(userResult));
    	user = userResult.getData();
    	
    	ShopCartOrder shopCartOrder = new ShopCartOrder();
    	shopCartOrder.setCreateTime(System.currentTimeMillis());
    	shopCartOrder.setUserId(user.getId());
    	shopCartOrder.setToken(tokenResult.getData());
    	shopCartOrder.setProductIds(Stream.of(product1.getId(), product2.getId()).collect(Collectors.toList()));
    	shopCartOrder.setQuantities(Stream.of(1l, 2l).collect(Collectors.toList()));

		Order createOrder = orderFlowService.createOrder(shopCartOrder);
    	Assert.assertNotNull(createOrder);
    	System.out.println("createOrder        :"+objectMapper.writeValueAsString(createOrder));

    	Order orderExample = new Order();
    	orderExample.setUserId(user.getId());
    	orderExample.setParentId(createOrder.getId());
    	Result<List<Order>> subOrderList = orderService.queryOrders(orderExample, 0, 1000);

    	Order subPayOrder1 = subOrderList.getData().get(0);
    	subPayOrder1.setPaidAmount(subPayOrder1.getReceiptAmount());
    	Order paymentOrder1 = orderFlowService.payment(subPayOrder1);
    	Assert.assertNotNull(paymentOrder1);
    	System.out.println("payment            :"+objectMapper.writeValueAsString(paymentOrder1));
    	
    	Order subPayOrder2 = subOrderList.getData().get(1);
    	subPayOrder2.setPaidAmount(subPayOrder2.getReceiptAmount());
    	Order paymentOrder2 = orderFlowService.payment(subPayOrder2);
    	Assert.assertNotNull(paymentOrder2);
    	System.out.println("payment            :"+objectMapper.writeValueAsString(paymentOrder2));

    	Result<Order> mainOrder = orderService.getOrderById(createOrder.getId());
    	Assert.assertNotNull(mainOrder);
    	System.out.println("getOrderById       :"+objectMapper.writeValueAsString(mainOrder));
    	
    	try {
    		Order closedOrder = orderFlowService.close(paymentOrder1);
	    	Assert.assertNotNull(closedOrder);
	    	System.out.println("close              :"+objectMapper.writeValueAsString(closedOrder));
		}catch(Exception e) {
			e.printStackTrace();
		}

    	try {
	    	Order closedOrder = orderFlowService.complete(paymentOrder1);
	    	Assert.assertNotNull(closedOrder);
	    	System.out.println("complete           :"+objectMapper.writeValueAsString(closedOrder));
		}catch(Exception e) {
			e.printStackTrace();
		}
    	try {
	    	Order closedOrder = orderFlowService.close(paymentOrder2);
	    	Assert.assertNotNull(closedOrder);
	    	System.out.println("close              :"+objectMapper.writeValueAsString(closedOrder));
		}catch(Exception e) {
			e.printStackTrace();
		}
    	try {
	    	Order completedOrder = orderFlowService.complete(createOrder);
	    	Assert.assertNotNull(completedOrder);
	    	System.out.println("complete           :"+objectMapper.writeValueAsString(completedOrder));
		}catch(Exception e) {
			e.printStackTrace();
		}
    	
    	subOrderList = orderService.queryOrders(orderExample, 0, 1000);
    	subOrderList.getData().stream().forEach(order->{
    			try {
    				System.out.println("queryOrders        :"+objectMapper.writeValueAsString(order));
				} catch (Exception e) {
					e.printStackTrace();
				}
    	});
    }

}
