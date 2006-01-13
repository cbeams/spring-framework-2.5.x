/*
 * Copyright 2002-2006 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.scripting;

import net.sf.cglib.asm.Type;
import net.sf.cglib.core.Signature;
import net.sf.cglib.proxy.InterfaceMaker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.Method;

/**
 * Abstract factory class for creating {@link Script} instances.
 * <p/>Users can configure the factory to specify the location of the script resource
 * and any additional configuration that may be required to create the scripted object
 * such as the interfaces required for the scripted proxy.
 * <p/>Users can then configure the scripted object itself with any configuration
 * data or dependencies as required.
 *
 * @author Rob Harrop
 * @author Rod Johnson
 * @since 2.0M2
 */
public abstract class AbstractScriptFactory implements BeanNameAware, BeanFactoryPostProcessor, BeanPostProcessor, ResourceLoaderAware {

	/**
	 * Prefix used to identify scripts that are defined 'inline' in a bean definition.
	 */
	private static final String INLINE_SCRIPT_PREFIX = "inline:";

	/**
	 * Name of the 'createObject' factory method used to construct scripted objects.
	 */
	private static final String CREATE_OBJECT_METHOD = "createObject";

	/**
	 * {@link Log} for this class.
	 */
	protected Log logger = LogFactory.getLog(getClass());

	/**
	 * Is refrehing for the scripted object enabled?
	 */
	private boolean enableRefresh = true;

	/**
	 * The name assigned to this instance when configured in the
	 * Spring {@link org.springframework.beans.factory.BeanFactory}.
	 */
	private String ownBeanName;

	/**
	 * The {@link org.springframework.beans.factory.BeanFactory} that
	 * this instance is running in.
	 */
	private ConfigurableListableBeanFactory beanFactory;

	/**
	 * {@link ResourceLoader} implementation used to resolve script resources.
	 */
	private ResourceLoader resourceLoader = new DefaultResourceLoader();

	private Map beanScriptSources = new HashMap();

	private Map beanInterfaces = new HashMap();

	public void setEnableRefresh(boolean enableRefresh) {
		this.enableRefresh = enableRefresh;
	}

	public void setBeanName(String beanName) {
		this.ownBeanName = beanName;
	}

	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
		String[] names = beanFactory.getBeanDefinitionNames();
		for (int i = 0; i < names.length; i++) {
			String name = names[i];
			BeanDefinition bd = beanFactory.getBeanDefinition(name);
			if (bd instanceof RootBeanDefinition) {
				RootBeanDefinition rbd = (RootBeanDefinition) bd;
				if (isScriptDefinition(rbd)) {
					// Add beanName as second argument, causing the 2-arg create method to be invoked
					logger.info("Handling createObject() method for bean name [" + name + "]");
					rbd.getConstructorArgumentValues().addGenericArgumentValue(name);
				}
			}
		}
	}

	private boolean isScriptDefinition(RootBeanDefinition rbd) {
		int ctorArgCount = rbd.getConstructorArgumentValues().getArgumentCount();
		return this.ownBeanName.equals(rbd.getFactoryBeanName()) &&
				CREATE_OBJECT_METHOD.equals(rbd.getFactoryMethodName()) &&
				(ctorArgCount == 1 || ctorArgCount == 2);
	}

	public Object createObject(String script, String beanName) throws Exception {
		return createObject(convertToScriptSource(script), null, beanName);
	}

	public Object createObject(String script, Class[] interfaces, String beanName) throws Exception {
		Assert.notNull(script, "Script location or data must be specified.");
		return createObject(convertToScriptSource(script), interfaces, beanName);
	}

	public Object createObject(ScriptSource scriptSource, String beanName) throws Exception {
		return createObject(scriptSource, null, beanName);
	}

	public Object createObject(ScriptSource scriptSource, Class[] interfaces, String beanName) throws Exception {
		Assert.notNull(scriptSource, "ScriptSource is required");
		Assert.notNull(beanName, "Bean name is required");

		if (requiresConfigInterface()) {
			Class configInterface = createConfigInterface(this.beanFactory.getBeanDefinition(beanName), interfaces);
			if(interfaces == null) {
				interfaces = new Class[]{configInterface};
			} else {
				List tmp = new ArrayList(Arrays.asList(interfaces));
				tmp.add(configInterface);
				interfaces = (Class[]) tmp.toArray(new Class[tmp.size()]);
			}
		}

		Script script = getScript(scriptSource, interfaces);
		this.beanScriptSources.put(beanName, scriptSource);
		this.beanInterfaces.put(beanName, interfaces);
		return script.createObject();
	}

	private ScriptSource convertToScriptSource(String script) {
		if (script.startsWith(INLINE_SCRIPT_PREFIX)) {
			return new StaticScriptSource(script.substring(INLINE_SCRIPT_PREFIX.length()));
		}
		else {
			return new ResourceScriptSource(this.resourceLoader.getResource(script));
		}
	}

	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		ScriptSource scriptSource = (ScriptSource) this.beanScriptSources.get(beanName);
		if (this.enableRefresh && scriptSource != null) {
			RefreshableScriptTargetSource ts = new RefreshableScriptTargetSource(this.beanFactory, beanName, scriptSource);

			DelegatingIntroductionInterceptor introduction = new DelegatingIntroductionInterceptor(ts);
			introduction.suppressInterface(TargetSource.class);

			Class[] interfaces = (Class[]) this.beanInterfaces.get(beanName);
			if (interfaces == null) {
				interfaces = bean.getClass().getInterfaces();
			}

			ProxyFactory proxyFactory = new ProxyFactory();
			proxyFactory.setInterfaces(interfaces);
			proxyFactory.addAdvice(introduction);
			proxyFactory.setTargetSource(ts);
			return proxyFactory.getProxy();
		}
		else {
			return bean;
		}
	}

	private Class createConfigInterface(BeanDefinition definition, Class[] interfaces) {
		InterfaceMaker maker = new InterfaceMaker();
		PropertyValue[] propertyValues = definition.getPropertyValues().getPropertyValues();
		for (int i = 0; i < propertyValues.length; i++) {
			PropertyValue propertyValue = propertyValues[i];
			String baseName = StringUtils.capitalize(propertyValue.getName());
			String setterName = "set" + baseName;
			String getterName = "get" + baseName;
			Class propertyType = findPropertyType(getterName, interfaces);
			Signature signature = new Signature(setterName, Type.VOID_TYPE, new Type[]{Type.getType(propertyType)});
			maker.add(signature, new Type[0]);
		}
		return maker.create();
	}

	private Class findPropertyType(String getterName, Class[] interfaces) {
		for (int i = 0; i < interfaces.length; i++) {
			Class iface = interfaces[i];
			Method[] methods = iface.getMethods();
			for (int j = 0; j < methods.length; j++) {
				Method method = methods[j];
				if(getterName.equals(method.getName()) && method.getParameterTypes().length == 0) {
					return method.getReturnType();
				}
			}
		}
		return Object.class;
	}

	protected boolean requiresConfigInterface() {
		return false;
	}

	protected abstract Script getScript(ScriptSource scriptSource, Class[] interfaces);
}
