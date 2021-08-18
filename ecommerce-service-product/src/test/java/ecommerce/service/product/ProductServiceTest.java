package ecommerce.service.product;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

import ecommerce.service.ProductServiceBootApplication;
import ecommerce.service.client.ProductService;
import ecommerce.service.client.Result;
import ecommerce.service.client.base.Product;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=ProductServiceBootApplication.class)
public class ProductServiceTest {

    @Autowired
    private ProductService productService;
    
//    @Autowired
//    private ProductDAOFromMySQL productDAO;
//    @Test
//    public void testInitiator() throws Exception {
//    	productDAO.Initiator();
//    }
    
    @Test
    public void testInsert() throws Exception {
    	ObjectMapper objectMapper = new ObjectMapper();
    	Product product = new Product();
    	product.setName(""+System.currentTimeMillis());
    	product.setPrice(11111l);
    	product.setQuantity(11l);
    	product.setCategoryId(1);
    	product.setStatus(Product.Status.Actived.value);
    	Result<Product> result = productService.saveOrUpdateProduct(product);
    	Assert.assertNotNull(result.getData());
    	System.out.println("saveOrUpdateProduct:"+objectMapper.writeValueAsString(result));
    	
    	Result<Product> getResult = productService.getProductById(result.getData().getId());
    	Assert.assertNotNull(getResult.getData());
    	System.out.println("getProductById     :"+objectMapper.writeValueAsString(getResult));
    	
    	product = getResult.getData();
    	product.setName(UUID.randomUUID().toString()+product.getId());
    	product.setPrice(888888l);
    	product.setQuantity(88l);
    	product.setCategoryId(8);
    	product.setStatus(Product.Status.Invalid.value);
    	Result<Product> updateResult = productService.saveOrUpdateProduct(product);
    	Assert.assertNotNull(updateResult.getData());
    	System.out.println("saveOrUpdateProduct:"+objectMapper.writeValueAsString(updateResult));

    	Result<Product> afterUpdateResult = productService.getProductById(product.getId());
    	Assert.assertNotNull(afterUpdateResult.getData());
    	System.out.println("getProductById     :"+objectMapper.writeValueAsString(afterUpdateResult));
    	
    	Result<Boolean> removeResult = productService.removeProductById(product.getId());
    	Assert.assertNotNull(removeResult.getData());
    	System.out.println("removeProductById  :"+objectMapper.writeValueAsString(removeResult));
    	
    	Result<Product> reGetResult = productService.getProductById(product.getId());
    	Assert.assertTrue(reGetResult.getData().getStatus()==Product.Status.Invalid.value);
    	System.out.println("getProductById     :"+objectMapper.writeValueAsString(reGetResult));
    	
    	Result<List<Product>> queryResult = productService.queryProducts(new Product(), 0, 100);
    	Assert.assertNotNull(queryResult.getData());
    	queryResult.getData().stream().forEach(e->{
        	try {
				System.out.println("queryProducts      :"+objectMapper.writeValueAsString(e));
			} catch (JsonProcessingException e1) {
				e1.printStackTrace();
			}
    	});
    }
    
    @Test
    public void testBatchQuantity() throws Exception {
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

    	Result<List<Long>> increResult = productService.increBatchQuantity(
    			Stream.of(product1.getId(), product2.getId()).collect(Collectors.toList()), 
    			Stream.of(-1l, -2l).collect(Collectors.toList()));
    	Assert.assertNotNull(increResult.getData());
    	System.out.println("increBatchQuantity  :"+objectMapper.writeValueAsString(increResult));
    }
    
    @Test
    public void testQuantity() throws Exception {
    	ObjectMapper objectMapper = new ObjectMapper();
    	Product product = new Product();
    	product.setName(""+System.currentTimeMillis());
    	product.setPrice(22222l);
    	product.setQuantity(22l);
    	product.setCategoryId(2);
    	product.setStatus(Product.Status.Actived.value);
    	Result<Product> result = productService.saveOrUpdateProduct(product);
    	Assert.assertNotNull(result.getData());
    	System.out.println("saveOrUpdateProduct:"+objectMapper.writeValueAsString(result));

    	Result<Long> increResult = productService.increQuantity(result.getData().getId(), 1);
    	Assert.assertNotNull(increResult.getData());
    	System.out.println("increQuantity      :"+objectMapper.writeValueAsString(increResult));

    	Result<Product> getResult = productService.getProductById(product.getId());
    	Assert.assertNotNull(getResult.getData());
    	System.out.println("getProductById     :"+objectMapper.writeValueAsString(getResult));
    	
    	Result<Long> syncResult = productService.flushQuantity(result.getData().getId());
    	Assert.assertNotNull(syncResult.getData());
    	System.out.println("flushQuantity      :"+objectMapper.writeValueAsString(syncResult));
    	
    	Result<Product> reGetResult = productService.getProductById(product.getId());
    	Assert.assertNotNull(reGetResult.getData());
    	System.out.println("getProductById     :"+objectMapper.writeValueAsString(reGetResult));

    	Result<Long> decreResult = productService.increQuantity(result.getData().getId(), -5);
    	Assert.assertNotNull(decreResult.getData());
    	System.out.println("increQuantity      :"+objectMapper.writeValueAsString(decreResult));

    	Result<Long> re2GetQuantResult = productService.flushQuantity(product.getId());
    	Assert.assertNotNull(re2GetQuantResult.getData());
    	System.out.println("flushQuantity      :"+objectMapper.writeValueAsString(re2GetQuantResult));
    }

}
