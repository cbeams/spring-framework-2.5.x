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
 * <br/>Note that as it is expected to be used mostly for accessing
 * factory methods, this factory by default operates in a <b>singleton</b> fashion.
 * The first request to {@link #getObject} by the owning bean factory will cause
 * a method invocation, whose return value will be cached for subsequent requests.
 * An internal {@link #setSingleton singleton} property may be set to false, to
 * cause this factory to invoke the target method each time it is asked for an
 * object.
 * <br/>A static target method may be specified by setting the
 * {@link #setStaticMethod staticMethod} property to a String representing the fully
 * qualified static method name. Alternately, a target instance method may be specified,
 * by setting the {@link #setTarget target} property as the
 * target object, and the {@link #setTargetMethod targetMethod} property as the name of the method to call on
 * that target object.
 * Arguments for the method invocation may be specified by setting the args property.
 * <br/><br/>This class depends on afterPropertiesSet being called once all properties
 * have been set, as per the InitializingBean contract.
 * 
 * @author colin sampaleanu
 * @since 2003-11-21
 * @version $Id: MethodInvokingFactoryBean.java,v 1.1 2003-11-22 00:45:12 colins Exp $
 */
public class MethodInvokingFactoryBean implements FactoryBean, InitializingBean {

	private boolean _singleton = true;
	private String _staticMethod;
	private Object _target;
	private String _targetMethod;
	private Object[] _args;

	// stores the method call result in the singleton case
	private Object _singletonObj;
	// the method we will call
	private Method _methodObj;

	/*
	 * @see org.springframework.beans.factory.FactoryBean#getObject()
	 */
	public Object getObject() throws Exception {
		if (_singleton) {
			if (_singletonObj == null)
				_singletonObj = obtainObject();
			return _singletonObj;
		}

		return obtainObject();
	}

	/*
	 * Will return the same value each time if the singleton property is set to
	 * true, and otherwise return the value returned from invoking the
	 * specified method.
	 * 
	 * @see org.springframework.beans.factory.FactoryBean#getObjectType()
	 */
	public Class getObjectType() {
		return _methodObj.getReturnType();
	}

	/*
	 * @see org.springframework.beans.factory.FactoryBean#isSingleton()
	 */
	public boolean isSingleton() {
		return _singleton;
	}

	/*
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws IllegalArgumentException, BeansException {

		if (_args == null)
			_args = new Object[0];
		Class[] types = new Class[_args.length];
		for (int i = 0; i < _args.length; ++i)
			types[i] = _args[i].getClass();

		if ((_target == null && _staticMethod == null) || (_target != null && _staticMethod != null))
			throw new IllegalArgumentException(
					"At least one, and one only of target or staticMethod must be set.");

		Class targetClass;
		String methodName;

		try {
			if (_staticMethod != null) {
				String invalid = "Invalid fully qualified static method name: ";
				int index = _staticMethod.lastIndexOf('.');
				if (index == -1)
					throw new IllegalArgumentException(invalid + _staticMethod);
				methodName = _staticMethod.substring(index + 1);
				String fqClassname = _staticMethod.substring(0, index);
				targetClass = Thread.currentThread().getContextClassLoader().loadClass(fqClassname);
			}
			else {
				if (_targetMethod == null)
					throw new IllegalArgumentException("targetMethod must be set when target is set.");

				targetClass = _target.getClass();
				methodName = _targetMethod;
			}
			_methodObj = targetClass.getMethod(methodName, types);
			if (_staticMethod != null && !Modifier.isStatic(_methodObj.getModifiers()))
				throw new IllegalArgumentException("target method should be static but is not");
					
		}
		catch (Exception e) {
			throw new FatalBeanException("Unable to obtain target method object", e);
		}

	}

	/**
	 * Set if a singleton should be created, or a new object on each request
	 * else. Default is true.
	 */
	public void setSingleton(boolean singleton) {
		_singleton = singleton;
	}

	public String getStaticMethod() {
		return _staticMethod;
	}
	/**
	 * The fully qualified name of the static method to call (for example,
	 * <code>java.lang.System.getProperties</code>. If this property is non-null, then
	 * the target property must be null; they are mutually exclusive.
	 * 
	 */
	public void setStaticMethod(String staticMethod) {
		_staticMethod = staticMethod;
	}
	
	
	public Object getTarget() {
		return _target;
	}
	/**
	 * Set the target object on which to call the target method. If this property is
	 * non-null, then the staticMethod property must be null; they are mutually
	 * exclusive.
	 */
	public void setTarget(Object target) {
		_target = target;
	}

	public String getTargetMethod() {
		return _targetMethod;
	}
	/**
	 * When the target property has been set, specifies the name of the method on that
	 * object which should be invoked. Not used for static method invocations.
	 */
	public void setTargetMethod(String targetMethod) {
		_targetMethod = targetMethod;
	}
	
	public Object[] getArgs() {
		return _args;
	}
	/**
	 * Allows arguments for the method invocation to be specified. If this property
	 * is not set, a method with no arguments is assumed.
	 */
	public void setArgs(Object[] args) {
		_args = args;
	}

	// internal method to actually get the object via the method call
	private Object obtainObject() throws Exception {

		// in the static case, target will just be null
		return _methodObj.invoke(_target, _args);
	}
}