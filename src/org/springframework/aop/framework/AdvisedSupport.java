/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.aop.framework;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.aopalliance.intercept.Interceptor;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.Advisor;
import org.springframework.aop.IntroductionAdvisor;
import org.springframework.aop.IntroductionInterceptor;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.TargetSource;
import org.springframework.aop.ThrowsAdvice;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.target.SingletonTargetSource;
import org.springframework.util.StringUtils;

/**
 * Superclass for AOP Proxy configuration managers.
 * These are not themselves AOP proxies, but
 * subclasses of this class are normally factories from which 
 * AOP proxy instances are obtained directly.
 *
 * <p>This class frees subclasses of the housekeeping of Interceptors
 * and Advisors, but doesn't actually implement proxy creation
 * methods, which are provided by subclasses.
 *
 * @author Rod Johnson
 * @version $Id: AdvisedSupport.java,v 1.26 2004-03-12 03:16:02 johnsonr Exp $
 * @see org.springframework.aop.framework.AopProxy
 */
public class AdvisedSupport extends ProxyConfig implements Advised {
	
	/**
	 * Canonical TargetSource when there's no target, and behaviour is supplied
	 * by the advisors.
	 */
	public static TargetSource EMPTY_TARGET_SOURCE = new TargetSource() {
		public Class getTargetClass() {
			return null;
		}

		public boolean isStatic() {
			return true;
		}

		public Object getTarget() {
			return null;
		}

		public void releaseTarget(Object target) {
		}
	};


	/** 
	 * List of Advice. If an Interceptor is added, it will be wrapped
	 * in an Advice before being added to this List. 
	 */
	private List advisors = new LinkedList();
	
	/**
	 * Array updated on changes to the advisors list,
	 * which is easier to manipulate internally
	 */
	private Advisor[] advisorsArray = new Advisor[0];

	/** Interfaces to be implemented by the proxy */
	private Set interfaces = new HashSet();

	protected TargetSource targetSource = EMPTY_TARGET_SOURCE;

	
	private MethodInvocationFactory methodInvocationFactory;
	
	/**
	 * Set to true when the first AOP proxy has been created, meaning that we must
	 * track advice changes via onAdviceChange() callback.
	 */
	private boolean isActive;
	
	/** List of AdvisedSupportListener */
	private LinkedList listeners = new LinkedList();
	
	protected AdvisorChainFactory advisorChainFactory;
	

	/**
	 * No arg constructor to allow use as a Java bean.
	 */
	public AdvisedSupport() {
		setAdvisorChainFactory(new HashMapCachingAdvisorChainFactory());
		//setMethodInvocationFactory(new SimpleMethodInvocationFactory());
	}
	
	/**
	 * Create a DefaultProxyConfig with the given parameters.
	 * @param interfaces the proxied interfaces
	 */
	public AdvisedSupport(Class[] interfaces) {
		// Make sure we get default advisor chain and method invocation factories
		this();
		setInterfaces(interfaces);
	}
	
	public void addListener(AdvisedSupportListener l) {
		listeners.add(l);
	}
	
	public void removeListener(AdvisedSupportListener l) {
		listeners.remove(l);
	}

	public void setTargetSource(TargetSource ts) {
		if (isActive() && getOptimize()) {
			throw new AopConfigException("Can't change target with an optimized CGLIB proxy: it has it's own target");
		}
		this.targetSource = ts;
	}
	
	public void setTarget(Object target) {
		setTargetSource(new SingletonTargetSource(target));
	}
	
	/**
	 *  @return the TargetSource. Never returns null
	 */
	public final TargetSource getTargetSource() {
		return this.targetSource;
	}
	
	public void setAdvisorChainFactory(AdvisorChainFactory advisorChainFactory) {
		this.advisorChainFactory = advisorChainFactory;
		addListener(advisorChainFactory);
	}
	
	/**
	 * Return the AdvisorChainFactory associated with this ProxyConfig.
	 */
	public final AdvisorChainFactory getAdvisorChainFactory() {
		return this.advisorChainFactory;
	}
	
	/**
	 * @return Returns the methodInvocationFactory.
	 */
	public final MethodInvocationFactory getMethodInvocationFactory() {
		return this.methodInvocationFactory;
	}
	/**
	 * @param methodInvocationFactory The methodInvocationFactory to set.
	 */
	public void setMethodInvocationFactory(MethodInvocationFactory methodInvocationFactory) {
		this.methodInvocationFactory = methodInvocationFactory;
	}
	
