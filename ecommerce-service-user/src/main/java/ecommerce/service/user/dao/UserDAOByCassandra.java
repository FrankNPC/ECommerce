package ecommerce.service.user.dao;

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
//import ecommerce.service.client.base.User;
//
//@Repository
//public class UserDAOByCassandra {
//
//    @Autowired
//    private CassandraTemplate cassandraTemplate;
//
//    @PostConstruct
//    protected void Initiator(){
////    	cassandraTemplate.getCqlOperations().execute("DROP TABLE users");
//    	cassandraTemplate.getCqlOperations().execute(
//    			"CREATE TABLE IF NOT EXISTS users "
//    			+ "(id bigint,"
//    			+ "username varchar,"
//    			+ "session_id varchar,"
//    			+ "password varchar,"
//    			+ "PRIMARY KEY(id, username))"
////    			+ "WITH CLUSTERING ORDER BY ( DESC)"
//    			);
//	}
//	
//    private User convert(EcommerceUser eUser) {
//    	User user = new User();
//    	user.setUsername(eUser.username);
//    	user.setId(eUser.id);
//    	user.setPassword(eUser.password);
//    	user.setSessionId(eUser.sessionId);
//    	return user;
//    }
//
//	private static Map<Integer, String> pagingStateMap = new ConcurrentHashMap<>();
//    public List<User> queryUsers(User userExample, int page, int size){
//    	CassandraPageRequest request = null;
//    	String state = pagingStateMap.get(size);
//    	if (page==0||state==null) {
//    		request = CassandraPageRequest.first(size);
//    	}else {
//    		request = CassandraPageRequest.of(PageRequest.of(0, size), PagingState.fromString(state));
//    		if (page<0) {
//    			request=(CassandraPageRequest) request.previous();
//    		}else if (page>0){
//    			request=request.next();
//    		}
//    	}
//		Slice<EcommerceUser> slice = cassandraTemplate.slice(
//											Query.empty().pageRequest(request), EcommerceUser.class);
//		List<User> userList = new ArrayList<>();
//		if (slice.hasContent()) {
//			slice.getContent().stream().forEach(user->userList.add(convert(user)));
//		}
//		request = (CassandraPageRequest) slice.getPageable();
//		if (request.getPagingState()!=null) {
//			pagingStateMap.put(size, request.getPagingState().toString());
//		}
//		return userList;
//	}
//
//    public User getUserById(long id) {
//    	return convert(cassandraTemplate.selectOneById(id, EcommerceUser.class));
//    }
//    public User getUserByName(String username) {
//    	return convert(cassandraTemplate.selectOne(
//    				Query.query(Criteria.where("username").is(username)).withAllowFiltering(), EcommerceUser.class));
//    }
//    public User getUserBySessionId(String sessionId) {
//    	return convert(cassandraTemplate.selectOne(
//				Query.query(Criteria.where("session_id").is(sessionId)).withAllowFiltering(), EcommerceUser.class));
//    }
//    
//    public boolean insert(User user) {
//        return cassandraTemplate.getCqlOperations().execute(
//        		String.format("insert into users (id, username,session_id,password) values (%s, '%s','%s','%s') IF NOT EXISTS",
//        				user.getId(), user.getUsername(), user.getSessionId(), user.getPassword()
//        				));
//    }
//
//    public boolean update(User user) {
//        return cassandraTemplate.update(
//        		Query.query(Criteria.where("id").is(user.getId()),
//    							Criteria.where("username").is(user.getUsername())), 
////		        				Criteria.where("session_id").is(user.getSessionId()), 
//    				Update.empty().set("password",user.getPassword())
//    								.set("session_id",user.getSessionId()), EcommerceUser.class);
//    }
//    
//    public boolean updateQuantity(long id, long quantity) {
//        return cassandraTemplate.update(
//        		Query.query(Criteria.where("id").is(id)), 
//        				Update.empty().set("quantity",quantity), EcommerceUser.class);
//
//    }
//
//}
