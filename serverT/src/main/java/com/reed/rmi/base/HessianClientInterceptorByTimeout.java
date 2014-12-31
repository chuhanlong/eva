package com.reed.rmi.base;

import org.springframework.remoting.caucho.HessianClientInterceptor;

import com.caucho.hessian.client.HessianProxyFactory;

public class HessianClientInterceptorByTimeout extends HessianClientInterceptor {
	private HessianProxyFactory proxyFactory = new HessianProxyFactory();

	public void setConnectTimeout(long timeout) {
		this.proxyFactory.setConnectTimeout(timeout);
	}
}
