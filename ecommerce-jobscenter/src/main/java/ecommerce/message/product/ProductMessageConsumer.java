package ecommerce.message.product;

import java.util.Arrays;
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
import ecommerce.service.ProductMessageTopics;
import ecommerce.service.client.ProductService;
import ecommerce.service.client.Result;
import ecommerce.service.client.base.Order;

public class ProductMessageConsumer implements MessageListener<String, String> {

    private static final Logger logger = LoggerFactory.getLogger(ProductMessageConsumer.class);

	public ProductMessageConsumer(Map<String, Object> config) throws Exception {
        ContainerProperties containerProperties = new ContainerProperties(ProductMessageTopics.ProductQuantitySync.value);
        containerProperties.setMessageListener(this);
        
        new KafkaMessageListenerContainer<String, Order>(
        		new DefaultKafkaConsumerFactory<String, Order>(config),
        		containerProperties).start();
    }

    @Autowired
    private ProductService productService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
	@Override
	public void onMessage(ConsumerRecord<String, String> consumer) {
		Arrays.stream(consumer.value().split(","))
			.filter(str->str!=null&&!str.isEmpty())
				.forEach(str->{
					try {
						Long id = stringRedisTemplate.opsForSet().remove(KeyIdentifies.ProductQuantitySync.value, Long.toString(Long.parseLong(str)));
						if (id!=null&&id.longValue()>0) {
							Result<Long> result = productService.flushQuantity(id);
							logger.info(new ObjectMapper().writeValueAsString(result));
						}
					} catch (Exception e) {
						logger.error(e.getMessage());
					}
				});
	}

    
}
