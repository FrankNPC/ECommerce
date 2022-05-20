package ecommerce.service.order.dao;

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
//
//import com.datastax.driver.core.PagingState;
//
//import ecommerce.service.client.base.Order;
//
////@Repository
//public class OrderDAOByCassandra{
////	implements PlatformTransactionManager{
////
////	private TransactionStatus status;
////	@Override
////	public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
////		if (status!=null) {return status;}
////		return status = new TransactionStatus() {
////			private String transactionId="";
////			public String toString() {return this.transactionId;}
////			public Object createSavepoint() throws TransactionException {
////				if (transactionId.isEmpty()) {
////					transactionId = System.currentTimeMillis()+"_"+definition.getName()+"_"+StringUtils.hex62EncodingWithRandom(32);
////				}
////				return transactionId;
////			}
////			public void rollbackToSavepoint(Object savepoint) throws TransactionException {
////				if (transactionId.equals(savepoint)) {
////					rollback(this);
////				}
////			}
////			public void releaseSavepoint(Object savepoint) throws TransactionException {
////				if (transactionId.equals(savepoint)) {
////					rollback(this);
////					transactionId="";
////				}
////			}
////			public boolean isNewTransaction() {
////				return !transactionId.isEmpty();
////			}
////			public boolean hasSavepoint() {
////				return !transactionContainer.isEmpty();
////			}
////			private boolean rollbackOnly=false;
////			public void setRollbackOnly() {
////				rollbackOnly=true;
////			}
////			public boolean isRollbackOnly() {
////				return rollbackOnly;
////			}
////			public void flush() {
////			}
////			public boolean isCompleted() {
////				return transactionId.isEmpty();
////			}};
////	}
////	@SuppressWarnings("serial")
////	@Override
////	public void commit(TransactionStatus status) throws TransactionException {
////		for(TransactionUnit unit : transactionContainer) {
////			if (!unit.execute()) {throw new TransactionException(unit.toString()) {};}
////		}
////		this.status=null;
////	}
////	@Override
////	public void rollback(TransactionStatus status) throws TransactionException {
////		for(TransactionUnit unit : transactionContainer) {
////			if (!unit.execute()) {throw new TransactionException(unit.toString()) {};}
////		}
////		this.status=null;
////	}
////	
////	private interface TransactionUnit {
////		public boolean execute();
////	}
////	
////	private List<TransactionUnit> transactionContainer = new ArrayList<>();
////	private long lockedId = -1;
////	
////	private boolean lockRecord(long id) {
////		if (lockedId==id) {return true;}
////		if (cassandraTemplate.update(
////				Query.query(Criteria.where("id").is(id), Criteria.where("transaction_id").is("")), 
////						Update.empty().set("transaction_id",this.status.toString()), EcommerceOrder.class)) {
////			lockedId=id;return true;
////		}
////		return false;
////	}
//	
//	@Autowired
//	private CassandraTemplate cassandraTemplate;
//
//	@PostConstruct
//	private void Initiator(){
////		cassandraTemplate.getCqlOperations().execute("DROP TABLE orders");
//
//		cassandraTemplate.getCqlOperations().execute(
//				"CREATE TABLE IF NOT EXISTS orders "
//				+ "(id bigint,"
//				+ "parent_id bigint,"
//				+ "user_id bigint,"
//				+ "product_id bigint,"
//				+ "receipt_amount bigint,"
//				+ "paid_amount bigint,"
//				+ "status int,"
//				+ "create_time int,"
//				+ "external_order_id varchar,"
////				+ "transaction_id varchar,"
//				+ "PRIMARY KEY(id))"
////				+ "WITH CLUSTERING ORDER BY ( DESC)"
//				);
//	}
//
//	private Order convert(EcommerceOrder eOrder) {
//		Order order = new Order();
//		order.setCreateTime(eOrder.create_time);
//		order.setExternalOrderId(eOrder.external_order_id);
//		order.setId(eOrder.id);
//		order.setPaidAmount(eOrder.paid_amount);
//		order.setParentId(eOrder.parent_id);
//		order.setProductId(eOrder.product_id);
//		order.setReceiptAmount(eOrder.receipt_amount);
//		order.setStatus(eOrder.status);
//		order.setUserId(eOrder.user_id);
//		return order;
//	}
//
//	private static Map<Integer, String> pagingStateMap = new ConcurrentHashMap<>();
//	public List<Order> queryOrders(Order orderExample, int page, int size){
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
//		Slice<EcommerceOrder> slice = cassandraTemplate.slice(
//											Query.empty().pageRequest(request), EcommerceOrder.class);
//		List<Order> orderList = new ArrayList<>();
//		if (slice.hasContent()) {
//			slice.getContent().stream().forEach(order->orderList.add(convert(order)));
//		}
//		request = (CassandraPageRequest) slice.getPageable();
//		if (request.getPagingState()!=null) {
//			pagingStateMap.put(size, request.getPagingState().toString());
//		}
//		return orderList;
//	}
//
//	public Order getOrderById(long id) {
//		return convert(cassandraTemplate.selectOneById(id, EcommerceOrder.class));
//	}
//	
//	public Order getOrderByIdAndUserId(long id, long userId) {
//		Order order = getOrderById(id);
//		return order==null||userId!=order.getUserId()?null:order;
//	}
//	
//	public boolean insert(Order orderExample) {
//		return cassandraTemplate.getCqlOperations().execute(String.format("insert into orders (id,parent_id,user_id,product_id,receipt_amount,create_time,status,paid_amount,external_order_id)"
//				+ "values (%d,%d,%d,%d,%d,%d,0,0,'') IF NOT EXISTS",
//			orderExample.getId(), orderExample.getParentId(), orderExample.getUserId(), orderExample.getProductId(), orderExample.getReceiptAmount(), orderExample.getCreateTime()));
//	}
//
//	public boolean updateStatus(Order orderExample, int status) {
//		EcommerceOrder eOrder = cassandraTemplate.selectOneById(orderExample.getId(), EcommerceOrder.class);
//		if (eOrder==null||eOrder.status!=status) {return false;}
//		Query query = Query.query(Criteria.where("id").is(orderExample.getId()));
//		Update update = Update.empty().set("status",status);
//		return cassandraTemplate.update(query, update, EcommerceOrder.class);
//	}
//	
//	public boolean increPaidAmount(long id, long paidAmount) {
//		Query query = Query.query(Criteria.where("id").is(id));
//		Update update = Update.empty().set("paid_amount",paidAmount);
//		return cassandraTemplate.update(query, update, EcommerceOrder.class);
//	}
//	
//}
