package ecommerce.service.user;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ecommerce.common.StringUtils;
import ecommerce.service.UserServiceBootApplication;
import ecommerce.service.client.Result;
import ecommerce.service.client.TokenService;
import ecommerce.service.client.UserService;
import ecommerce.service.client.base.User;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=UserServiceBootApplication.class)
public class UserServiceTest {

    @Autowired
    private UserService userService;
    
    @Autowired
    private TokenService tokenService;

    @Test
    public void testToken() throws Exception {
    	ObjectMapper objectMapper = new ObjectMapper();
    	
    	Result<String> result = tokenService.getToken();
    	Assert.assertTrue(result.getData()!=null);
    	System.out.println("getToken   :"+objectMapper.writeValueAsString(result));
    	Result<Boolean> verifyResult = tokenService.verifyToken(result.getData());
    	Assert.assertTrue(verifyResult.getData());
    	System.out.println("verifyToken:"+objectMapper.writeValueAsString(verifyResult));
    }

//    @Autowired
//    private UserDAOByCassandra userDAO;
//    @Test
//    public void testDao() throws Exception {
//    	User user1 = new User();
//    	user1.setId(1l);
//    	user1.setUsername("2");
//    	user1.setSessionId("1");
//    	user1.setPassword("1");
//
//    	System.out.println(userDAO.insert(user1));
//    	System.out.println(userDAO.insert(user1));
//    }
    @Test
    public void testInsert() throws Exception {
    	ObjectMapper objectMapper = new ObjectMapper();
    	User user = new User();
    	user.setUsername(""+System.currentTimeMillis());
    	user.setPassword(StringUtils.hex62EncodingWithRandom(32));
    	Result<User> result = userService.saveOrUpdateUser(user);
    	Assert.assertNotNull(result.getData());
    	System.out.println("saveOrUpdateUser:"+objectMapper.writeValueAsString(result));

    	Result<User> getResult = userService.getUserById(user.getId());
    	Assert.assertNotNull(getResult.getData());
    	System.out.println("getUserById     :"+objectMapper.writeValueAsString(getResult));

    	Result<User> getByNameResult = userService.getUserByName(user.getUsername());
    	Assert.assertNotNull(getByNameResult.getData());
    	System.out.println("getByNameResult :"+objectMapper.writeValueAsString(getByNameResult));

    	Result<User> getBySessionIdResult = userService.getUserBySessionId(user.getSessionId());
    	Assert.assertNotNull(getBySessionIdResult.getData());
    	System.out.println("getBySessionIdResult:"+objectMapper.writeValueAsString(getBySessionIdResult));

//    	Result<Boolean> removedResult = userService.removeUserById(user);
//    	Assert.assertNotNull(removedResult.getData());
//    	System.out.println("removeUserById  :"+objectMapper.writeValueAsString(removedResult));
//
//    	Result<User> reGetResult = userService.getUserById(user.getId());
//    	Assert.assertNull(reGetResult.getData());
//    	System.out.println("getUserById     :"+objectMapper.writeValueAsString(reGetResult));
    }

    @Test
    public void testQuery() throws Exception {
    	ObjectMapper objectMapper = new ObjectMapper();
    	Result<List<User>> result = userService.queryUsers(new User(), 0, 20);
    	Assert.assertTrue(result.getData()!=null);
    	result.getData().stream().forEach(e->{
        	try {
				System.out.println("queryUsers:"+objectMapper.writeValueAsString(e));
			} catch (JsonProcessingException e1) {
				e1.printStackTrace();
			}
    	});
    }


    @Test
    public void testUpdate() throws Exception {
    	ObjectMapper objectMapper = new ObjectMapper();
    	User user = new User();
    	user.setUsername(""+System.currentTimeMillis());
    	user.setPassword(StringUtils.hex62EncodingWithRandom(32));
    	Result<User> result = userService.saveOrUpdateUser(user);
    	Assert.assertNotNull(result.getData());
    	System.out.println("saveOrUpdateUser:"+objectMapper.writeValueAsString(result));

    	String password = StringUtils.hex62EncodingWithRandom(32);
    	user.setPassword(password);
    	Result<User> updatedResult = userService.saveOrUpdateUser(user);
    	Assert.assertNotNull(updatedResult.getData());
    	Assert.assertTrue(password.equals(updatedResult.getData().getPassword()));
    	System.out.println("saveOrUpdateUser:"+objectMapper.writeValueAsString(updatedResult));
    	
    	String sessionId= user.getSessionId();
    	user.setSessionId(null);

    	Result<User> resetSessionIdResult = userService.saveOrUpdateUser(user);
    	Assert.assertNotNull(resetSessionIdResult.getData());
    	Assert.assertNotNull(sessionId.equals(resetSessionIdResult.getData().getSessionId()));
    	System.out.println("resetSessionId  :"+objectMapper.writeValueAsString(resetSessionIdResult));
    	
    }
}
