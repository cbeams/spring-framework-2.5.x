/*
 * Copyright 2002-2007 the original author or authors.
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.aopalliance.aop.Advice;
import org.apache.commons.logging.LogFactory;

import org.springframework.aop.Advisor;
import org.springframework.aop.DynamicIntroductionAdvice;
import org.springframework.aop.IntroductionAdvisor;
import org.springframework.aop.IntroductionInfo;
import org.springframework.aop.TargetSource;
import org.springframework.aop.support.DefaultIntroductionAdvisor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.target.EmptyTargetSource;
import org.springframework.aop.target.SingletonTargetSource;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * Base class for AOP proxy configuration managers.
 * These are not themselves AOP proxies, but subclasses of this class are
 * normally factories from which AOP proxy instances are obtained directly.
 *
 * <p>This class frees subclasses of the housekeeping of Advices
 * and Advisors, but doesn't actually implement proxy creation
 * methods, which are provided by subclasses.
 *
 * <p>This class is serializable; subclasses need not be.
 * This class is used to hold snapshots of proxies.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.aop.framework.AopProxy
 */
public class AdvisedSupport extends ProxyConfig implements Advised {

	/**
	 * Canonical TargetSource when there's no target, and behavior is
	 * supplied by the advisors.
	 */
	public static final TargetSource EMPTY_TARGET_SOURCE = EmptyTargetSource.INSTANCE;


	/** List of AdvisedSupportListener */
	private transient List listeners = new LinkedList();

	/**
	 * Package-protected to allow direct access for efficiency
	 */
	TargetSource targetSource = EMPTY_TARGET_SOURCE;

	transient AdvisorChainFactory advisorChainFactory;

	/**
	 * List of Advice. If an Interceptor is added, it will be wrapped
	 * in an Advice before being added to this List.
	 */
	private List advisors = new LinkedList();

	/**
	 * Array updated on changes to the advisors list, which is easier
	 * to manipulate internally.
	 */
	private Advisor[] advisorArray = new Advisor[0];

	/**
	 * Interfaces to be implemented by the proxy. Held in List to keep the order
	 * of registration, to create JDK proxy with specified order of interfaces.
	 */
	private List interfaces = new ArrayList();

	/**
	 * Set to true when the first AOP proxy has been created, meaning that we
	 * must track advice changes via onAdviceChange callback.
	 */
	private transient boolean active;


	/**
	 * No-arg constructor for use as a JavaBean.
	 */
	public AdvisedSupport() {
		initDefaultAdvisorChainFactory();
	}

	/**
	 * Create a AdvisedSupport instance with the given parameters.
	 * @param interfaces the proxied interfaces
	 */
	public AdvisedSupport(Class[] interfaces) {
		this();
		setInterfaces(interfaces);
	}

	/**
	 * Initialize the default AdvisorChainFactory.
	 */
	private void initDefaultAdvisorChainFactory() {
		setAdvisorChainFactory(new HashMapCachingAdvisorChainFactory());
	}


	public void addListener(AdvisedSupportListener listener) {
		this.listeners.add(listener);
	}

	public void removeListener(AdvisedSupportListener listener) {
		this.listeners.remove(listener);
	}

	/**
	 * Set the given object as target.
	 * Will create a SingletonTargetSource for the object.
	 * @see #setTargetSource
	 * @see org.springframework.aop.target.SingletonTargetSource
	 */
	public void setTarget(Object target) {
		setTargetSource(new SingletonTargetSource(target));
	}

	public void setTargetSource(TargetSource targetSource) {
		if (isActive() && isOptimize()) {
			throw new AopConfigException("Cannot change target with an optimized CGLIB proxy: It has its own target.");
		}
		this.targetSource = (targetSource != null ? targetSource : EMPTY_TARGET_SOURCE);
	}

	public TargetSource getTargetSource() {
		return this.targetSource;
	}

	public Class getTargetClass() {
		return this.targetSource.getTargetClass();
	}

	public void setAdvisorChainFactory(AdvisorChainFactory advisorChainFactory) {
		this.advisorChainFactory = advisorChainFactory;
		addListener(advisorChainFactory);
	}

	public AdvisorChainFactory getAdvisorChainFactory() {
		return this.advisorChainFactory;
	}


	/**
	 * Call this method on a new instance created by the no-arg constructor
	 * to create an independent copy of the configuration from the other.
	 * @param other AdvisedSupport to copy configuration from
	 */
	protected void copyConfigurationFrom(AdvisedSupport other) {
		copyConfigurationFrom(other, other.targetSource, other.advisors);
	}

	/**
	 * Take interfaces and ProxyConfig configuration from the
	 * other AdvisedSupport, but allow substitution of a fresh
	 * TargetSource and interceptor chain
	 * @param other other AdvisedSupport object to take
	 * interfaces and ProxyConfig superclass configuration from
	 * @param ts new TargetSource
	 * @param pAdvisors new Advisor chain
	 */
	protected void copyConfigurationFrom(AdvisedSupport other, TargetSource ts, List pAdvisors) {
		copyFrom(other);
		this.targetSource = ts;
		setInterfaces((Class[]) other.interfaces.toArray(new Class[other.interfaces.size()]));
		this.advisors = new LinkedList();
		for (int i = 0; i < pAdvisors.size(); i++) {
			Advisor advice = (Advisor) pAdvisors.get(i);
			addAdvisor(advice);
		}
	}

