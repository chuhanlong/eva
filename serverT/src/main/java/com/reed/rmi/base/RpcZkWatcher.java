package com.reed.rmi.base;

import java.net.InetAddress;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.reed.rmi.client.scanner.ClientServiceScanner;

/**
 * zookeeper Watcher to connect zk \ create node \ listen node change
 * 
 * @author reed
 * 
 */
@Component
public class RpcZkWatcher implements Watcher {
	/** log */
	private Logger logger = LoggerFactory.getLogger(RpcZkWatcher.class);
	public final static SimpleDateFormat f = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	/**
	 * 同步工具
	 * 
	 * **/
	private CountDownLatch count = new CountDownLatch(1);
	// StandardCharsets.UTF_8;
	private static final Charset CHARSET = Charset.forName("UTF-8");

	/** 本地服务标志,判断从znode上取到的节点是否是本地节点 ,默认取当前节点IP */
	public static String localTag;

	/** zookeeper 集群地址，如：10.1.77.15:2181,10.1.77.16:2181,10.1.77.18:2181 */
	public String zkClaster;

	/** zookeeper znode root */
	public final static String root = "/rpc";

	/** zookeeper connect time out */
	public static int timeout = 5000;
	/**
	 * zk实例
	 * **/
	public ZooKeeper zk;

	public static String getLocalTag() {
		return localTag;
	}

	public static void setLocalTag(String localTag) {
		RpcZkWatcher.localTag = localTag;
	}

	public String getZkClaster() {
		return zkClaster;
	}

	public void setZkClaster(String zkClaster) {
		this.zkClaster = zkClaster;
	}

	public static int getTimeout() {
		return timeout;
	}

	public static void setTimeout(int timeout) {
		RpcZkWatcher.timeout = timeout;
	}

	public RpcZkWatcher() {

	}

	/**
	 * hosts， zookeeper的访问地址
	 * 
	 * **/
	public RpcZkWatcher(String hosts, String tag) {
		localTag = tag;
		zkClaster = hosts;
		try {
			if (StringUtils.isBlank(localTag)) {
				localTag = InetAddress.getLocalHost().getHostAddress();
			}
			zk = new ZooKeeper(hosts, timeout, new Watcher() {
//				@Override
				public void process(WatchedEvent event) {
					if (event.getState() == Event.KeeperState.SyncConnected) {
						count.countDown();
					}
				}
			});
		} catch (Exception e) {
			logger.error("zk watcher init failed=====>" + e.getMessage());
		}
	}

	/***
	 * 
	 * 此方法是写入数据 如果不存在此节点 就会新建，已存在就是 更新
	 * 
	 * **/
	public void write(String path, String value) throws Exception {

		Stat stat = zk.exists(path, false);
		if (stat == null) {
			zk.create(path, value.getBytes(CHARSET), Ids.OPEN_ACL_UNSAFE,
					CreateMode.PERSISTENT);
		} else {
			zk.setData(path, value.getBytes(CHARSET), -1);
		}

	}

	/**
	 * 读取znode
	 * 
	 * @param path
	 * @param watch
	 * @return
	 * @throws Exception
	 */
	public String read(String path, Watcher watch) throws Exception {
		byte[] data = zk.getData(path, watch, null);
		return new String(data, CHARSET);
	}

	/**
	 * 监听path的变化自动切换rpc节点，目前切换规则：随机
	 * 
	 * @param path
	 * @throws Exception
	 */
	public void automicSwitch(String path) throws Exception {
		logger.debug("znode:" + path + "发生变化，rpc节点自动切换.......,时间  "
				+ f.format(new Date()));
		List<String> list = getChildren();
		if (list != null && list.size() > 0) {
			int r = getIndexByRandom(list.size());
			String index = root + "/" + list.get(r);
			String slave = new String(zk.getData(index, false, null));
			// 监听此节点的详细情况
			zk.exists(root, this);
			logger.info("rpc自动切换为：" + slave + ".......,时间  "
					+ f.format(new Date()));

			doBusiness(slave);

		} else {
			logger.error("No slave to 切换.......,时间  " + f.format(new Date()));
		}

	}

	/**
	 * 业务逻辑
	 * 
	 * @param node
	 */
	public void doBusiness(String node) {
		// DO business
		ApplicationContext ctx = AppliactionContextHelper
				.getApplicationContext();
		ClientServiceScanner clientServiceScanner = ctx
				.getBean(ClientServiceScanner.class);
		String url = clientServiceScanner.getServiceUrl();
		String[] strs = url.split(":");
		clientServiceScanner.setServiceUrl(strs[0] + "://" + node + ":"
				+ strs[2]);
		clientServiceScanner
				.postProcessBeanFactory((ConfigurableListableBeanFactory) ctx
						.getAutowireCapableBeanFactory());
	}

	/**
	 * 从注册所有节点中随机获取一个
	 * 
	 * @param size
	 * @return
	 */
	private int getIndexByRandom(int size) {
		return new Random().nextInt(size);
	}

	/**
	 * 初始化启动节点
	 * 
	 * @throws Exception
	 */
	public void startMaster() throws Exception {
		List<String> list = getChildren();
		if (list != null && list.size() > 0) {
			String m = new String(zk.getData(
					root + "/" + list.get(getIndexByRandom(list.size())),
					false, null));
			logger.debug(m + "的Master启动了........");
			doBusiness(m);
		}
	}

	/**
	 * 创建znode根节点，持久化状态
	 * 
	 * @throws Exception
	 */
	public void createPersist() throws Exception {
		// add root node and listen it
		if (zk.exists(root, this) == null) {
			zk.create(root, root.getBytes(), Ids.OPEN_ACL_UNSAFE,
					CreateMode.PERSISTENT);
			logger.debug("创建主节点成功........");
		}
	}

	/**
	 * 创建本地节点，临时状态，连接断开后即删除
	 * 
	 * @throws Exception
	 */
	public void createTemp() throws Exception {
		zk.create(root + "/" + localTag, localTag.getBytes(),
				Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
		logger.debug(localTag + "创建子节点成功...........");

	}

	/**
	 * 根据root根查询子节点
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<String> getChildren() throws Exception {
		List<String> list = null;
		// list root's children and listen children's change
		list = zk.getChildren(root, this);
		Collections.sort(list);
		if (list == null || list.isEmpty()) {
			logger.error("父路径" + root + "下面没有节点");
		}
		return list;
	}

	public void close() throws Exception {
		zk.close();
	}

	/**
	 * 监听
	 */
//	@Override
	public void process(WatchedEvent event) {
		String path = event.getPath();
		// 如果发现，监听的节点变化，那么就重新，进行监听
		try {
			if (event.getType() == Event.EventType.NodeDeleted) {
				logger.debug("注意有节点挂掉，重新调整监听策略........");
				automicSwitch(path);
			}
			if (event.getType() == Event.EventType.NodeChildrenChanged) {
				logger.debug("注意有节点变化，重新调整监听策略........");
				automicSwitch(path);
			}
		} catch (Exception e) {
			logger.error("zk listen event failed======>path:{},event:{},ex:{}",
					event.getPath(), event.getType(), e.getMessage());
		}
	}

	public static void main(String[] args) throws Exception {
		String ip = InetAddress.getLocalHost().getHostAddress();
		RpcZkWatcher s = new RpcZkWatcher(
				"10.1.77.15:2181,10.1.77.16:2181,10.1.77.18:2181", "b");
		s.createPersist();// 创建主节点
		s.createTemp();
		s.startMaster();
		Thread.sleep(Long.MAX_VALUE);
		s.close();

	}

}