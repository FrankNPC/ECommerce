package ecommerce.service.product.dao;

//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//import javax.annotation.PostConstruct;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.cassandra.core.CassandraTemplate;
//import org.springframework.data.cassandra.core.query.CassandraPageRequest;
//import org.springframework.data.cassandra.core.query.Criteria;
//import org.springframework.data.cassandra.core.query.Query;
//import org.springframework.data.cassandra.core.query.Update;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Slice;
//import org.springframework.stereotype.Repository;
//
//import com.datastax.driver.core.PagingState;
//
//import ecommerce.service.client.base.Product;
//
//@Repository
//public class ProductDAOByCassandra {
//
//	@Autowired
//	private CassandraTemplate cassandraTemplate;
//
//	@PostConstruct
//	private void Initiator(){
////		cassandraTemplate.getCqlOperations().execute("DROP TABLE products");
//		cassandraTemplate.getCqlOperations().execute(
//				"CREATE TABLE IF NOT EXISTS products "
//				+ "(id bigint,"
//				+ "price bigint,"
//				+ "quantity bigint,"
//				+ "status int,"
//				+ "category_id int,"
//				+ "name varchar,"
//				+ "PRIMARY KEY(id))"
////				+ "WITH CLUSTERING ORDER BY ( DESC)"
//				);
//	}
//
//	private Product convert(EcommerceProduct prod) {
//		Product product = new Product();
//		product.setCategoryId(prod.category_id);
//		product.setId(prod.id);
//		product.setName(prod.name);
//		product.setPrice(prod.price);
//		product.setQuantity(prod.quantity);
//		product.setStatus(prod.status);
//		return product;
//	}
//
//
//	private static Map<Integer, String> pagingStateMap = new ConcurrentHashMap<>();
//	public List<Product> queryProducts(Product productExample, int page, int size){
//		CassandraPageRequest request = null;
//		String state = pagingStateMap.get(size);
//		if (page==0||state==null) {
//			request = CassandraPageRequest.first(size);
//		}else {
//			request = CassandraPageRequest.of(PageRequest.of(0, size), PagingState.fromString(state));
//			if (page<0) {
//				request=(CassandraPageRequest) request.previous();
//			}else if (page>0){
//				request=request.next();
//			}
//		}
//		Slice<EcommerceProduct> slice = cassandraTemplate.slice(
//											Query.empty().pageRequest(request), EcommerceProduct.class);
//		List<Product> productList = new ArrayList<>();
//		if (slice.hasContent()) {
//			slice.getContent().stream().forEach(prod->productList.add(convert(prod)));
//		}
//		request = (CassandraPageRequest) slice.getPageable();
//		if (request.getPagingState()!=null) {
//			pagingStateMap.put(size, request.getPagingState().toString());
//		}
//		return productList;
//	}
//
//	public Product getProductById(long id) {
//		return convert(cassandraTemplate.selectOneById(id, EcommerceProduct.class));
//	}
//	
//	public boolean insert(Product product) {
//		return cassandraTemplate.getCqlOperations().execute(
//				String.format("insert into products (id,price,status,category_id,name) values (%d,%d,%d,%d,'%s') IF NOT EXISTS",
//						product.getId(), product.getPrice(), product.getStatus(), product.getCategoryId(), product.getName()
//						));
//	}
//
//	public boolean update(Product product) {
//		return cassandraTemplate.update(
//				Query.query(Criteria.where("id").is(product.getId())), 
//						Update.empty()
//						.set("name",product.getName())
//						.set("price",product.getPrice())
//						.set("status",product.getStatus())
//						.set("category_id",product.getCategoryId()), EcommerceProduct.class);
//	}
//	public boolean updateQuantity(long id, long quantity) {
//		return cassandraTemplate.update(
//				Query.query(Criteria.where("id").is(id)), 
//						Update.empty().set("quantity",quantity), EcommerceProduct.class);
//
//	}
//}
