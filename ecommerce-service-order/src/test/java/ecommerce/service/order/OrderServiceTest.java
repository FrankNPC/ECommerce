package ecommerce.service.order;

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
import ecommerce.service.client.base.Product;
import ecommerce.service.client.base.ShopCartOrder;
import ecommerce.service.client.base.User;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.test.context.ActiveProfiles;
@ActiveProfiles("local")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=OrderServiceBootApplication.class)
public class OrderServiceTest {

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
    	System.out.println("saveOrUpdateProduct:"+objectMapper.writeValueAsString(userResult));
    	user = userResult.getData();
    	
    	ShopCartOrder shopCartOrder = new ShopCartOrder();
    	shopCartOrder.setUserId(user.getId());
    	shopCartOrder.setToken(tokenResult.getData());
    	shopCartOrder.setProductIds(Stream.of(product1.getId(), product2.getId()).collect(Collectors.toList()));
    	shopCartOrder.setQuantities(Stream.of(1l, 2l).collect(Collectors.toList()));

		Result<Boolean> createOrderResult = orderService.createOrder(shopCartOrder);
    	System.out.println("saveOrUpdateProduct:"+objectMapper.writeValueAsString(createOrderResult));
    	Assert.assertNotNull(createOrderResult.getData());
    }

}
