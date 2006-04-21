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

package org.springframework.aop.framework;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.cglib.core.CodeGenerationException;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Dispatcher;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.proxy.NoOp;
import net.sf.cglib.transform.impl.UndeclaredThrowableStrategy;
import org.aopalliance.aop.AspectException;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aop.support.AopUtils;

/**
 * CGLIB2-based AopProxy implementation for the Spring AOP framework.
 * Requires CGLIB2 on the class path.
 *
 * <p>Objects of this type should be obtained through proxy factories,
 * configured by an AdvisedSupport object. This class is internal to the
 * Spring AOP framework and need not be used directly by client code.
 *
 * <p>DefaultAopProxyFactory will automatically create CGLIB2-based proxies
 * if necessary, for example in case of proxying a target class. See
 * DefaultAopProxyFactory's javadoc for details.
 *
 * <p>Proxies created using this class are thread-safe if the underlying
 * (target) class is thread-safe.
 *
 * <p>Built and tested against CGLIB 2.0.2, as of Spring 1.1.
 * Basic functionality will still work on CGLIB 2.0.1, but it is
 * generally recommended to use CGLIB 2.0.2 or later.
 *
 * @author Rod Johnson
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @see DefaultAopProxyFactory
 * @see AdvisedSupport#setProxyTargetClass
 */
public class Cglib2AopProxy implements AopProxy, Serializable {

	// Constants for CGLIB callback array indices
	private static final int AOP_PROXY = 0;
	private static final int INVOKE_TARGET = 1;
	private static final int NO_OVERRIDE = 2;
	private static final int DISPATCH_TARGET = 3;
	private static final int DISPATCH_ADVISED = 4;
	private static final int INVOKE_EQUALS = 5;


	/**
	 * Static to optimize serialization
	 */
	protected final static Log logger = LogFactory.getLog(Cglib2AopProxy.class);

	/**
	 * Keeps track of the <code>Class</code>es that we have validated for final methods
	 */
	private static Set validatedClasses = new HashSet();


	/** Config used to configure this proxy */
	protected final AdvisedSupport advised;

	private Object[] constructorArgs;

	private Class[] constructorArgTypes;

	/** Dispatcher used for methods on Advised */
	private final transient AdvisedDispatcher advisedDispatcher = new AdvisedDispatcher();

	private transient int fixedInterceptorOffset;

	private transient Map fixedInterceptorMap;


	/**
	 * Create a new Cglib2AopProxy for the given config.
	 * @throws AopConfigException if the config is invalid. We try to throw an informative
	 * exception in this case, rather than let a mysterious failure happen later.
	 */
	protected Cglib2AopProxy(AdvisedSupport config) throws AopConfigException {
		if (config == null) {
			throw new AopConfigException("Cannot create AopProxy with null ProxyConfig");
		}
		if (config.getAdvisors().length == 0 && config.getTargetSource() == AdvisedSupport.EMPTY_TARGET_SOURCE) {
			throw new AopConfigException("Cannot create AopProxy with no advisors and no target source");
		}
		//DK - is this check really necessary?
		//should this 'config.getTargetSource() == AdvisedSupport.EMPTY_TARGET_SOURCE' be enough?
		if (config.getTargetSource().getTargetClass() == null) {
			throw new AopConfigException("Either an interface or a target is required for proxy creation");
		}
		this.advised = config;
	}

	/**
	 * Set constructor arguments to use for creating the proxy.
	 * @param constructorArgs the constructor argument values
	 * @param constructorArgTypes the constructor argument types
	 */
	protected void setConstructorArguments(Object[] constructorArgs, Class[] constructorArgTypes) {
		if (constructorArgs == null || constructorArgTypes == null) {
			throw new IllegalArgumentException("Both constructorArgs and constructorArgTypes need to be specified");
		}
		if (constructorArgs.length != constructorArgTypes.length) {
			throw new IllegalArgumentException("Number of constructorArgs (" + constructorArgs.length +
					") must match number of constructorArgTypes (" + constructorArgTypes.length + ")");
		}
		this.constructorArgs = constructorArgs;
		this.constructorArgTypes = constructorArgTypes;
	}


