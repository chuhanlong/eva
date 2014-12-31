package com.reed.rmi.base;

import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.AnnotationScopeMetadataResolver;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ScopeMetadata;
import org.springframework.context.annotation.ScopeMetadataResolver;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Service;

/**
 * 配置自动扫描@service注解的bean，注册为远程服务基础类，各实现类需针对采用的远程服务通信类型，实现doScan
 * 
 * @author reed
 * 
 */
public abstract class BaseScanner extends ClassPathBeanDefinitionScanner {

	public BeanNameGenerator beanNameGenerator;

	public ScopeMetadataResolver scopeMetadataResolver = new AnnotationScopeMetadataResolver();

	public BeanDefinitionRegistry registry;

	public final static String HTTPINVOKER = "0";
	public final static String HESSIAN = "1";

	public BaseScanner(BeanDefinitionRegistry registry,
			String rpcBeanNameAnnotation) {
		super(registry);
		this.registry = registry;
		this.beanNameGenerator = new RpcBeanNameGenerator(rpcBeanNameAnnotation);
	}

	@Override
	protected void registerDefaultFilters() {
		addIncludeFilter(new AnnotationTypeFilter(Service.class));
	}

	protected BeanDefinitionHolder applyScopedProxyMode(ScopeMetadata metadata,
			BeanDefinitionHolder definition, BeanDefinitionRegistry registry) {

		ScopedProxyMode scopedProxyMode = metadata.getScopedProxyMode();
		if (scopedProxyMode.equals(ScopedProxyMode.NO)) {
			return definition;
		}
		boolean proxyTargetClass = scopedProxyMode
				.equals(ScopedProxyMode.TARGET_CLASS);
		return ScopedProxyUtils.createScopedProxy(definition, registry,
				proxyTargetClass);
	}

}
