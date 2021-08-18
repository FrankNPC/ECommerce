package ecommerce.message.order;

import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.listener.config.ContainerProperties;

import com.fasterxml.jackson.databind.ObjectMapper;

import ecommerce.service.client.base.Order;
import ecommerce.service.order.OrderMessageTopics;
import ecommerce.service.order.client.OrderFlowService;

public class PaymentOrderMessageConsumer implements MessageListener<String, Order> {

	private static final Logger logger = LoggerFactory.getLogger(PaymentOrderMessageConsumer.class);

	public PaymentOrderMessageConsumer(Map<String, Object> config) throws Exception {
        ContainerProperties containerProperties = new ContainerProperties(OrderMessageTopics.PaymentOrder.value);
        containerProperties.setMessageListener(this);
        
        new KafkaMessageListenerContainer<String, Order>(
        		new DefaultKafkaConsumerFactory<String, Order>(config),
        		containerProperties).start();
    }
	
    @Autowired
    private OrderFlowService orderFlowService;
    
	@Override
	public void onMessage(ConsumerRecord<String, Order> consumer) {
		try {
			Order order = orderFlowService.payment(consumer.value());
			logger.info(new ObjectMapper().writeValueAsString(order));
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
    
}
