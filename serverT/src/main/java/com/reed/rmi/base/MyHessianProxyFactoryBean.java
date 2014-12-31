package com.reed.rmi.base;

import org.springframework.remoting.caucho.HessianProxyFactoryBean;

import com.caucho.hessian.client.HessianProxyFactory;

/**
 * 继承HessianProxyFactoryBean,扩展对两个timeout的配置
 * 
 * @author reed
 * 
 */
public class MyHessianProxyFactoryBean extends HessianProxyFactoryBean {

	private HessianProxyFactory proxyFactory = new HessianProxyFactory();

	private long readTimeout = -1;
	private long connectTimeout = -1;

	public void setReadTimeout(long readTimeout) {
		this.readTimeout = readTimeout;
	}

	public long getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(long connectTimeOut) {
		this.connectTimeout = connectTimeOut;
	}

	public void afterPropertiesSet() {
		proxyFactory.setReadTimeout(readTimeout);
		proxyFactory.setConnectTimeout(connectTimeout);
		setProxyFactory(proxyFactory);
		super.afterPropertiesSet();
	}
}
