package com.reed.rmi.base;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * rpc client listener to get rpc url from zookeeper
 * 
 * @author reed
 * 
 */
@Component
public abstract class BaseZkListener implements
		ApplicationListener<ContextRefreshedEvent> {

	/** 是否使用zookeeper注册服务 */
	private boolean useZk = false;
	/** zookeeper 集群地址，如：10.1.77.15:2181,10.1.77.16:2181,10.1.77.18:2181 */
	private String zkClaster;

	private RpcZkWatcher rpcZkWatcher;

	public RpcZkWatcher getRpcZkWatcher() {
		return rpcZkWatcher;
	}

	public void setRpcZkWatcher(RpcZkWatcher rpcZkWatcher) {
		this.rpcZkWatcher = rpcZkWatcher;
	}

	public String getZkClaster() {
		return zkClaster;
	}

	public void setZkClaster(String zkClaster) {
		this.zkClaster = zkClaster;
	}

	public boolean isUseZk() {
		return useZk;
	}

	public void setUseZk(boolean useZk) {
		this.useZk = useZk;
	}
	
}