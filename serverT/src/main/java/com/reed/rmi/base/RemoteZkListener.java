package com.reed.rmi.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * rpc remote listener to register rpc url to zookeeper
 * 
 * @author reed
 * 
 */
@Component
public class RemoteZkListener extends BaseZkListener{

	/** log */
	private Logger logger = LoggerFactory.getLogger(RemoteZkListener.class);

	/**
	 * after context loading to connect zk to register rpc node
	 */
//	@Override
	public void onApplicationEvent(ContextRefreshedEvent ev) {
		if (super.isUseZk()) {
			logger.debug(">>>>>>>>>>>>>>>>>>zookeeper connecting.....");
			if (super.getRpcZkWatcher() == null) {
				try {
					RpcZkWatcher rpcZkWatcher = new RpcZkWatcher(
							super.getZkClaster(), null);
					super.setRpcZkWatcher(rpcZkWatcher);
					rpcZkWatcher.createPersist();
					rpcZkWatcher.createTemp();
					logger.info(">>>>>>>>>>>>>>>>>>zookeeper has connected.....");
				} catch (Exception e) {
					logger.error("client watcher start failed=====>"
							+ e.getMessage());
				}
			}
		}
	}
}