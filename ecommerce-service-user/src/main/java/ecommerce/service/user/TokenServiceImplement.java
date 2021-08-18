package ecommerce.service.user;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import ecommerce.common.StringUtils;
import ecommerce.service.KeyIdentifies;
import ecommerce.service.client.Result;
import ecommerce.service.client.TokenService;

@Service
public class TokenServiceImplement implements TokenService{

    @Resource
    private StringRedisTemplate stringRedisTemplate;

	@Override
	public Result<String> getToken(){
		Result<String> result = new Result<String>();
		String randomStr = String.format(KeyIdentifies.TokenKey.value, StringUtils.hex62EncodingWithRandom(32));
		stringRedisTemplate.opsForValue().set(randomStr, Long.toString(System.currentTimeMillis()), KeyIdentifies.TokenKey.interval, TimeUnit.SECONDS);
		result.setData(randomStr);
		result.setCode(Result.Code.OK.value);
		return result;
	}
	
	@Override
	public Result<String> getTokenByUserId(long userId){
		Result<String> result = new Result<String>();
		String randomStr = String.format(KeyIdentifies.TokenKey.value, StringUtils.hex62EncodingWithRandom(32, userId));
		stringRedisTemplate.opsForValue().set(randomStr, Long.toString(System.currentTimeMillis()), KeyIdentifies.TokenKey.interval, TimeUnit.SECONDS);
		result.setData(randomStr);
		result.setCode(Result.Code.OK.value);
		return result;
	}

	@Override
	public Result<Boolean> verifyToken(String token){
		Result<Boolean> result = new Result<Boolean>();
		result.setData(stringRedisTemplate.delete(token));
		result.setCode(result.getData()==null?Result.Code.Error.value:Result.Code.OK.value);
		return result;
	}
	
}