	/**
	 * Call this method on a new instance created by the no-arg consructor
	 * to create an independent copy of the configuration
	 * from the other.
	 * Does not copy MethodInvocationFactory; a parameter should be provided to the constructor
	 * if necessary. Note that the same MethodInvocationFactory should <b>not</b> be used
	 * for the new instance, or it may not be independent.
	 * @param other DefaultProxyConfig to copy configuration from
	 */
	protected void copyConfigurationFrom(AdvisedSupport other) {
		copyFrom(other);
		this.targetSource = other.targetSource;
		setInterfaces((Class[]) other.interfaces.toArray(new Class[other.interfaces.size()]));
		this.advisors = new LinkedList();
		for (int i = 0; i < other.advisors.size(); i++) {
			Advisor advice = (Advisor) other.advisors.get(i);
			addAdvisor(advice);
		}
	}

	
	public void addInterceptor(Interceptor interceptor) throws AopConfigException {
		int pos = (this.advisors != null) ? this.advisors.size() : 0;
		addInterceptor(pos, interceptor);
	}
	
	public boolean isInterfaceProxied(Class intf) {
		return this.interfaces.contains(intf);
	}

	/**
	 * Cannot add IntroductionInterceptors this way.
	 */
	public void addInterceptor(int pos, Interceptor interceptor) throws AopConfigException {
		if (!(interceptor instanceof MethodInterceptor)) {
			throw new AopConfigException(getClass().getName() + " only handles MethodInterceptors");
		}
		if (interceptor instanceof IntroductionInterceptor) {
			throw new AopConfigException("IntroductionInterceptors may only be added as part of IntroductionAdvice");
		}
		addAdvisor(pos, new DefaultPointcutAdvisor(interceptor));
	}
	
	public void addBeforeAdvice(final MethodBeforeAdvice ba) throws AopConfigException {
		addAdvisor(new DefaultPointcutAdvisor(Pointcut.TRUE, ba));
	}
	
	public void addThrowsAdvice(final ThrowsAdvice throwsAdvice) throws AopConfigException {
		addAdvisor(new DefaultPointcutAdvisor(throwsAdvice));
	}
	
