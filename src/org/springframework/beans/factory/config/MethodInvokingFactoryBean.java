package org.springframework.beans.factory.config;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

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
 * {@link #setStaticMethod staticMethod} property to a String representing the fully
 * qualified static method name. Alternately, a target instance method may be
 * specified, by setting the {@link #setTarget target} property as the target
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
 *   &lt;property name="staticMethod">&lt;value>java.lang.System.getProperties&lt;/value>&lt;/property>
 * &lt;/bean>
 * &lt;bean id="javaVersion" class="org.springframework.beans.factory.config.MethodInvokingFactoryBean">
 *   &lt;property name="target">&lt;ref local='sysProps'/>&lt;/property>
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
 * @since 2003-11-21
 * @version $Id: MethodInvokingFactoryBean.java,v 1.5 2004-02-13 17:52:57 jhoeller Exp $
 */
public class MethodInvokingFactoryBean implements FactoryBean, InitializingBean {

	public static final VoidType VOID = new VoidType();
	

	private boolean singleton = true;

	private String staticMethod;

	private Object target;

	private String targetMethod;

	private Object[] args;

	// stores the method call result in the singleton case
	private Object singletonObj;

	// the method we will call
	private Method methodObj;


	/**
	 * Set if a singleton should be created, or a new object on each request
	 * else. Default is true.
	 */
	public void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}

	/**
	 * The fully qualified name of the static method to call (for example,
	 * <code>java.lang.System.getProperties</code>. If this property is non-null, then
	 * the target property must be null; they are mutually exclusive.
	 * 
	 */
	public void setStaticMethod(String staticMethod) {
		this.staticMethod = staticMethod;
	}

	/**
	 * Set the target object on which to call the target method. If this property is
	 * non-null, then the staticMethod property must be null; they are mutually
	 * exclusive.
	 */
	public void setTarget(Object target) {
		this.target = target;
	}

	/**
	 * When the target property has been set, specifies the name of the method on that
	 * object which should be invoked. Not used for static method invocations.
	 */
	public void setTargetMethod(String targetMethod) {
		this.targetMethod = targetMethod;
	}

	/**
	 * Allows arguments for the method invocation to be specified. If this property
	 * is not set (null), or the Object array is of length 0, a method with no
	 * arguments is assumed.
	 */
	public void setArgs(Object[] args) {
		this.args = args;
	}


	public Object getObject() throws Exception {
		if (this.singleton) {
			if (this.singletonObj == null) {
				this.singletonObj = obtainObject();
			}
			return this.singletonObj;
		}
		return obtainObject();
	}

	/*
	 * Will return the same value each time if the singleton property is set
	 * to true, and otherwise return the value returned from invoking the
	 * specified method.
	 */
	public Class getObjectType() {
		Class type = this.methodObj.getReturnType();
		if (type.equals(void.class)) {
			type = MethodInvokingFactoryBean.VoidType.class;
		}
		return type;
	}

	public boolean isSingleton() {
		return singleton;
	}

	public void afterPropertiesSet() throws IllegalArgumentException, BeansException {
		if (this.args == null) {
			this.args = new Object[0];
		}
		Class[] types = new Class[this.args.length];
		for (int i = 0; i < this.args.length; ++i)
			types[i] = this.args[i].getClass();

		if ((this.target == null && this.staticMethod == null) ||
		    (this.target != null && this.staticMethod != null)) {
			throw new IllegalArgumentException(
					"At least one, and one only of target or staticMethod must be set.");
		}

		Class targetClass;
		String methodName;

		try {
			if (this.staticMethod != null) {
				String invalid = "Invalid fully qualified static method name: ";
				int index = this.staticMethod.lastIndexOf('.');
				if (index == -1) {
					throw new IllegalArgumentException(invalid + this.staticMethod);
				}
				methodName = this.staticMethod.substring(index + 1);
				String fqClassname =this.staticMethod.substring(0, index);
				targetClass = Thread.currentThread().getContextClassLoader().loadClass(fqClassname);
			}
			else {
				if (this.targetMethod == null)
					throw new IllegalArgumentException("targetMethod must be set when target is set.");

				targetClass = this.target.getClass();
				methodName = this.targetMethod;
			}

			// try to get the exact method first
			try {
				this.methodObj = targetClass.getMethod(methodName, types);
			}
			catch (NoSuchMethodException ex) {
				int matches = 0;
				// then try to get a method with the same number of args
				// we'll fail at runtime if in fact the args are not assignment compatible
				Method[] methods = targetClass.getMethods();
				for (int i = 0; i < methods.length; ++i) {
					Method method = methods[i];
					if (method.getName().equals(methodName) && method.getParameterTypes().length == types.length) {
						this.methodObj = method;
						++matches;
					}
				}

				// just rethrow exception if we can't get a match
				if (this.methodObj == null || matches > 1) {
					throw ex;
				}
			}
			if (this.staticMethod != null && !Modifier.isStatic(this.methodObj.getModifiers()))
				throw new IllegalArgumentException("target method should be static but is not");

		}
		catch (Exception ex) {
			throw new FatalBeanException("Unable to obtain target method object", ex);
		}

	}

	// internal method to actually get the object via the method call
	private Object obtainObject() throws Exception {
		// in the static case, target will just be null
		Object result = methodObj.invoke(target, args);
		if (result == null) {
			result = VOID;
		}
		return result;
	}


  // special marker class used for a null return value
	public static class VoidType {
	}

}
