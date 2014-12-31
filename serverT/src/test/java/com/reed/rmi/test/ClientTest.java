package com.reed.rmi.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.chu.dao.domain.Test;
import com.chu.service.TestService;

//import com.reed.rmi.domain.User;
//import com.reed.rmi.service.RemoteVersionService;
//import com.reed.rmi.service.UserService;

public class ClientTest {

	private static final Logger logger = LoggerFactory
			.getLogger(ClientTest.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Thread t = new Thread(new Runnable() {
		// public void run() {
		// ApplicationContext applicationContext = new
		// ClassPathXmlApplicationContext(
		// "client.xml");
		// // ApplicationContext applicationContext = new
		// // ClassPathXmlApplicationContext(
		// // "client-httpinvoker.xml");
		// try {
		// UserService service = (UserService) applicationContext
		// .getBean("userService");
		// User u = service.findById(1l);
		// RemoteVersionService remoteVersionService = (RemoteVersionService)
		// applicationContext
		// .getBean("remoteVersionService");
		// logger.info("============>"
		// + remoteVersionService.getVersion());
		// logger.info("============>" + u);
		// logger.info("============>" + service.save(u));
		// Thread.sleep(Long.MAX_VALUE);
		// } catch (Exception ex) {
		// ex.printStackTrace();
		// logger.error("ex error=======>" + ex.getMessage());
		// }
		// }
		// });
		Thread t = new TestThread();
		Thread t2 = new TestThread();
		t.start();
		t2.start();
	}

	private static class TestThread extends Thread {
		@Override
		public void run() {
			ApplicationContext applicationContext = new ClassPathXmlApplicationContext(
					"client.xml");
			// ApplicationContext applicationContext = new
			// ClassPathXmlApplicationContext(
			// "client-httpinvoker.xml");
			try {
				TestService service = (TestService) applicationContext
						.getBean("testService");
				Test test = service.findById(1);
				logger.info("============>" + test.getName());
//				User u = service.findById(1l);
//				RemoteVersionService remoteVersionService = (RemoteVersionService) applicationContext
//						.getBean("remoteVersionService");
//				logger.info("============>" + remoteVersionService.getVersion());
//				logger.info("============>" + u);
//				logger.info("============>" + service.save(u));
				Thread.sleep(Long.MAX_VALUE);
			} catch (Exception ex) {
				ex.printStackTrace();
				logger.error("ex error=======>" + ex.getMessage());
			}
		}
	}
}
