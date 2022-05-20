package ecommerce.service.client;

public interface TokenService {

	public Result<String> getToken();
	
	public Result<String> getTokenByUserId(long userId);
	
	public Result<Boolean> verifyToken(String token);
}
