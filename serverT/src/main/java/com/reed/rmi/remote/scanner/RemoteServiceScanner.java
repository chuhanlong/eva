package com.reed.rmi.remote.scanner;

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.context.annotation.ScopeMetadata;
import org.springframework.remoting.caucho.HessianServiceExporter;
import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.reed.rmi.base.BaseScanner;

/**
 * scanner 自动扫描指定包下的@service注解的类，注册为远程服务，以beanName作为远程服务名
 * 
 * @author reed
 * 
 */
@Component
public class RemoteServiceScanner implements BeanFactoryPostProcessor,
		InitializingBean, ApplicationContextAware {

	private ApplicationContext applicationContext;
	/** 需要扫描的package,多个包以","分割 */
	private String basePackage;

	/** 定义remote service 的 bean name的注解名 */
	private String rpcBeanNameAnnotation;
	/** 远程服务代理类型,0:httpInvoker,1:hessian */
	private String proxyType;

	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;

	}

	public String getProxyType() {
		return proxyType;
	}

	public void setProxyType(String proxyType) {
		this.proxyType = proxyType;
	}

	public String getRpcBeanNameAnnotation() {
		return rpcBeanNameAnnotation;
	}

	public void setRpcBeanNameAnnotation(String rpcBeanNameAnnotation) {
		this.rpcBeanNameAnnotation = rpcBeanNameAnnotation;
	}

	public String getBasePackage() {
		return basePackage;
	}

	public void setBasePackage(String basePackage) {
		this.basePackage = basePackage;
	}

	public void afterPropertiesSet() throws Exception {

	}

	public void postProcessBeanFactory(
			ConfigurableListableBeanFactory beanFactory) throws BeansException {
		Scanner scanner = new Scanner((BeanDefinitionRegistry) beanFactory,
				this.rpcBeanNameAnnotation);
		scanner.setResourceLoader(this.applicationContext);

		scanner.scan(StringUtils.tokenizeToStringArray(this.basePackage,
				ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));

	}

	private final class Scanner extends BaseScanner {

		public Scanner(BeanDefinitionRegistry registry,
				String rpcBeanNameAnnotation) {
			super(registry, rpcBeanNameAnnotation);
		}

		@Override
		protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
			Set<BeanDefinitionHolder> beanDefinitions = new LinkedHashSet<BeanDefinitionHolder>();
			for (String basePackage : basePackages) {
				Set<BeanDefinition> candidates = findCandidateComponents(basePackage);
				for (BeanDefinition candidate : candidates) {
					ScopeMetadata scopeMetadata = this.scopeMetadataResolver
							.resolveScopeMetadata(candidate);
					candidate.setScope(scopeMetadata.getScopeName());
					String originalBeanName = this.beanNameGenerator
							.generateBeanName(candidate, this.registry);
					// if (candidate instanceof AbstractBeanDefinition) {
					// postProcessBeanDefinition((AbstractBeanDefinition)
					// candidate, originalBeanName);
					// }

					ScannedGenericBeanDefinition bd = (ScannedGenericBeanDefinition) candidate;
					String beanClassName = bd.getBeanClassName();
					if (proxyType.equals(HTTPINVOKER)) {
						bd.setBeanClassName(HttpInvokerServiceExporter.class
								.getName());
						bd.setBeanClass(HttpInvokerServiceExporter.class);
					}
					if (proxyType.equals(HESSIAN)) {
						bd.setBeanClassName(HessianServiceExporter.class
								.getName());
						bd.setBeanClass(HessianServiceExporter.class);
					}
					bd.getPropertyValues().add("service",
							applicationContext.getBean(originalBeanName));
					// bd.getPropertyValues().add("beanClassName",
					// beanClassName);
					String[] interfaces = bd.getMetadata().getInterfaceNames();
					if (interfaces == null || interfaces.length == 0)
						continue;
					// HessianServiceExporter.class.getClassLoader().getResource(name)
					Class interf = null;
					try {
						interf = Class.forName(interfaces[0]);
					} catch (ClassNotFoundException e) {
						continue;
					}
					bd.getPropertyValues().add("serviceInterface", interf);
					// if (candidate instanceof AnnotatedBeanDefinition) {
					// AnnotationConfigUtils.processCommonDefinitionAnnotations((AnnotatedBeanDefinition)
					// candidate);
					// }
					BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(
							candidate, "/" + originalBeanName);
					definitionHolder = applyScopedProxyMode(scopeMetadata,
							definitionHolder, this.registry);
					beanDefinitions.add(definitionHolder);
					registerBeanDefinition(definitionHolder, this.registry);
				}
			}
			if (beanDefinitions.isEmpty()) {
				System.out.println("not service be scaned");
			} else {
				for (BeanDefinitionHolder holder : beanDefinitions) {
					AnnotatedBeanDefinition definition = (AnnotatedBeanDefinition) holder
							.getBeanDefinition();
					System.out.println(holder.getBeanName());

					System.out.println(definition.getMetadata()
							.getAnnotationTypes());
				}
			}
			return beanDefinitions;
		}

	}

}
