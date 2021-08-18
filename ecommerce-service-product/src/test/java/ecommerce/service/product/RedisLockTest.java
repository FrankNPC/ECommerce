package ecommerce.service.product;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import ecommerce.service.ProductServiceBootApplication;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=ProductServiceBootApplication.class)
public class RedisLockTest {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedisLock redisLock;
    @Test
    public void testIncre() throws Exception {
    	
//    	stringRedisTemplate.opsForHash().put("abc", "ddd", "2");
//    	stringRedisTemplate.opsForHash().put("abc", "eee", "3");
//    	List<Object> result1 = stringRedisTemplate.opsForHash().multiGet("abc", Arrays.asList("ddd", "eee"));
//    	List<Object> result2 = stringRedisTemplate.opsForHash().multiGet("abc", Arrays.asList("ddd", "e"));
//    	
//    	long time = Long.parseLong(result1.get(0).toString());
    	
    	Boolean b = stringRedisTemplate.opsForValue().setIfAbsent("test22", "9");
		System.out.println(b);

    	if (redisLock.tryLock(Arrays.asList("testlock123"), 100000000, TimeUnit.MICROSECONDS)) {
			Long number = stringRedisTemplate.execute(
					new DefaultRedisScript<Long>(
							"local incre=nil \n"+
							"if tonumber(redis.call('GET', KEYS[1]))+tonumber(ARGV[1])>=0 then \n"+
							"  incre = redis.call('INCRBY', KEYS[1], ARGV[1]) \n"+
							"end \n"+
							"return incre \n"
							, Long.class),
					Arrays.asList("test22"),  "3"
				);
			redisLock.unlock();
			System.out.println(stringRedisTemplate.opsForValue().get("test22"));
			System.out.println(number);
    	}

		List<String> keys = (List<String>) Arrays.asList("test123", "test1234");
		keys.stream().forEach(x->stringRedisTemplate.opsForValue().setIfAbsent(x, "123"));
		List<String> keys1 = keys.stream().map(x->x+"___").collect(Collectors.toList());
		Object[] numberObjects = new Object[]{"1", "2"};
    	if (redisLock.tryLock(keys1, 10000, TimeUnit.MICROSECONDS)) {
			String numbers = stringRedisTemplate.execute(
					new DefaultRedisScript<String>(
							"local ret = '' \n "+
							"for i=1, #ARGV, 1 do \n"+
							"  if tonumber(redis.call('GET', KEYS[i]))+tonumber(ARGV[i])>=0 then \n"+
							"    ret = ret .. ',' .. redis.call('INCRBY', KEYS[i], ARGV[i]) \n"+
							"  else \n"+
							"    ret = ret .. ',' \n"+
							"  end \n"+
							"end \n"+
							"return ret \n"
							, String.class),
					keys, numberObjects
				);
			redisLock.unlock();
			keys.stream().forEach(x->System.out.println(stringRedisTemplate.opsForValue().get(x)));
			System.out.println(numbers);
    	}
    }

}
