package ecommerce.service.user;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import ecommerce.common.StringUtils;
import ecommerce.service.KeyIdentifies;
import ecommerce.service.client.Result;
import ecommerce.service.client.UserService;
import ecommerce.service.client.base.User;
import ecommerce.service.user.dao.UserDAOByMySQL;

@Service
public class UserServiceImplement implements UserService{
	
//    @Autowired
//    private UserDAOByCassandra userDAO;
    @Autowired
    private UserDAOByMySQL userDAO;

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    
	@Override
	public Result<List<User>> queryUsers(User user, int start, int size) {
		start = start<0?0:start;
		size = size<10||size>100?100:size;
		
		Result<List<User>> result = new Result<List<User>>();
		result.setData(userDAO.queryUsers(user, start, size));
		result.setCode(result.getData()==null||result.getData().isEmpty()?Result.Code.Error.value:Result.Code.OK.value);
		
		return result;
	}
	
	@Override
	public Result<User> getUserById(long userId) {
		Result<User> result = new Result<User>();
		result.setData(userDAO.getUserById(userId));
		result.setCode(result.getData()==null?Result.Code.Error.value:Result.Code.OK.value);
		return result;
	}
	
	@Override
	public Result<User> saveOrUpdateUser(User user) {
		Result<User> result = new Result<User>();
		result.setCode(Result.Code.Error.value);
		if (user==null
				||user.getPassword()==null||user.getPassword().length()!=32
				||user.getUsername()==null||user.getUsername().length()<8||user.getUsername().length()>32) {
			return result;
		}

		if (user.getId()==null) {
			Long id = stringRedisTemplate.opsForValue().increment(KeyIdentifies.UserId.value, 1);
			user.setId(id);
	    	user.setSessionId(StringUtils.hex62EncodingWithRandom(32,user.getId()));
			if (userDAO.insert(user)) {
				result.setData(user);
			}
		}else {
			if (user.getSessionId()==null) {
		    	user.setSessionId(StringUtils.hex62EncodingWithRandom(32,user.getId()));
			}
			if (userDAO.update(user)) {
				result.setData(user);
			}
		}
		result.setCode(result.getData()==null?Result.Code.Error.value:Result.Code.OK.value);
		return result;
	}

	@Override
	public Result<Boolean> removeUserById(User user) {
		Result<Boolean> result = new Result<Boolean>();
//		result.setData(userDAO.delete(user.getId())>0);
		result.setCode(result.getData()==null?Result.Code.Error.value:Result.Code.OK.value);
		return result;
	}

	@Override
	public Result<User> getUserByName(String username) {
		Result<User> result = new Result<User>();
		result.setData(userDAO.getUserByName(username));
		result.setCode(result.getData()==null?Result.Code.Error.value:Result.Code.OK.value);
		return result;
	}

	@Override
	public Result<User> getUserBySessionId(String sessionId) {
		Result<User> result = new Result<User>();
		result.setData(userDAO.getUserBySessionId(sessionId));
		result.setCode(result.getData()==null?Result.Code.Error.value:Result.Code.OK.value);
		return result;
	}


}
