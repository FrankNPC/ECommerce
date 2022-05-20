package ecommerce.service.product;


//import ecommerce.service.ProductServiceBootApplication;
//import ecommerce.service.client.base.Product;
//import ecommerce.service.product.dao.ProductDAOByCassandra;
//
//import java.util.List;
//
//import javax.annotation.Resource;
//
//import org.junit.Assert;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//
//import com.datastax.driver.core.Cluster;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringBootTest(classes=ProductServiceBootApplication.class)
//public class CassandraTest {
//	
//	public static void main(String[] args) {
//		Cluster.builder().addContactPoint("127.0.0.1").build().connect()
//		.execute("CREATE KEYSPACE IF NOT EXISTS ecommerce WITH REPLICATION = {'class': 'SimpleStrategy','replication_factor' : 1};");
//	}
//
//	@Resource
//	private ProductDAOByCassandra productDAO;
//	@Test
//	public void testCassandraByProduct() throws Exception {
//		
//		ObjectMapper objectMapper = new ObjectMapper();
//		Product product = new Product();
//		product.setId(System.currentTimeMillis());
//		product.setName(""+System.currentTimeMillis());
//		product.setPrice(11111l);
//		product.setQuantity(11l);
//		product.setCategoryId(1);
//		product.setStatus(Product.Status.Actived.value);
//
//		Assert.assertTrue(productDAO.insert(product));
//
//		product.setName(""+System.currentTimeMillis());
//		product.setPrice(22222l);
//		product.setQuantity(22l);
//		product.setCategoryId(2);
//		product.setStatus(Product.Status.Invalid.value);
//		Assert.assertTrue(productDAO.update(product));
//		
//		Assert.assertTrue(productDAO.updateQuantity(product.getId(), 5));
//
//		product = productDAO.getProductById(product.getId());
//		Assert.assertNotNull(product);
//		System.out.println("getProductById		   :"+objectMapper.writeValueAsString(product));
//
//		List<Product> prodList = productDAO.queryProducts(null, 0, 2);
//		Assert.assertNotNull(prodList);
//		prodList.stream().forEach(p->{
//			try {
//				System.out.println("queryProducts			:"+objectMapper.writeValueAsString(p));
//			} catch (JsonProcessingException e) {
//				e.printStackTrace();
//			}
//		});
//
//		List<Product> prodList1 = productDAO.queryProducts(null, 2, 3);
//		Assert.assertNotNull(prodList1);
//		prodList1.stream().forEach(p->{
//			try {
//				System.out.println("queryProducts			:"+objectMapper.writeValueAsString(p));
//			} catch (JsonProcessingException e) {
//				e.printStackTrace();
//			}
//		});
//		List<Product> prodList12 = productDAO.queryProducts(null, 3, 5);
//		Assert.assertNotNull(prodList12);
//		prodList12.stream().forEach(p->{
//			try {
//				System.out.println("queryProducts			:"+objectMapper.writeValueAsString(p));
//			} catch (JsonProcessingException e) {
//				e.printStackTrace();
//			}
//		});
//		List<Product> prodList3 = productDAO.queryProducts(null, 0, 20);
//		Assert.assertNotNull(prodList3);
//		prodList3.stream().forEach(p->{
//			try {
//				System.out.println("queryProducts			:"+objectMapper.writeValueAsString(p));
//			} catch (JsonProcessingException e) {
//				e.printStackTrace();
//			}
//		});
//	}
//
//}
