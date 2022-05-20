package ecommerce.service.product;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import ecommerce.common.StringUtils;

public class RedisLock implements Lock{

	@Resource
	private StringRedisTemplate stringRedisTemplate;

	public RedisLock(long nodeId) {
		this.sessionId = "RedisLock_" + nodeId + "_"
				+ StringUtils.hex62EncodingWithRandom(16, System.currentTimeMillis());
	}

	private long distributLockTimeoutInMillis = 3000;
	private List<String> distributableKeys = new ArrayList<String>();
	private String sessionId = "";

	public void setDistributeKeys(List<String> keys) {
		this.distributableKeys.clear();
		this.distributableKeys.addAll(keys);
	}

	@Override
	public void lock() {
		this.lock(distributableKeys, distributLockTimeoutInMillis);
	}
	public void lock(List<String> keys, long distributLockTimeoutInMillis, TimeUnit unit) {
		this.lock(distributableKeys, unit.toMillis(distributLockTimeoutInMillis));
	}
	public void lock(List<String> keys, long distributLockTimeoutInMillis) {
		if (keys.isEmpty()) {return;}
		this.setDistributeKeys(keys);
		long current = System.currentTimeMillis();
		distributLockTimeoutInMillis+=3000;
		while(keys!=null) {
			distributLockTimeoutInMillis-=(System.currentTimeMillis()-current);
			if (distributLockTimeoutInMillis<3000) {unlock();break;}
			String signals = stringRedisTemplate.execute(
					new DefaultRedisScript<String>(
							"local ret = '' \n "+

//							"for i=1, #KEYS do \n"+
//							"  if redis.call('GET', KEYS[i]) then\n"+
//							"    return '' \n"+
//							"  end \n"+
//							"end \n"+
							
							"for i=1, #KEYS do \n"+
							"  if redis.call('GET', KEYS[i])==ARGV[1] or redis.call('SETNX', KEYS[i], ARGV[1])==1 then \n"+
							"    redis.call('PEXPIRE', KEYS[i], tonumber(ARGV[2])) \n"+
							"  else \n"+
							"    ret = ret .. ',' .. KEYS[i] \n"+
							"  end \n"+
							"end \n"+
							"return ret \n"
							, String.class),
					keys, sessionId, Long.toString(distributLockTimeoutInMillis)
				);
			keys=null;
			if (signals!=null&&!signals.isEmpty()) {
				keys=Arrays.stream(signals.split(",")).skip(1).collect(Collectors.toList());
			}
		}
	}

	@Override
	public void lockInterruptibly() throws InterruptedException {
		return;
	}

	@Override
	public boolean tryLock() {
		try {
			return this.tryLock(distributableKeys, distributLockTimeoutInMillis,  distributLockTimeoutInMillis, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return false;
	}
	@Override
	public boolean tryLock(long distributLockTimeoutInMillis, TimeUnit unit) throws InterruptedException {
		return this.tryLock(distributableKeys, distributLockTimeoutInMillis, distributLockTimeoutInMillis, unit);
	}
	public boolean tryLock(long distributLockTimeoutInMillis, long tryTimeoutInMillis, TimeUnit unit) throws InterruptedException {
		return this.tryLock(distributableKeys, distributLockTimeoutInMillis, distributLockTimeoutInMillis, unit);
	}
	public boolean tryLock(List<String> keys, long distributLockTimeoutInMillis, TimeUnit unit) throws InterruptedException {
		return this.tryLock(distributableKeys, distributLockTimeoutInMillis, distributLockTimeoutInMillis, unit);
	}
	public boolean tryLock(List<String> keys, long distributLockTimeoutInMillis, long tryTimeoutInMillis, TimeUnit unit) throws InterruptedException {
		if (keys.isEmpty()) {throw new InterruptedException();}
		setDistributeKeys(keys);
		distributLockTimeoutInMillis+=3000;
		long current = System.currentTimeMillis();
		long future = current+tryTimeoutInMillis;
		while(keys!=null) {
			distributLockTimeoutInMillis-=(System.currentTimeMillis()-current);
			if (distributLockTimeoutInMillis<3000||future<System.currentTimeMillis()) {unlock();break;}
			String signals = stringRedisTemplate.execute(
					new DefaultRedisScript<String>(
							"local ret = '' \n "+
					
//							"for i=1, #KEYS do \n"+
//							"  if redis.call('GET', KEYS[i]) then\n"+
//							"    return '' \n"+
//							"  end \n"+
//							"end \n"+
									
							"for i=1, #KEYS do \n"+
							"  if redis.call('GET', KEYS[i])==ARGV[1] or redis.call('SETNX', KEYS[i], ARGV[1])==1 then \n"+
							"    redis.call('PEXPIRE', KEYS[i], tonumber(ARGV[2])) \n"+
							"  else \n"+
							"    ret = ret .. ',' .. KEYS[i] \n"+
							"  end \n"+
							"end \n"+
							"return ret \n"
							, String.class),
					keys, sessionId, Long.toString(distributLockTimeoutInMillis)
				);
			keys=null;
			if (signals!=null&&!signals.isEmpty()) {
				keys=Arrays.stream(signals.split(",")).skip(1).collect(Collectors.toList());
			}
		}
		return true;
	}

	public void unlock(List<String> keys) {
		stringRedisTemplate.execute(
				new DefaultRedisScript<String>(
						"for i=1, #ARGV, 1 do \n"+
						"  if redis.call('GET', KEYS[i])==ARGV[1] then \n "+
						"    redis.call('DEL', KEYS[i]) \n"+
						"  end \n"+
						"end \n"
						, String.class),
				keys, sessionId
			);
	}
	@Override
	public void unlock() {
		this.unlock(this.distributableKeys);
	}

	@Override
	public Condition newCondition() {
		return null;
	}

}
