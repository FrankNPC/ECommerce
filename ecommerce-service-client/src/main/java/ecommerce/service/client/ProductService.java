package ecommerce.service.client;

import java.util.List;

import ecommerce.service.client.base.Product;

public interface ProductService {

	public Result<List<Product>> queryProducts(Product product, int start, int size);

	public Result<Product> getProductById(long productId);
	public Result<Product> saveOrUpdateProduct(Product product);
	public Result<Boolean> removeProductById(long productId);

	public Result<Long> increQuantity(long productId, long quantity);
	
	public Result<List<Long>> increBatchQuantity(List<Long> productIds, List<Long> quantities);

	public Result<List<Long>> queryQuantity(List<Long> productIds);
	public Result<Long> flushQuantity(long productId);
	public Result<Boolean> removeQuantity(long productId);
}