	/**
	 * Return the index (from 0) of the given AOP Alliance interceptor,
	 * or -1 if no such interceptor is an advice for this proxy.
	 * The return value of this method can be used to index into
	 * the Advisors array.
	 * @param interceptor AOP Alliance interceptor to search for
	 * @return index from 0 of this interceptor, or -1 if there's
	 * no such advice.
	 */
	public int indexOf(Interceptor interceptor) {
		for (int i = 0; i < this.advisors.size(); i++) {
			Advisor advisor = (Advisor) this.advisors.get(i);
			if (advisor.getAdvice() == interceptor) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Return the index (from 0) of the given advisor,
	 * or -1 if no such advisor applies to this proxy.
	 * The return value of this method can be used to index into
	 * the Advisors array.
	 * @param advisor advisor to search for
	 * @return index from 0 of this advisor, or -1 if there's
	 * no such advisor.
	 */
	public int indexOf(Advisor advisor) {
		return this.advisors.indexOf(advisor);
	}

	public final boolean removeAdvisor(Advisor advisor) {
		int index = indexOf(advisor);
		if (index == -1) {
			return false;
		}
		else {
			removeAdvisor(index);
			return true;
		}
	}
	
	public void removeAdvisor(int index) throws AopConfigException {
		if (isFrozen())
			throw new AopConfigException("Cannot remove Advisor: config is frozen");
		if (index < 0 || index > advisors.size() - 1)
			throw new AopConfigException("Advisor index " + index + " is out of bounds: " +					"Only have " + advisors.size() + " advisors");
		Advisor advisor = (Advisor) advisors.get(index);
		if (advisor instanceof IntroductionAdvisor) {
			IntroductionAdvisor ia = (IntroductionAdvisor) advisor;
			// We need to remove interfaces
			for (int j = 0; j < ia.getInterfaces().length; j++) {
				removeInterface(ia.getInterfaces()[j]);
			}
		}
		
		this.advisors.remove(index);
		updateAdvisorsArray();
		adviceChanged();
	}

	/**
	 * Convenience method to remove an interceptor
	 */
	public final boolean removeInterceptor(Interceptor interceptor) throws AopConfigException {
		int index = indexOf(interceptor);
		if (index == -1) {
			return false;
		}
		else {
			removeAdvisor(index);
			return true;
		}
	}

	/**
	 * Set the interfaces to be proxied.
	 * @param interfaces the interfaces to set
	 */
	public void setInterfaces(Class[] interfaces) {
		this.interfaces.clear();
		for (int i = 0; i < interfaces.length; i++) {
			addInterface(interfaces[i]);
		}
	}

	/**
	 * Add a new proxied interface.
	 * @param newInterface additional interface to proxy
	 */
	public void addInterface(Class newInterface) {
		this.interfaces.add(newInterface);
		adviceChanged();
		logger.debug("Added new aspect interface: " + newInterface);
	}

	/**
	 * Remove a proxied interface.
	 * Does nothing if it isn't proxied.
	 */
	public boolean removeInterface(Class intf) {
		return this.interfaces.remove(intf);
	}

	public final Class[] getProxiedInterfaces() {
		//return (Class[]) this.interfaces.toArray();
		Class[] classes = new Class[this.interfaces.size()];
		int i = 0;
		for (Iterator itr = this.interfaces.iterator(); itr.hasNext() ;) {
			Class clazz = (Class) itr.next();
			classes[i++] = clazz;
		}
		return classes;
	}


	private void addAdvisorInternal(int pos, Advisor advice) throws AopConfigException {
		if (isFrozen())
			throw new AopConfigException("Cannot add advisor: config is frozen");
		this.advisors.add(pos, advice);
		updateAdvisorsArray();
		adviceChanged();
	}
	
	public void addAdvisor(int pos, IntroductionAdvisor advisor) throws AopConfigException {
		advisor.validateInterfaces();
		
		// If the advisor passed validation we can make the change	 
		for (int i = 0; i < advisor.getInterfaces().length; i++) {
			 addInterface(advisor.getInterfaces()[i]);
		 }
		addAdvisorInternal(pos, advisor);
	}

	public void addAdvisor(int pos, Advisor advisor) throws AopConfigException {
		if (advisor instanceof IntroductionAdvisor) {
			addAdvisor(pos, (IntroductionAdvisor) advisor);
		}
		else {
			addAdvisorInternal(pos, advisor);
		}
	}
	
	public void addAdvisor(Advisor advice) {
		int pos = this.advisors.size();
		addAdvisor(pos, advice);
	}

	/**
	 * Bring the array up to date with the list
	 *
	 */
	private void updateAdvisorsArray() {
		this.advisorsArray = (Advisor[]) this.advisors.toArray(new Advisor[this.advisors.size()]);
	}
	
	public final Advisor[] getAdvisors() {
		return this.advisorsArray;
	}

	/**
	 * Replace the given advisor.
	 * <b>NB:</b>If the advisor is an IntroductionAdvisor
	 * and the replacement is not or implements different interfaces,
	 * the proxy will need to be re-obtained or the old interfaces
	 * won't be supported and the new interface won't be implemented.
	 * @param a advisor to replace
	 * @param b advisor to replace it with
	 * @return whether it was replaced. If the advisor wasn't found in the
	 * list of advisors, this method returns false and does nothing.
	 */
	public final boolean replaceAdvisor(Advisor a, Advisor b) throws AopConfigException {
		int index = indexOf(a);
		if (index == -1 || b == null)
			return false;
		removeAdvisor(index);
		addAdvisor(index, b);
		return true;
	}

	/**
	 * Is this interceptor included in any advisor?
	 * @param mi interceptor to check inclusion of
	 * @return whether this interceptor instance could be run in an invocation
	 */
	public final boolean interceptorIncluded(Interceptor mi) {
		if (this.advisors.size() == 0)
			return false;
		for (int i = 0; i < this.advisors.size(); i++) {
			Advisor advice = (Advisor) this.advisors.get(i);
			if (advice.getAdvice() == mi)
				return true;
		}
		return false;
	}

	/**
	 * Count interceptors of the given class
	 * @param interceptorClass class of the interceptor to check
	 * @return the count of the interceptors of this class or subclasses
	 */
	public final int countInterceptorsOfType(Class interceptorClass) {
		if (this.advisors.size() == 0)
			return 0;
		int count = 0;
		for (int i = 0; i < this.advisors.size(); i++) {
			Advisor advisor = (Advisor) this.advisors.get(i);
			if (interceptorClass.isAssignableFrom(advisor.getAdvice().getClass()))
				++count;
		}
		return count;
	}
	
	/**
	 * Invoked when advice has changed.
	 */
	private synchronized void adviceChanged() {
		if (this.isActive) {
			for (int i = 0; i < listeners.size(); i++) {
				((AdvisedSupportListener) listeners.get(i)).adviceChanged(this);
			}
		}
	}
	
	private void activate() {
		this.isActive = true;
		for (int i = 0; i < listeners.size(); i++) {
			((AdvisedSupportListener) listeners.get(i)).activated(this);
		}
	}
	

	/**
	 * Subclasses should call this to get a new AOP proxy. They should <b>not</b>
	 * create an AOP proxy with this as an argument.
	 */
	protected synchronized AopProxy createAopProxy() {
		if (!isActive) {
			activate();
		}
		
		return getAopProxyFactory().createAopProxy(this);
	}
	
	/**
	 * Subclasses can call this to check whether any AOP proxies have been created yet.
	 */
	protected final boolean isActive() {
		return isActive;
	}

	public String toProxyConfigString() {
		return toString();
	}

	/**
	 * For debugging/diagnostic use.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer(getClass().getName() + ": ");
		sb.append(this.interfaces.size() + " interfaces=[" + StringUtils.collectionToCommaDelimitedString(this.interfaces) + "]; ");
		sb.append(this.advisors.size() + " pointcuts=[" + StringUtils.collectionToCommaDelimitedString(this.advisors) + "]; ");
		sb.append("targetSource=[" + this.targetSource + "]; ");
		sb.append("advisorChainFactory=" + advisorChainFactory);
		sb.append(super.toString());
		return sb.toString();
	}

}
