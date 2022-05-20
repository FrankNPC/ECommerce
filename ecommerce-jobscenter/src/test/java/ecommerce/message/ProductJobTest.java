package ecommerce.message;

import ecommerce.jobscenter.JobsCenterBootApplication;
import ecommerce.message.product.ProductMessageConsumer;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.springframework.test.context.ActiveProfiles;
@ActiveProfiles("allinone")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=JobsCenterBootApplication.class)
public class ProductJobTest {

	@Resource
	private ProductMessageConsumer productMessageConsumer;
	
	@Test
	public void test() {
		Lock lock = new ReentrantLock();
		lock.lock();
		try {
			lock.newCondition().await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		lock.unlock();
	}
}