	/**
	 * Set the interfaces to be proxied.
	 */
	public void setInterfaces(Class[] interfaces) {
		Assert.notNull(interfaces, "Interfaces must not be null");
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
		Assert.notNull(newInterface, "Interface must not be null");
		if (!newInterface.isInterface()) {
			throw new IllegalArgumentException("[" + newInterface.getName() + "] is not an interface");
		}
		if (!this.interfaces.contains(newInterface)) {
			this.interfaces.add(newInterface);
			adviceChanged();
			if (logger.isDebugEnabled()) {
				logger.debug("Added new aspect interface: " + newInterface.getName());
			}
		}
	}

	public Class[] getProxiedInterfaces() {
		return (Class[]) this.interfaces.toArray(new Class[this.interfaces.size()]);
	}

	/**
	 * Remove a proxied interface.
	 * <p>Does nothing if the given interface isn't proxied.
	 */
	public boolean removeInterface(Class intf) {
		return this.interfaces.remove(intf);
	}

	public void addAdvice(Advice advice) throws AopConfigException {
		int pos = (this.advisors != null) ? this.advisors.size() : 0;
		addAdvice(pos, advice);
	}

	public boolean isInterfaceProxied(Class intf) {
		for (Iterator it = this.interfaces.iterator(); it.hasNext();) {
			Class proxyIntf = (Class) it.next();
			if (intf.isAssignableFrom(proxyIntf)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Cannot add introductions this way unless the advice implements IntroductionInfo.
	 */
	public void addAdvice(int pos, Advice advice) throws AopConfigException {
		if (advice instanceof IntroductionInfo) {
			// We don't need an IntroductionAdvisor for this kind of introduction:
			// it's fully self-describing
			addAdvisor(pos, new DefaultIntroductionAdvisor(advice, (IntroductionInfo) advice));
		}
		else if (advice instanceof DynamicIntroductionAdvice) {
			// We need an IntroductionAdvisor for this kind of introduction
			throw new AopConfigException("DynamicIntroductionAdvice may only be added as part of IntroductionAdvisor");
		}
		else {
			addAdvisor(pos, new DefaultPointcutAdvisor(advice));
		}
	}

	/**
	 * Remove the Advisor containing the given advice.
	 * @param advice advice to remove
	 * @return whether the Advice was found and removed
	 * (<code>false</code> if there was no such advice)
	 */
	public final boolean removeAdvice(Advice advice) throws AopConfigException {
		int index = indexOf(advice);
		if (index == -1) {
			return false;
		}
		else {
			removeAdvisor(index);
			return true;
		}
	}

	/**
	 * Return the index (from 0) of the given AOP Alliance Advice,
	 * or -1 if no such advice is an advice for this proxy.
	 * <p>The return value of this method can be used to index into
	 * the advisors array.
	 * @param advice AOP Alliance advice to search for
	 * @return index from 0 of this advice, or -1 if there's no such advice
	 */
	public int indexOf(Advice advice) {
		for (int i = 0; i < this.advisors.size(); i++) {
			Advisor advisor = (Advisor) this.advisors.get(i);
			if (advisor.getAdvice() == advice) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Return the index (from 0) of the given advisor,
	 * or -1 if no such advisor applies to this proxy.
	 * <p>The return value of this method can be used to index
	 * into the advisors array.
	 * @param advisor advisor to search for
	 * @return index from 0 of this advisor, or -1 if there's no such advisor
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
		if (isFrozen()) {
			throw new AopConfigException("Cannot remove Advisor: Configuration is frozen.");
		}
		if (index < 0 || index > this.advisors.size() - 1) {
			throw new AopConfigException("Advisor index " + index + " is out of bounds: " +
					"This configuration only has " + this.advisors.size() + " advisors.");
		}

		Advisor advisor = (Advisor) this.advisors.get(index);
		if (advisor instanceof IntroductionAdvisor) {
			IntroductionAdvisor ia = (IntroductionAdvisor) advisor;
			// we need to remove interfaces
			for (int j = 0; j < ia.getInterfaces().length; j++) {
				removeInterface(ia.getInterfaces()[j]);
			}
		}
		
		this.advisors.remove(index);
		updateAdvisorArray();
		adviceChanged();
	}

	private void addAdvisorInternal(int pos, Advisor advice) throws AopConfigException {
		if (isFrozen()) {
			throw new AopConfigException("Cannot add advisor: Configuration is frozen.");
		}
		if (pos > this.advisors.size()) {
			throw new IllegalArgumentException(
					"Illegal position " + pos + " in advisor list with size " + this.advisors.size());
		}
		this.advisors.add(pos, advice);
		updateAdvisorArray();
		adviceChanged();
	}

	public void addAdvisor(int pos, IntroductionAdvisor advisor) throws AopConfigException {
		advisor.validateInterfaces();

		// If the advisor passed validation, we can make the change.
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

	public void addAdvisor(Advisor advisor) {
		int pos = this.advisors.size();
		addAdvisor(pos, advisor);
	}

	public void addAllAdvisors(Advisor[] advisors) {
		for (int i = 0; i < advisors.length; i++) {
			Advisor advisor = advisors[i];
			this.advisors.add(advisor);
		}
		updateAdvisorArray();
		adviceChanged();
	}

	/**
	 * Bring the array up to date with the list.
	 */
	protected final void updateAdvisorArray() {
		this.advisorArray = (Advisor[]) this.advisors.toArray(new Advisor[this.advisors.size()]);
	}

	public final Advisor[] getAdvisors() {
		return this.advisorArray;
	}

	/**
	 * Allows uncontrolled access to the {@link List} of {@link Advisor Advisors}.
	 * Use with care, and remember to {@link #updateAdvisorArray() refresh the advisor array}
	 * and {@link #adviceChanged() fire advice changed events} when making any modifications.
	 */
	protected final List getAdvisorsInternal() {
		return this.advisors;
	}

	/**
	 * Replace the given advisor.
	 * <p><b>NB:</b> If the advisor is an IntroductionAdvisor
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
		if (index == -1 || b == null) {
			return false;
		}
		removeAdvisor(index);
		addAdvisor(index, b);
		return true;
	}

	/**
	 * Is this advice included in any advisor?
	 * @param advice advice to check inclusion of
	 * @return whether this advice instance could be run in an invocation
	 */
	public final boolean adviceIncluded(Advice advice) {
		if (this.advisors.size() == 0) {
			return false;
		}
		for (int i = 0; i < this.advisors.size(); i++) {
			Advisor advisor = (Advisor) this.advisors.get(i);
			if (advisor.getAdvice() == advice) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Count advices of the given class.
	 * @param adviceClass the advice class to check
	 * @return the count of the interceptors of this class or subclasses
	 */
	public final int countAdvicesOfType(Class adviceClass) {
		Assert.notNull(adviceClass, "Advice class must not be null");
		if (this.advisors.size() == 0) {
			return 0;
		}
		int count = 0;
		for (int i = 0; i < this.advisors.size(); i++) {
			Advisor advisor = (Advisor) this.advisors.get(i);
			if (advisor.getAdvice() != null &&
					adviceClass.isAssignableFrom(advisor.getAdvice().getClass())) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Invoked when advice has changed.
	 */
	protected final synchronized void adviceChanged() {
		if (this.active) {
			for (int i = 0; i < this.listeners.size(); i++) {
				((AdvisedSupportListener) this.listeners.get(i)).adviceChanged(this);
			}
		}
	}

	private void activate() {
		this.active = true;
		for (int i = 0; i < this.listeners.size(); i++) {
			((AdvisedSupportListener) this.listeners.get(i)).activated(this);
		}
	}

	/**
	 * Subclasses should call this to get a new AOP proxy. They should <b>not</b>
	 * create an AOP proxy with this as an argument.
	 */
	protected synchronized AopProxy createAopProxy() {
		if (!this.active) {
			activate();
		}
		return getAopProxyFactory().createAopProxy(this);
	}
	
	/**
	 * Subclasses can call this to check whether any AOP proxies have been created yet.
	 */
	protected final boolean isActive() {
		return this.active;
	}
	

	//---------------------------------------------------------------------
	// Serialization support
	//---------------------------------------------------------------------
	
	/**
	 * Serializes a copy of the state of this class, ignoring subclass state.
	 */
	protected Object writeReplace() {
		if (logger.isDebugEnabled()) {
			logger.debug("Disconnecting " + this);
		}

		// Copy state to avoid dependencies on BeanFactory etc that subclasses may have.
		AdvisedSupport copy = this;

		// If we're in a non-serializable subclass, copy into an AdvisedSupport object.
		if (!getClass().equals(AdvisedSupport.class)) {
			copy = new AdvisedSupport();
			copy.copyConfigurationFrom(this);
		}

		// May return this.
		return copy;
	}
	 
	/**
	 * Initializes transient fields.
	 */
	private Object readResolve() {
		this.logger = LogFactory.getLog(getClass());
		this.active = true;
		this.listeners = new LinkedList();
		initDefaultAdvisorChainFactory();
		return this;
	}
	
	
	public String toProxyConfigString() {
		return toString();
	}

	/**
	 * For debugging/diagnostic use.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer(getClass().getName() + ": ");
		sb.append(this.interfaces.size()).append(" interfaces ");
		sb.append(ClassUtils.classNamesToString(this.interfaces)).append("; ");
		sb.append(this.advisors.size()).append(" advisors ");
		sb.append(this.advisors).append("; ");
		sb.append("targetSource [").append(this.targetSource).append("]; ");
		sb.append("advisorChainFactory [").append(this.advisorChainFactory);
		sb.append("]; ").append(super.toString());
		return sb.toString();
	}

}
