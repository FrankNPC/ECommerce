package ecommerce.service.product;

//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//import java.util.stream.Collectors;
//
//import javax.annotation.Resource;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.data.redis.core.script.DefaultRedisScript;
//import org.springframework.kafka.core.KafkaTemplate;
//
//import ecommerce.service.KeyIdentifies;
//import ecommerce.service.ProductMessageTopics;
//import ecommerce.service.client.ProductService;
//import ecommerce.service.client.Result;
//import ecommerce.service.client.base.Product;
//import ecommerce.service.product.dao.ProductDAOByCassandra;
//
////@Service
//public class ProductServiceImplementByCassandra implements ProductService{
//
//	private static final Logger logger = LoggerFactory.getLogger(ProductServiceImplementByCassandra.class);
//	
//	@Autowired
//	private ProductDAOByCassandra productDAO;
//
//	@Resource
//	private RedisTemplate<String, Product> redisTemplate;
//
//	@Resource
//	private KafkaTemplate<String, String> stringKafkaTemplate;
//	
//	@Resource
//	private StringRedisTemplate stringRedisTemplate;
//
//	@Resource
//	private RedisLock distributedLock;
//	
//	@Override
//	public Result<List<Product>> queryProducts(Product product, int start, int size) {
//		Result<List<Product>> result = new Result<List<Product>>();
//
//		start = start<0?0:start;
//		size = size<10||size>100?100:size;
//		
//		List<Product> prodList = productDAO.queryProducts(product, start, size);
//		result.setData(prodList);
//		result.setCode(result.getData()==null||result.getData().isEmpty()?Result.Code.Error.value:Result.Code.OK.value);
//		return result;
//	}
//	
//	@Override
//	public Result<Product> getProductById(long productId) {
//		Result<Product> result = new Result<Product>();
//		result.setCode(Result.Code.Error.value);
//		
//		String productKey = String.format(KeyIdentifies.ProductKey.value, productId);
//		Product product = redisTemplate.opsForValue().get(productKey);
//		if (product==null) {
//			product = productDAO.getProductById(productId);
//			if (product==null) {return result;}
//			redisTemplate.opsForValue().set(productKey, product, KeyIdentifies.ProductKey.interval, TimeUnit.SECONDS);
//		}
//		result.setData(product);
//		result.setCode(result.getData()==null?Result.Code.Error.value:Result.Code.OK.value);
//		return result;
//	}
//	
//	@Override
//	public Result<Product> saveOrUpdateProduct(Product product) {
//		Result<Product> result = new Result<Product>();
//		if (product.getId()==null) {
//			Long id = stringRedisTemplate.opsForValue().increment(KeyIdentifies.ProductId.value, 1);
//			long categoryId = product.getCategoryId(),leftOne=1l<<62;
//			while((categoryId&leftOne)==0) {categoryId<<=1;}
//			product.setId(id|categoryId);
//			if (productDAO.insert(product)) {
//				result.setData(product);
//				String productKey = String.format(KeyIdentifies.ProductKey.value, product.getId());
//				redisTemplate.opsForValue().set(productKey, product, KeyIdentifies.ProductKey.interval, TimeUnit.SECONDS);
//				stringRedisTemplate.opsForHash().put(KeyIdentifies.ProductQuantityKey.value, Long.toString(product.getId()), Long.toString(product.getQuantity()));
//			}
//		}else {
//			Result<Product> prodResult = getProductById(product.getId());
//			if (prodResult!=null&&prodResult.getCode()==Result.Code.OK.value&&!product.getName().equals(prodResult.getData().getName())) {
//			}
//			if (productDAO.update(product)) {
//				result.setData(product);
//			}
//			String productKey = String.format(KeyIdentifies.ProductKey.value, product.getId());
//			redisTemplate.delete(productKey);
//		}
//
//		this.flushQuantity(product.getId(), true);
//		if (product.getStatus()==Product.Status.Invalid.value) {
//			this.removeQuantity(product.getId());
//		}
//		result.setCode(result.getData()==null?Result.Code.Error.value:Result.Code.OK.value);
//		return result;
//	}
//
//	@Override
//	public Result<Boolean> removeProductById(long productId) {
//		Result<Boolean> result = new Result<Boolean>();
//		result.setCode(Result.Code.Error.value);
//
//		Result<Product> prodResult = this.getProductById(productId);
//		if (prodResult==null||prodResult.getCode()!=Result.Code.OK.value) {
//			return result;
//		}
//		
//		Product product = prodResult.getData();
//		product.setStatus(Product.Status.Invalid.value);
//		if (productDAO.update(product)) {
//			result.setData(true);
//			if (product.getStatus()==Product.Status.Invalid.value) {
//				this.removeQuantity(product.getId());
//			}
//		}
//		
//		String productKey = String.format(KeyIdentifies.ProductKey.value, productId);
//		redisTemplate.delete(productKey);
//		result.setCode(result.getData()==null?Result.Code.Error.value:Result.Code.OK.value);
//		return result;
//	}
//
//	@Override
//	public Result<Long> increQuantity(long productId, long quantity) {
//		Result<Long> result = new Result<Long>();
//		result.setCode(Result.Code.Error.value);
//		if (quantity==0) {return result;}
//		
//		Result<Product> prodResult = this.getProductById(productId);
//		if (prodResult==null||prodResult.getCode()!=Result.Code.OK.value) {
//			return result;
//		}
//
//		List<String> prodIdKeys = Collections.singletonList(Long.toString(productId));
//		List<String>   lockKeys = Collections.singletonList(String.format(KeyIdentifies.ProductQuantityLock.value, productId));
//		Object[] quantityValues = Collections.singletonList(Long.toString(quantity)).toArray();
//		try {
//			if (distributedLock.tryLock(lockKeys, KeyIdentifies.ProductQuantityLock.interval, TimeUnit.MICROSECONDS)) {
//				Long number = stringRedisTemplate.execute(
//						new DefaultRedisScript<Long>(
//							"local incre=nil \n"+
//							"if tonumber(redis.call('HGET', '"+KeyIdentifies.ProductQuantityKey.value+"', KEYS[1]))+tonumber(ARGV[1])>=0 then\n"+
//							"  incre = redis.call('HINCRBY', '"+KeyIdentifies.ProductQuantityKey.value+"', KEYS[1], ARGV[1]) \n"+
//							"end \n"+
//							"return incre \n"
//								, Long.class),
//						prodIdKeys, quantityValues
//					);
//				distributedLock.unlock();
//				if (number!=null) {
//					stringKafkaTemplate.send(ProductMessageTopics.ProductQuantity.value, ""+productId);
//					result.setData(number);
//				}
//			}
//		} catch (InterruptedException e) {
//			logger.error(e.getMessage());
//		}
//		result.setCode(result.getData()==null?Result.Code.Error.value:Result.Code.OK.value);
//		return result;
//	}
//
//	@Override
//	public Result<List<Long>> increBatchQuantity(List<Long> productIds, List<Long> quantities, boolean positive){
//		Result<List<Long>> result = new Result<List<Long>>();
//		result.setCode(Result.Code.Error.value);
//		if (productIds.size()!=quantities.size()) {return result;}
//
//		String numberIds = null;
//		List<String> prodIdKeys = productIds.stream().map(String::valueOf).collect(Collectors.toList());
//		List<String>   lockKeys = productIds.stream().map(x->String.format(KeyIdentifies.ProductQuantityLock.value,x)).collect(Collectors.toList());
//		Object[] quantityValues = quantities.stream().map(String::valueOf).collect(Collectors.toList()).toArray();
//		try {
//			if (distributedLock.tryLock(lockKeys, KeyIdentifies.ProductQuantityLock.interval, TimeUnit.MICROSECONDS)) {
//				numberIds = stringRedisTemplate.execute(
//						new DefaultRedisScript<String>(
//								"local ret = '' \n "+
//								"for i=1, #ARGV, 1 do \n"+
//								"  if tonumber(redis.call('HGET', '"+KeyIdentifies.ProductQuantityKey.value+"', KEYS[i]))+tonumber(ARGV[i])>=0 then\n"+
//								"	ret = ret .. ',' .. redis.call('HINCRBY', '"+KeyIdentifies.ProductQuantityKey.value+"', KEYS[i], ARGV[i]) \n"+
//								"  else \n"+
//								"	ret = ret .. ',' \n"+
//								"  end \n"+
//								"end \n"+
//								"return ret \n"
//								, String.class),
//						prodIdKeys, quantityValues
//					);
//			}
//		} catch (InterruptedException e) {
//			logger.error(e.getMessage());
//		}finally {
//			distributedLock.unlock();
//		}
//		if (numberIds!=null) {
//			stringKafkaTemplate.send(ProductMessageTopics.ProductQuantity.value, productIds.stream().map(id->Long.toString(id)).collect(Collectors.joining(",")));
//			result.setData(Arrays.stream(numberIds.split(","))
//									.skip(1)
//										.map(n->n.isEmpty()?null:Long.parseLong(n))
//											.collect(Collectors.toList()));
//		}
//		
//		result.setCode(result.getData()==null?Result.Code.Error.value:Result.Code.OK.value);
//		return result;
//	}
//
//	private long flushQuantity(long productId, boolean force) {
//		long quantity=-1;
//		Object quantityStr = stringRedisTemplate.opsForHash().get(KeyIdentifies.ProductQuantityKey.value, Long.toString(productId));
//		if (quantityStr!=null) {
//			productDAO.updateQuantity(productId, Long.parseLong(quantityStr.toString()));
//		}
//		return quantity;
//	}
//
//	@Override
//	public Result<List<Long>> queryQuantity(List<Long> productIds){
//		Result<List<Long>> result = new Result<List<Long>>();
//		result.setCode(Result.Code.Error.value);
//		List<Object> quantities = stringRedisTemplate.opsForHash().multiGet(KeyIdentifies.ProductQuantityKey.value, productIds.stream().map(String::valueOf).collect(Collectors.toList()));
//		if (quantities!=null) {
//			result.setData(quantities.stream().map(x->x==null?null:Long.parseLong(x.toString())).collect(Collectors.toList()));
//		}
//		result.setCode(result.getData()==null?Result.Code.Error.value:Result.Code.OK.value);
//		return result;
//	}
//	
//	@Override
//	public Result<Long> flushQuantity(long productId) {
//		Result<Long> result = new Result<Long>();
//		result.setCode(Result.Code.Error.value);
//		
//		Result<Product> prodResult = this.getProductById(productId);
//		if (prodResult.getCode()!=Result.Code.OK.value) {
//			return result;
//		}
//		long quantity = flushQuantity(prodResult.getData().getId(), true);
//		result.setData(quantity==-1?prodResult.getData().getQuantity():quantity);
//		result.setCode(result.getData()==null?Result.Code.Error.value:Result.Code.OK.value);
//		return result;
//	}
//
//	@Override
//	public Result<Boolean> removeQuantity(long productId) {
//		Result<Boolean> result = new Result<Boolean>();
//		Result<Product> prodResult = this.getProductById(productId);
//		if (prodResult.getCode()!=Result.Code.OK.value) {
//			return result;
//		}
//		this.flushQuantity(productId, true);
//		result.setData(stringRedisTemplate.opsForHash().delete(KeyIdentifies.ProductQuantityKey.value, Long.toString(productId))!=null);
//		result.setCode(result.getData()==null?Result.Code.Error.value:Result.Code.OK.value);
//		return result;
//	}
//}