	public Object getProxy() {
		return getProxy(null);
	}

	public Object getProxy(ClassLoader classLoader) {
		if (logger.isDebugEnabled()) {
			Class targetClass = this.advised.getTargetSource().getTargetClass();
			logger.debug("Creating CGLIB2 proxy" +
					(targetClass != null ? " for [" + targetClass.getName() + "]" : ""));
		}

		Enhancer enhancer = createEnhancer();
		try {
			Class rootClass = this.advised.getTargetSource().getTargetClass();
			Class proxySuperClass = (AopUtils.isCglibProxyClass(rootClass)) ? rootClass.getSuperclass() : rootClass;

			// Create proxy in specific ClassLoader, if given.
			if (classLoader != null) {
				enhancer.setClassLoader(classLoader);
			}

			// Validate the class, writing log messages as necessary.
			validateClassIfNecessary(proxySuperClass);

			enhancer.setSuperclass(proxySuperClass);

			enhancer.setCallbackFilter(new ProxyCallbackFilter(this.advised));
			enhancer.setStrategy(new UndeclaredThrowableStrategy(UndeclaredThrowableException.class));
			enhancer.setInterfaces(AopProxyUtils.completeProxiedInterfaces(this.advised));

			Callback[] callbacks = getCallbacks(rootClass);
			enhancer.setCallbacks(callbacks);

			if (CglibUtils.canSkipConstructorInterception()) {
				enhancer.setInterceptDuringConstruction(false);
			}
			
			Class[] types = new Class[callbacks.length];
			for (int x = 0; x < types.length; x++) {
				types[x] = callbacks[x].getClass();
			}
			enhancer.setCallbackTypes(types);

			// Generate the proxy class and create a proxy instance.
			Object proxy;
			if (this.constructorArgs != null) {
				proxy = enhancer.create(this.constructorArgTypes, this.constructorArgs);
			}
			else {
				proxy = enhancer.create();
			}

			return proxy;
		}
		catch (CodeGenerationException ex) {
			throw new AspectException("Couldn't generate CGLIB subclass of class '" +
					this.advised.getTargetSource().getTargetClass() + "': " +
					"Common causes of this problem include using a final class or a non-visible class",
					ex);
		}
		catch (IllegalArgumentException ex) {
			throw new AspectException("Couldn't generate CGLIB subclass of class '" +
					this.advised.getTargetSource().getTargetClass() + "': " +
					"Common causes of this problem include using a final class or a non-visible class",
					ex);
		}
		catch (Exception ex) {
			// TargetSource.getTarget failed
			throw new AspectException("Unexpected AOP exception", ex);
		}
	}

	/**
	 * Creates the CGLIB {@link Enhancer}. Subclasses may wish to override this to return a custom
	 * {@link Enhancer} implementation.
	 */
	protected Enhancer createEnhancer() {
		return new Enhancer();
	}

	/**
	 * Checks to see whether the supplied <code>Class</code> has already been validated and
	 * validates it if not.
	 */
	private void validateClassIfNecessary(Class proxySuperClass) {
		if (logger.isInfoEnabled()) {
			synchronized (validatedClasses) {
				if (!validatedClasses.contains(proxySuperClass)) {
					doValidateClass(proxySuperClass);
					validatedClasses.add(proxySuperClass);
				}
			}
		}
	}

