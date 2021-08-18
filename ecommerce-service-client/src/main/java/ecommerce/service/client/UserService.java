package ecommerce.service.client;

import java.util.List;

import ecommerce.service.client.base.User;

public interface UserService {

	public Result<List<User>> queryUsers(User user, int start, int size);
	public Result<User> getUserById(long userId);
	public Result<User> getUserByName(String username);
	public Result<User> getUserBySessionId(String sessionId);
	public Result<User> saveOrUpdateUser(User user);
	public Result<Boolean> removeUserById(User user);

}
