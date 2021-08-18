package ecommerce.message.order;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import com.fasterxml.jackson.databind.ObjectMapper;

import ecommerce.service.KeyIdentifies;
import ecommerce.service.client.base.Order;
import ecommerce.service.client.base.ShopCartOrder;
import ecommerce.service.order.OrderMessageTopics;

@EnableScheduling 
public class DelayMessageSchedulingConfigurer implements SchedulingConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(DelayMessageSchedulingConfigurer.class);

    @Autowired
    private KafkaTemplate<String, ShopCartOrder> shopCartOrderKafkaTemplate;

    @Autowired
    private KafkaTemplate<String, Order> orderKafkaTemplate;
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addFixedDelayTask(new Runnable() {
				@Override
				public void run() {
					Lock lock = new ReentrantLock();
					Condition condition = lock.newCondition();
					while(true) {
						Set<String> shopCartOrderSet = stringRedisTemplate.opsForZSet().range(KeyIdentifies.DelayShopCartOrderMessage.value, 0, 0);
						long min = System.currentTimeMillis();
						for(String shopCartOrderStr : shopCartOrderSet) {
							try {
								ShopCartOrder shopCartOrder = new ObjectMapper().readValue(shopCartOrderStr, ShopCartOrder.class);
								if (shopCartOrder.getCreateTime()+OrderMessageTopics.RetryShopCartOrder.interval>=System.currentTimeMillis()) {
									shopCartOrderKafkaTemplate.send(OrderMessageTopics.RetryShopCartOrder.value, shopCartOrder);
									stringRedisTemplate.opsForZSet().remove(KeyIdentifies.DelayShopCartOrderMessage.value, shopCartOrder);
								}else {
									min = shopCartOrder.getCreateTime()<min?shopCartOrder.getCreateTime():min;
								}
							} catch (IOException e) {
								logger.error(e.getMessage());
							}
						}
						if (lock.tryLock()) {
							try {
								condition.await(System.currentTimeMillis()-min, TimeUnit.MILLISECONDS);
							} catch (InterruptedException e) {
								logger.error(e.getMessage());
							}finally {
								lock.unlock();
							}
						}
					}
				}
			}, 0l);
        
        taskRegistrar.addFixedDelayTask(new Runnable() {
				@Override
				public void run() {
					Lock lock = new ReentrantLock();
					Condition condition = lock.newCondition();
					while(true) {
						Set<String> orderSet = stringRedisTemplate.opsForZSet().range(KeyIdentifies.DelayCloseMessage.value, 0, 0);
						long min = System.currentTimeMillis();
						for(String orderStr : orderSet) {
							try {
								Order order = new ObjectMapper().readValue(orderStr, Order.class);
								if (order.getCreateTime()+OrderMessageTopics.OrderClose.interval>=System.currentTimeMillis()) {
									orderKafkaTemplate.send(OrderMessageTopics.OrderClose.value, order);
									stringRedisTemplate.opsForZSet().remove(KeyIdentifies.DelayCloseMessage.value, order);
								}else {
									min = order.getCreateTime()<min?order.getCreateTime():min;
								}
							} catch (IOException e) {
								logger.error(e.getMessage());
							}
						}
						if (lock.tryLock()) {
							try {
								condition.await(System.currentTimeMillis()-min, TimeUnit.MILLISECONDS);
							} catch (InterruptedException e) {
								logger.error(e.getMessage());
							}finally {
								lock.unlock();
							}
						}
					}
				}
			}, 0l);
        
        taskRegistrar.addFixedDelayTask(new Runnable() {
				@Override
				public void run() {
					Lock lock = new ReentrantLock();
					Condition condition = lock.newCondition();
					while(true) {
						Set<String> orderSet = stringRedisTemplate.opsForZSet().range(KeyIdentifies.DelayCompleteMessage.value, 0, 0);
						long min = System.currentTimeMillis();
						for(String orderStr : orderSet) {
							try {
								Order order = new ObjectMapper().readValue(orderStr, Order.class);
								if (order.getCreateTime()+OrderMessageTopics.OrderComplete.interval>=System.currentTimeMillis()) {
									orderKafkaTemplate.send(OrderMessageTopics.OrderComplete.value, order);
									stringRedisTemplate.opsForZSet().remove(KeyIdentifies.DelayCompleteMessage.value, order);
								}else {
									min = order.getCreateTime()<min?order.getCreateTime():min;
								}
							} catch (IOException e) {
								logger.error(e.getMessage());
							}
						}
						if (lock.tryLock()) {
							try {
								condition.await(System.currentTimeMillis()-min, TimeUnit.MILLISECONDS);
							} catch (InterruptedException e) {
								logger.error(e.getMessage());
							}finally {
								lock.unlock();
							}
						}
					}
				}
			}, 0l);
	}
	
}