	/**
	 * Checks for final methods on the <code>Class</code> and writes warnings to the log
	 * for each one found.
	 */
	private void doValidateClass(Class proxySuperClass) {
		Method[] methods = proxySuperClass.getMethods();
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			if (!Object.class.equals(method.getDeclaringClass()) && Modifier.isFinal(method.getModifiers())) {
				logger.info("Unable to proxy method [" + method + "] because it is final: " +
						"All calls to this method via a proxy will be routed directly to the proxy.");
			}
		}
	}

	private Callback[] getCallbacks(Class rootClass) throws Exception {
		// parameters used for optimisation choices
		boolean exposeProxy = this.advised.isExposeProxy();
		boolean isFrozen = this.advised.isFrozen();
		boolean isStatic = this.advised.getTargetSource().isStatic();

		// Choose an "aop" interceptor (used for AOP calls).
		Callback aopInterceptor = new DynamicAdvisedInterceptor();

		// Choose a "straight to target" interceptor. (used for calls that are
		// unadvised but can return this). May be required to expose the proxy.
		Callback targetInterceptor = null;

		if (exposeProxy) {
			targetInterceptor = isStatic ?
					(Callback) new StaticUnadvisedExposedInterceptor(this.advised.getTargetSource().getTarget()) :
					(Callback) new DynamicUnadvisedExposedInterceptor();
		}
		else {
			targetInterceptor = isStatic ?
					(Callback) new StaticUnadvisedInterceptor(this.advised.getTargetSource().getTarget()) :
					(Callback) new DynamicUnadvisedInterceptor();
		}

		// Choose a "direct to target" dispatcher (used for
		// unadvised calls to static targets that cannot return this).
		Callback targetDispatcher = isStatic ?
				(Callback) new StaticDispatcher(this.advised.getTargetSource().getTarget()) : new SerializableNoOp();

		Callback[] mainCallbacks = new Callback[]{
			aopInterceptor, // for normal advice
			targetInterceptor, // invoke target without considering advice, if optimized
			new SerializableNoOp(), // no override for methods mapped to this
			targetDispatcher, this.advisedDispatcher,
			new EqualsInterceptor(this.advised)
		};

		Callback[] callbacks;

		// If the target is a static one and the advice chain is frozen,
		// then we can make some optimisations by sending the AOP calls
		// direct to the target using the fixed chain for that method.
		if (isStatic && isFrozen) {
			Callback[] fixedCallbacks = null;

			Method[] methods = rootClass.getMethods();
			fixedCallbacks = new Callback[methods.length];

			this.fixedInterceptorMap = new HashMap();

			// TODO: small memory optimisation here (can skip creation for
			// methods with no advice)
			for (int x = 0; x < methods.length; x++) {
				List chain = this.advised.getAdvisorChainFactory().getInterceptorsAndDynamicInterceptionAdvice(
						this.advised, null, methods[x], rootClass);
				fixedCallbacks[x] = new FixedChainStaticTargetInterceptor(
						chain, this.advised.getTargetSource().getTarget(), this.advised.getTargetSource().getTargetClass());
				this.fixedInterceptorMap.put(methods[x].toString(), new Integer(x));
			}

			// Now copy both the callbacks from mainCallbacks
			// and fixedCallbacks into the callbacks array.
			callbacks = new Callback[mainCallbacks.length + fixedCallbacks.length];

			for (int x = 0; x < mainCallbacks.length; x++) {
				callbacks[x] = mainCallbacks[x];
			}

			for (int x = 0; x < fixedCallbacks.length; x++) {
				callbacks[x + mainCallbacks.length] = fixedCallbacks[x];
			}

			this.fixedInterceptorOffset = mainCallbacks.length;
		}
		else {
			callbacks = mainCallbacks;
		}
		return callbacks;
	}

	/**
	 * Wrap a return of this if necessary to be the proxy
	 */
	private static Object massageReturnTypeIfNecessary(Object proxy, Object target, Object retVal) {
		// Massage return value if necessary
		if (retVal != null && retVal == target) {
			// Special case: it returned "this"
			// Note that we can't help if the target sets a reference
			// to itself in another returned object.
			retVal = proxy;
		}
		return retVal;
	}


	public int hashCode() {
		return 0;
	}

	/**
	 * Checks to see if this CallbackFilter is the same CallbackFilter used for
	 * another proxy.
	 */
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (other == this) {
			return true;
		}

		Cglib2AopProxy otherCglibProxy = null;
		if (other instanceof Cglib2AopProxy) {
			otherCglibProxy = (Cglib2AopProxy) other;
		}
		else {
			// not a valid comparison
			return false;
		}

		return AopProxyUtils.equalsInProxy(advised, otherCglibProxy.advised);
	}


	/**
	 * Serializable replacement for CGLIB's NoOp interface.
	 * Public to allow use elsewhere in the framework.
	 */
	public static class SerializableNoOp implements NoOp, Serializable {

	}

	/**
	 * Method interceptor used for static targets with no advice chain. The call
	 * is passed directly back to the target. Used when the proxy needs to be
	 * exposed and it can't be determined that the method won't return
	 * <code>this</code>.
	 */
	private static class StaticUnadvisedInterceptor implements MethodInterceptor, Serializable {

		private final Object target;

		public StaticUnadvisedInterceptor(Object target) {
			this.target = target;
		}

		public Object intercept(Object proxy, Method method, Object[] args,
				MethodProxy methodProxy) throws Throwable {

			Object retVal = methodProxy.invoke(target, args);
			return massageReturnTypeIfNecessary(proxy, target, retVal);
		}
	}


	/**
	 * Method interceptor used for static targets with no advice chain, when the
	 * proxy is to be exposed.
	 */
	private static class StaticUnadvisedExposedInterceptor implements MethodInterceptor, Serializable {

		private final Object target;

		public StaticUnadvisedExposedInterceptor(Object target) {
			this.target = target;
		}

		public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
			Object oldProxy = null;
			try {
				oldProxy = AopContext.setCurrentProxy(proxy);
				Object retVal = methodProxy.invoke(target, args);
				return massageReturnTypeIfNecessary(proxy, target, retVal);
			}
			finally {
				AopContext.setCurrentProxy(oldProxy);
			}
		}
	}


	/**
	 * Interceptor used to invoke a dynamic target without creating a method
	 * invocation or evaluating an advice chain. (We know there was no advice
	 * for this method.)
	 */
	private class DynamicUnadvisedInterceptor implements MethodInterceptor, Serializable {

		public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
			Object target = advised.getTargetSource().getTarget();
			try {
				Object retVal = methodProxy.invoke(target, args);
				return massageReturnTypeIfNecessary(proxy, target, retVal);
			}
			finally {
				advised.getTargetSource().releaseTarget(target);
			}
		}
	}


	/**
	 * Interceptor for unadvised dynamic targets when the proxy needs exposing.
	 */
	private class DynamicUnadvisedExposedInterceptor implements MethodInterceptor, Serializable {

		public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
			Object oldProxy = null;
			Object target = advised.getTargetSource().getTarget();
			try {
				oldProxy = AopContext.setCurrentProxy(proxy);
				Object retVal = methodProxy.invoke(target, args);
				return massageReturnTypeIfNecessary(proxy, target, retVal);
			}
			finally {
				AopContext.setCurrentProxy(oldProxy);
				advised.getTargetSource().releaseTarget(target);
			}
		}
	}


	/**
	 * Dispatcher for a static target. Dispatcher is much faster than
	 * interceptor. This will be used whenever it can be determined that a
	 * method definitely does not return "this"
	 */
	private static class StaticDispatcher implements Dispatcher, Serializable {

		private Object target;

		public StaticDispatcher(Object target) {
			this.target = target;
		}

		public Object loadObject() {
			return target;
		}
	}


	/**
	 * Dispatcher for any methods declared on the Advised class.
	 */
	private class AdvisedDispatcher implements Dispatcher, Serializable {

		public Object loadObject() throws Exception {
			return advised;
		}
	}


	/**
	 * Dispatcher for the equals() method. Ensures that the method call is
	 * always handled by this class.
	 */
	private static class EqualsInterceptor implements MethodInterceptor, Serializable {

		private final AdvisedSupport advised;

		public EqualsInterceptor(AdvisedSupport advised) {
			this.advised = advised;
		}

		public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
			Object other = args[0];
			if (other == null) {
				return Boolean.FALSE;
			}
			if (other == proxy) {
				return Boolean.TRUE;
			}
			AdvisedSupport otherAdvised = null;
			if (other instanceof Factory) {
				Callback callback = ((Factory) other).getCallback(INVOKE_EQUALS);
				if (!(callback instanceof EqualsInterceptor)) {
					return Boolean.FALSE;
				}
				otherAdvised = ((EqualsInterceptor) callback).advised;
			}
			else {
				// not a valid comparison
				return Boolean.FALSE;
			}
			return new Boolean(AopProxyUtils.equalsInProxy(this.advised, otherAdvised));
		}
	}


	/**
	 * Interceptor used specifcally for advised methods on a frozen, static proxy.
	 */
	private static class FixedChainStaticTargetInterceptor implements MethodInterceptor, Serializable {

		private final List adviceChain;

		private final Object target;

		private final Class targetClass;

		public FixedChainStaticTargetInterceptor(List adviceChain, Object target, Class targetClass) {
			this.adviceChain = adviceChain;
			this.target = target;
			this.targetClass = targetClass;
		}

		public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
			Object retVal = null;
			MethodInvocation invocation = new CglibMethodInvocation(proxy, this.target, method, args,
					this.targetClass, this.adviceChain, methodProxy);
			// If we get here, we need to create a MethodInvocation.
			retVal = invocation.proceed();
			retVal = massageReturnTypeIfNecessary(proxy, this.target, retVal);
			return retVal;

		}
	}


	/**
	 * General purpose AOP callback. Used when the target is dynamic or when the
	 * proxy is not frozen.
	 */
	private class DynamicAdvisedInterceptor implements MethodInterceptor, Serializable {

		public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
			MethodInvocation invocation = null;
			Object oldProxy = null;
			boolean setProxyContext = false;

			Class targetClass = null; //targetSource.getTargetClass();
			Object target = null;

			try {
				Object retVal = null;

				if (advised.exposeProxy) {
					// Make invocation available if necessary.
					oldProxy = AopContext.setCurrentProxy(proxy);
					setProxyContext = true;
				}

				// May be <code>null</code>. Get as late as possible to minimize the time we
				// "own" the target, in case it comes from a pool.
				target = getTarget();
				if (target != null) {
					targetClass = target.getClass();
				}

				List chain = advised.getAdvisorChainFactory()
						.getInterceptorsAndDynamicInterceptionAdvice(advised, proxy, method, targetClass);

				// Check whether we only have one InvokerInterceptor: that is,
				// no real advice, but just reflective invocation of the target.
				if (chain.isEmpty()) {
					// We can skip creating a MethodInvocation: just invoke the target directly.
					// Note that the final invoker must be an InvokerInterceptor, so we know
					// it does nothing but a reflective operation on the target, and no hot
					// swapping or fancy proxying.
					retVal = methodProxy.invoke(target, args);
				}
				else {
					// We need to create a method invocation...
					invocation = new CglibMethodInvocation(proxy, target, method, args,
							targetClass, chain, methodProxy);

					// If we get here, we need to create a MethodInvocation.
					retVal = invocation.proceed();
				}

				retVal = massageReturnTypeIfNecessary(proxy, target, retVal);
				return retVal;
			}
			finally {
				if (target != null) {
					releaseTarget(target);
				}

				if (setProxyContext) {
					// Restore old proxy
					AopContext.setCurrentProxy(oldProxy);
				}
			}
		}

		/**
		 * CGLIB uses this to drive proxy creation.
		 */
		public int hashCode() {
			return advised.hashCode();
		}

		protected Object getTarget() throws Exception {
			return advised.getTargetSource().getTarget();
		}

		protected void releaseTarget(Object target) throws Exception {
			advised.getTargetSource().releaseTarget(target);
		}
	}


	/**
	 * Implementation of AOP Alliance MethodInvocation used by this AOP proxy.
	 */
	private static class CglibMethodInvocation extends ReflectiveMethodInvocation {

		private final MethodProxy methodProxy;

		private boolean protectedMethod;

		public CglibMethodInvocation(Object proxy, Object target, Method method, Object[] arguments,
				Class targetClass, List interceptorsAndDynamicMethodMatchers, MethodProxy methodProxy) {
			super(proxy, target, method, arguments, targetClass, interceptorsAndDynamicMethodMatchers);
			this.methodProxy = methodProxy;
			this.protectedMethod = Modifier.isProtected(method.getModifiers());
		}

		/**
		 * Gives a marginal performance improvement versus using reflection to
		 * invoke the target when invoking public methods.
		 *
		 * @see org.springframework.aop.framework.ReflectiveMethodInvocation#invokeJoinpoint
		 */
		protected Object invokeJoinpoint() throws Throwable {
			if (this.protectedMethod) {
				return super.invokeJoinpoint();
			}
			else {
				return this.methodProxy.invoke(this.target, this.arguments);
			}
		}
	}


	/**
	 * CallbackFilter to assign Callbacks to methods.
	 */
	private class ProxyCallbackFilter implements CallbackFilter {

		private final AdvisedSupport advised;

		public ProxyCallbackFilter(AdvisedSupport advised) {
			this.advised = advised;
		}

		/**
		 * Implementation of CallbackFilter.accept() to return the index of the
		 * callback we need.
		 * <p>The callbacks for each proxy are built up of a set of fixed callbacks
		 * for general use and then a set of callbacks that are specific to a method
		 * for use on static targets with a fixed advice chain.
		 * <p>The callback used is determined thus:
		 * <dl>
		 * <dt>For exposed proxies</dt>
		 * <dd>Exposing the proxy requires code to execute before and after the
		 * method/chain invocation. This means we must use
		 * DynamicAdvisedInterceptor, since all other interceptors can avoid the
		 * need for a try/catch block</dd>
		 * <dt>For Object.finalize():</dt>
		 * <dd>No override for this method is used</dd>
		 * <dt>For equals():</dt>
		 * <dd>The EqualsInterceptor is used to redirect equals() calls to a
		 * special handler to this proxy.</dd>
		 * <dt>For methods on the Advised class:</dt>
		 * <dd>the AdvisedDispatcher is used to dispatch the call directly to
		 * the target</dd>
		 * <dt>For advised methods:</dt>
		 * <dd>If the target is static and the advice chain is frozen then a
		 * FixedChainStaticTargetInterceptor specific to the method is used to
		 * invoke the advice chain. Otherwise a DyanmicAdvisedInterceptor is
		 * used.</dd>
		 * <dt>For non-advised methods:</dt>
		 * <dd>Where it can be determined that the method will not return
		 * <code>this</code> or when <code>ProxyFactory.getExposeProxy()</code> returns
		 * <code>false</code>, then a Dispatcher is used. For static targets, the StaticDispatcher
		 * is used; and for dynamic targets, a DynamicUnadvisedInterceptor is used.
		 * If it possible for the method to return <code>this</code> then a
		 * StaticUnadvisedInterceptor is used for static targets - the
		 * DynamicUnadvisedInterceptor already considers this.</dd>
		 * </dl>
		 *
		 * @see net.sf.cglib.proxy.CallbackFilter#accept(java.lang.reflect.Method)
		 */
		public int accept(Method method) {

			if (method.getDeclaringClass() == Object.class && method.getName().equals("finalize")) {
				logger.debug("Object.finalize () method found - using NO_OVERRIDE");
				return NO_OVERRIDE;
			}

			if (method.getDeclaringClass() == Advised.class) {
				if (logger.isDebugEnabled()) {
					logger.debug("Method " + method + " is declared on Advised - using DISPATCH_ADVISED");
				}
				return DISPATCH_ADVISED;
			}

			// We must always proxy equals, to direct calls to this
			if (AopUtils.isEqualsMethod(method)) {
				logger.debug("Found equals() method - using INVOKE_EQUALS");
				return INVOKE_EQUALS;
			}

			Class targetClass = this.advised.getTargetSource().getTargetClass();

			// Proxy is not yet available, but that shouldn't matter

			List chain = this.advised.getAdvisorChainFactory().getInterceptorsAndDynamicInterceptionAdvice(this.advised, null, method, targetClass);

			boolean haveAdvice = !chain.isEmpty();
			boolean exposeProxy = this.advised.isExposeProxy();
			boolean isStatic = this.advised.getTargetSource().isStatic();
			boolean isFrozen = this.advised.isFrozen();

			if (haveAdvice || !isFrozen) {
				// If exposing the proxy, then AOP_PROXY must be used.
				if (exposeProxy) {
					if (logger.isDebugEnabled()) {
						logger.debug("Must expose proxy on advised method " + method + " - using AOP_PROXY");
					}
					return AOP_PROXY;
				}

				String key = method.toString();

				// Check to see if we have fixed interceptor to serve this method.
				// Else use the AOP_PROXY.
				if (isStatic && isFrozen && fixedInterceptorMap.containsKey(key)) {
					if (logger.isDebugEnabled()) {
						logger.debug("Method " + method + " has Advice and optimisations are enabled - " +
								"using specific FixedChainStaticTargetInterceptor");
					}

					// We know that we are optimising so we can use the
					// FixedStaticChainInterceptors.
					int index = ((Integer) fixedInterceptorMap.get(key)).intValue();
					return (index + fixedInterceptorOffset);
				}
				else {
					if (logger.isDebugEnabled()) {
						logger.debug("Unable to apply any optimisations to advised method " + method +
								" - using AOP_PROXY");
					}
					return AOP_PROXY;
				}

			}
			else {
				// See if the return type of the method is outside the class hierarchy
				// of the target type. If so we know it never needs to have return type
				// massage and can use a dispatcher.

				// If the proxy is being exposed, then must use the interceptor the
				// correct one is already configured. If the target is not static cannot
				// use a Dispatcher because the target can not then be released.
				if (exposeProxy || !isStatic) {
					return INVOKE_TARGET;
				}

				Class returnType = method.getReturnType();

				if (targetClass == returnType) {
					if (logger.isDebugEnabled()) {
						logger.debug("Method " + method +
								"has return type same as target type (may return this) - using INVOKE_TARGET");
					}
					return INVOKE_TARGET;
				}
				else if (returnType.isPrimitive() || !returnType.isAssignableFrom(targetClass)) {
					if (logger.isDebugEnabled()) {
						logger.debug("Method " + method +
								" has return type that ensures this cannot be returned- using DISPATCH_TARGET");
					}
					return DISPATCH_TARGET;
				}
				else {
					if (logger.isDebugEnabled()) {
						logger.debug("Method " + method +
								"has return type that is assignable from the target type (may return this) - " +
								"using INVOKE_TARGET");
					}
					return INVOKE_TARGET;
				}
			}
		}

		public int hashCode() {
			return 0;
		}

		public boolean equals(Object other) {
			if (other == null) {
				return false;
			}
			if (other == this) {
				return true;
			}

			ProxyCallbackFilter otherCallbackFilter = null;
			if (other instanceof ProxyCallbackFilter) {
				otherCallbackFilter = (ProxyCallbackFilter) other;
			}
			else {
				// not a valid comparison
				return false;
			}

			if (this.advised.isFrozen() != otherCallbackFilter.advised.isFrozen()) {
				return false;
			}

			if (this.advised.isExposeProxy() != otherCallbackFilter.advised.isExposeProxy()) {
				return false;
			}

			if (this.advised.getTargetSource().isStatic() !=
					otherCallbackFilter.advised.getTargetSource().isStatic()) {
				return false;
			}

			return (AopProxyUtils.equalsProxiedInterfaces(this.advised, otherCallbackFilter.advised) &&
					AopProxyUtils.equalsAdvisors(advised, otherCallbackFilter.advised));
		}
	}


	/**
	 * Helper class that determines whether the CGLIB Enhancer
	 * supports the <code>setInterceptDuringConstruction</code> method
	 * (which it should as of CGLIB 2.1).
	 */
	private static class CglibUtils {

		private static boolean canSkipConstructorInterception;

		static {
			try {
				Enhancer.class.getMethod("setInterceptDuringConstruction", new Class[] {boolean.class});
				canSkipConstructorInterception = true;
			}
			catch (NoSuchMethodException ex) {
				canSkipConstructorInterception = false;
			}
		}

		public static boolean canSkipConstructorInterception() {
			return canSkipConstructorInterception;
		}
	}

}
