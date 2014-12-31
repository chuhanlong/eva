package com.reed.rmi.base;

import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

/**
 * 处理自定义注解<@interface>
 * 
 * @author reed
 * 
 */
public class RpcBeanNameGenerator extends AnnotationBeanNameGenerator {

	/** 定义remote service 的 bean name的注解名 */
	private String rpcBeanNameAnnotation;

	public RpcBeanNameGenerator() {

	}

	public RpcBeanNameGenerator(String name) {
		this.rpcBeanNameAnnotation = name;
	}

	public String getRpcBeanNameAnnotation() {
		return rpcBeanNameAnnotation;
	}

	public void setRpcBeanNameAnnotation(String rpcBeanNameAnnotation) {
		this.rpcBeanNameAnnotation = rpcBeanNameAnnotation;
	}

	protected String determineBeanNameFromAnnotation(
			AnnotatedBeanDefinition annotatedDef) {

		AnnotationMetadata amd = annotatedDef.getMetadata();
		Set<String> types = amd.getAnnotationTypes();
		String beanName = null;
		for (String type : types) {
			// 判断是否是自定义注解
			if (type.equals(rpcBeanNameAnnotation)) {
				Map<String, Object> attributes = amd
						.getAnnotationAttributes(type);
				String value = (String) attributes.get("value");
				if (StringUtils.hasLength(value)) {
					beanName = value;
				}
			}
		}

		return beanName;
	}

}