package ecommerce.service.product.dao;

import java.io.IOException;
import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import ecommerce.service.client.base.Product;

@Mapper  
public interface ProductDAOByMySQL {

	static final String HbaseProductTableName = "products";
	static final String HbaseProductFamilyName = "name_description";
	static final String[] HbaseProductTableColumns = { "name", "description" };

	public default void Initiator(HbaseTemplate hbaseTemplate) throws IOException {
		hbaseTemplate.createTableWithFamilies(HbaseProductTableName, HbaseProductFamilyName);
	}
	public default void refreshHbaseFields(HbaseTemplate hbaseTemplate, Product product) throws IOException {
		
		String text = hbaseTemplate.getByRowKey(ProductDAOByMySQL.HbaseProductTableName, Long.toString(product.getId()), ProductDAOByMySQL.HbaseProductFamilyName, ProductDAOByMySQL.HbaseProductTableColumns[0]);
		product.setName(text);
	}
	public default void saveHbaseFields(HbaseTemplate hbaseTemplate, Product product) throws IOException {
		hbaseTemplate.saveOrUpdateByRowKey(HbaseProductTableName, Long.toString(product.getId()), HbaseProductFamilyName, HbaseProductTableColumns[0], product.getName());
	}

	@Select({"<script>",
		"SELECT * FROM products",
		"WHERE 1=1",
		"<when test='product.categoryId!=null'>",
		"AND category_id = #{product.categoryId}",
		"</when>",
		"ORDER BY category_id,id LIMIT #{start}, #{size}",
		"</script>"})
	@Results(id="productResults", value={
			@Result(property="id",   column="id"),
			@Result(property="price", column="price"),
			@Result(property="quantity", column="quantity"),
			@Result(property="status", column="status"),
			@Result(property="categoryId", column="category_id"),
	})
	public List<Product> queryProducts(Product product, int start, int size);

	@Select("SELECT * FROM products WHERE id = #{id} LIMIT 1")
	@ResultMap("productResults")
	public Product getProductById(long id);

	@Insert("INSERT INTO products(id,price,quantity,category_id) SELECT #{id}, #{price}, #{quantity}, #{categoryId}"
			+ " FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM products WHERE id = #{id} LIMIT 1)")
	@Results(id="insert", value={
			@Result(property="id", column="id", javaType = Boolean.class),
		})
	public boolean insert(Product product);

	@Update({"<script>",
		"UPDATE products SET id=id",
		"<when test='price!=null'>",
		", price = #{price}",
		"</when>",

		"<when test='status!=null'>",
		", status = #{status}",
		"</when>",
		
		"<when test='categoryId!=null'>",
		", category_id = #{categoryId}",
		"</when>",
		"WHERE id=#{id} LIMIT 1",
		"</script>"})
	@Results(id="update", value={
			@Result(property="id", column="id", javaType = Boolean.class),
		})
	public boolean update(Product product);

	@Update("UPDATE products SET quantity=#{quantity} WHERE id=#{id} LIMIT 1")
	@Results(id="updateQuantity", value={
			@Result(property="id", column="id", javaType = Boolean.class),
		})
	public boolean updateQuantity(long id, long quantity);
	
}
