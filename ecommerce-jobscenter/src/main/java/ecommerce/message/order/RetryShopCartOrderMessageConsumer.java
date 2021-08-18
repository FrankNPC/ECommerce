package ecommerce.message.order;

import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.listener.config.ContainerProperties;

import com.fasterxml.jackson.databind.ObjectMapper;

import ecommerce.service.KeyIdentifies;
import ecommerce.service.order.OrderMessageTopics;
import ecommerce.service.order.client.OrderFlowService;
import ecommerce.service.client.base.Order;
import ecommerce.service.client.base.ShopCartOrder;

public class RetryShopCartOrderMessageConsumer implements MessageListener<String, ShopCartOrder> {

    private static final Logger logger = LoggerFactory.getLogger(RetryShopCartOrderMessageConsumer.class);

	public RetryShopCartOrderMessageConsumer(Map<String, Object> config) throws Exception {
        ContainerProperties containerProperties = new ContainerProperties(OrderMessageTopics.RetryShopCartOrder.value);
        containerProperties.setMessageListener(this);
        
        new KafkaMessageListenerContainer<String, ShopCartOrder>(
        		new DefaultKafkaConsumerFactory<String, ShopCartOrder>(config),
        		containerProperties).start();
    }
	
    @Autowired
    private OrderFlowService orderFlowService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
	@Override
	public void onMessage(ConsumerRecord<String, ShopCartOrder> consumer) {
		try {
			Order order = orderFlowService.createOrder(consumer.value());
			String orderMessageStr = new ObjectMapper().writeValueAsString(order);
			if (order!=null) {
				stringRedisTemplate.opsForZSet().remove(KeyIdentifies.DelayShopCartOrderMessage.value, consumer.value().getUserId());
				stringRedisTemplate.opsForZSet().add(KeyIdentifies.DelayCloseMessage.value, orderMessageStr, order.getCreateTime());
				stringRedisTemplate.opsForZSet().add(KeyIdentifies.DelayCompleteMessage.value, orderMessageStr, order.getCreateTime());
			}
			logger.info(orderMessageStr);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
    
}
