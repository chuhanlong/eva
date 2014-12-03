package serverT;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.chu.service.TestService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring.xml")
@TransactionConfiguration(transactionManager = "chuTransactionManager", defaultRollback = false)
@Transactional
public class TestServiceTest extends TestCase {

	@Autowired
	private TestService testService;
	
	@Test
	public final void test() {
		com.chu.dao.domain.Test test = testService.findById(1);
		Assert.assertTrue(test != null);
	}
	
	@Test
	public final void testSave() {
		com.chu.dao.domain.Test test = new com.chu.dao.domain.Test();
		test.setId(1);
		test.setName("test1");
		int i = testService.save(test);
		Assert.assertTrue(i == 1);
	}
}
