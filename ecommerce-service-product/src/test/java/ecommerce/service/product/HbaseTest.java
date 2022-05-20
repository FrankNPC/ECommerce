package ecommerce.service.product;


import ecommerce.service.ProductServiceBootApplication;
import ecommerce.service.product.dao.HbaseTemplate;

import java.util.List;

import javax.annotation.Resource;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=ProductServiceBootApplication.class)
public class HbaseTest {

	private static final String ProductTableName = "products";
	private static final String ProductFamilyName = "name_description";
	private static final String[] ProductTableColumns = { "name", "description" };
	@Resource
	private HbaseTemplate hbaseTemplate;
//	@Test
	public void testHbase() throws Exception {
//		deleteTable(ProductTableName);
//		createTableWithFamilies(ProductTableName, ProductFamilyName);
		hbaseTemplate.saveOrUpdateByRowKey(ProductTableName, "product_1", ProductFamilyName, ProductTableColumns[0], "product_name"+System.currentTimeMillis());
		
		String value = hbaseTemplate.getByRowKey(ProductTableName, "product_1", ProductFamilyName, ProductTableColumns[0]);
		System.out.println(value);
		
		value = hbaseTemplate.getByRowKey(ProductTableName, "product_1", ProductFamilyName, ProductTableColumns[1]);
		System.out.println(value);
		
		List<String> valueResults = hbaseTemplate.queryByRowKey(ProductTableName, "product_1", ProductFamilyName, ProductTableColumns[0]);
		valueResults.stream().forEach(System.out::println);
	}

}
