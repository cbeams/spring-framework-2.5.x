package org.springframework.beans.factory.config;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.MethodInvoker;

/**
 * FactoryBean which returns a value which is the result of a static or instance
 * method invocation.
 *
 * <p>Note that as it is expected to be used mostly for accessing
 * factory methods, this factory by default operates in a <b>singleton</b> fashion.
 * The first request to {@link #getObject} by the owning bean factory will cause
 * a method invocation, whose return value will be cached for subsequent requests.
 * An internal {@link #setSingleton singleton} property may be set to false, to
 * cause this factory to invoke the target method each time it is asked for an
 * object.
 *
 * <p>A static target method may be specified by setting the
 * {@link #setTargetMethod targetMethod} property to a String representing the static
 * method name, with {@link #setTargetClass targetClass} specifying the Class that
 * the static method is defined on. Alternatively, a target instance method may be
 * specified, by setting the {@link #setTargetObject target} property as the target
 * object, and the {@link #setTargetMethod targetMethod} property as the name of the
 * method to call on that target object. Arguments for the method invocation may be
 * specified by setting the args property.
 *
 * <p>This class depends on {@link #afterPropertiesSet()} being called once
 * all properties have been set, as per the InitializingBean contract.
 *
 * <p>An example (in an XML based bean factory definition) of a bean definition
 * which uses this class to call a static factory method:
 * <pre>
 * &lt;bean id="myClass" class="org.springframework.beans.factory.config.MethodInvokingFactoryBean">
 *   &lt;property name="staticMethod">&lt;value>com.whatever.MyClassFactory.getInstance&lt;/value>&lt;/property>
 * &lt;/bean></pre>
 * An example of calling a static method then an instance method to get at a Java
 * System property. Somewhat verbose, but it works.<pre>
 * &lt;bean id="sysProps" class="org.springframework.beans.factory.config.MethodInvokingFactoryBean">
 *   &lt;property name="targetClass">&lt;value>java.lang.System&lt;/value>&lt;/property>
 *   &lt;property name="targetMethod">&lt;value>getProperties&lt;/value>&lt;/property>
 * &lt;/bean>
 * &lt;bean id="javaVersion" class="org.springframework.beans.factory.config.MethodInvokingFactoryBean">
 *   &lt;property name="targetObject">&lt;ref local='sysProps'/>&lt;/property>
 *   &lt;property name="targetMethod">&lt;value>getProperty&lt;/value>&lt;/property>
 *   &lt;property name="args">
 *     &lt;list>
 *       &lt;value>|java.version|&lt;/value>
 *     &lt;/list>
 *   &lt;/property>
 * &lt;/bean>
 * </pre>
 * 
 * @author Colin Sampaleanu
 * @author Juergen Hoeller
 * @since 2003-11-21
 * @version $Id: MethodInvokingFactoryBean.java,v 1.6 2004-02-19 16:21:45 jhoeller Exp $
 */
public class MethodInvokingFactoryBean extends MethodInvoker implements FactoryBean, InitializingBean {

	private boolean singleton = true;

	// stores the method call result in the singleton case
	private Object singletonObject;

	/**
	 * Set if a singleton should be created, or a new object on each request
	 * else. Default is true.
	 */
	public void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}

	public void afterPropertiesSet() throws ClassNotFoundException, NoSuchMethodException {
		prepare();
	}

	public Object getObject() throws Exception {
		if (this.singleton) {
			if (this.singletonObject == null) {
				this.singletonObject = invoke();
			}
			return this.singletonObject;
		}
		return invoke();
	}

	/*
	 * Will return the same value each time if the singleton property is set
	 * to true, and otherwise return the value returned from invoking the
	 * specified method.
	 */
	public Class getObjectType() {
		Class type = getPreparedMethod().getReturnType();
		if (type.equals(void.class)) {
			type = VoidType.class;
		}
		return type;
	}

	public boolean isSingleton() {
		return singleton;
	}

}
