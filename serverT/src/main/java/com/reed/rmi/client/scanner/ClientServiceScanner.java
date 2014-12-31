package com.reed.rmi.client.scanner;

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
import org.springframework.remoting.caucho.HessianProxyFactoryBean;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;
import org.springframework.remoting.httpinvoker.HttpInvokerRequestExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.reed.rmi.base.BaseScanner;

/**
 * 自动扫描指定包下被@service注解过的类，作为httpInvoker远程服务的客户端调用， 以接口名（首字母小写）作为bean
 * id注册为bean供本地调用
 * 
 * @author reed
 * 
 */
@Component
public class ClientServiceScanner implements BeanFactoryPostProcessor,
		InitializingBean, ApplicationContextAware {

	private ApplicationContext applicationContext;
	/** 需要扫描的package,多个包以","分割 */
	private String basePackage;
	/** httpInvoker 客户端代理类url前缀地址 */
	private String serviceUrl;
	/** 定义remote service 的 bean name的注解名 */
	private String rpcBeanNameAnnotation;
	/**
	 * hessian read timeout(s):out of this time client socket connection will be
	 * closed,defult is -1 no time out
	 */
	private long readTimeout = 5000;
	/**
	 * hessian connect timeout(s) refer to
	 * HessianConnectionFactoryByTimeout,,defult is -1 no time out
	 */
	private long connectTimeout = 10000;
	/** httpinvoker httpclient conneciton config */
	private HttpInvokerRequestExecutor httpInvokerRequestExecutor;

	/** 远程服务代理类型,0:httpInvoker,1:hessian */
	private String proxyType;

	public long getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(long readTimeout) {
		this.readTimeout = readTimeout;
	}

	public long getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(long connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public HttpInvokerRequestExecutor getHttpInvokerRequestExecutor() {
		return httpInvokerRequestExecutor;
	}

	public void setHttpInvokerRequestExecutor(
			HttpInvokerRequestExecutor httpInvokerRequestExecutor) {
		this.httpInvokerRequestExecutor = httpInvokerRequestExecutor;
	}

	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}

	public String getServiceUrl() {
		return serviceUrl;
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

	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;

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
					String url = serviceUrl + "/" + originalBeanName;
					if (proxyType.equals(HTTPINVOKER)) {
						bd.setBeanClassName(HttpInvokerProxyFactoryBean.class
								.getName());
						bd.setBeanClass(HttpInvokerProxyFactoryBean.class);
						// httpclient connction config
						if (httpInvokerRequestExecutor != null) {
							bd.getPropertyValues().add(
									"httpInvokerRequestExecutor",
									httpInvokerRequestExecutor);
						}
					}
					if (proxyType.equals(HESSIAN)) {
						bd.setBeanClassName(HessianProxyFactoryBean.class
								.getName());
						bd.setBeanClass(HessianProxyFactoryBean.class);
						// bd.setBeanClassName(MyHessianProxyFactoryBean.class
						// .getName());
						// bd.setBeanClass(MyHessianProxyFactoryBean.class);

						// connect timeout
						// bd.getPropertyValues().add("connectTimeout",
						// connectTimeout);

						// read timeout
						bd.getPropertyValues().add("readTimeout", readTimeout);
						// hessian调用支持方法重载 默认false不支持
						bd.getPropertyValues().add("overloadEnabled", true);
					}
					bd.getPropertyValues().add("serviceUrl", url);
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
					String beanName = interf.getSimpleName().substring(0, 1)
							.toLowerCase()
							+ interf.getSimpleName().substring(1);
					BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(
							candidate, beanName);
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
