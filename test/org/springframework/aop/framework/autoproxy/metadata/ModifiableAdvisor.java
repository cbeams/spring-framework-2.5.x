/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.aop.framework.autoproxy.metadata;

import java.util.Collection;
import java.util.Iterator;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.AopConfigException;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.aop.support.DefaultInterceptionIntroductionAdvisor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.metadata.Attributes;

/**
 * 
 * @author Rod Johnson
 * @version $Id: ModifiableAdvisor.java,v 1.3 2004-01-21 20:21:36 johnsonr Exp $
 */
public class ModifiableAdvisor extends DefaultInterceptionIntroductionAdvisor implements InitializingBean {

	protected final Log log = LogFactory.getLog(getClass());
	
	private Attributes attributes;

	public ModifiableAdvisor() {
		super(new ModifiableIntroductionInterceptor(), Modifiable.class);
	}


	public void setAttributes(Attributes atts) {
		this.attributes = atts;
	}

	private boolean matches(Collection c) {
		//log.info("Checking for modifiable attribute; atts.length=" + atts.size());
		for (Iterator itr = c.iterator(); itr.hasNext(); ) {
			Object next = itr.next();
			if (next instanceof ModifiableAttribute) {
				log.info("FOUND modifiable attribute " + next);
				return true;
			}
		}
		return false;
	}

	/**
	 * @see org.springframework.core.Ordered#getOrder()
	 */
	public int getOrder() {
		throw new UnsupportedOperationException();
	}


	/**
	 * @see org.springframework.aop.ClassFilter#matches(java.lang.Class)
	 */
	public boolean matches(Class targetClass) {
		log.info("Checking for mod attribute on " + targetClass + " attributes=" + attributes);
		Collection atts = this.attributes.getAttributes(targetClass);
		return matches(atts);
	}



	private static class ModifiableIntroductionInterceptor
		extends DelegatingIntroductionInterceptor
		implements Modifiable {
			
		private boolean dirty = false;
		/**
		 * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
		 */
		public Object invoke(MethodInvocation mi) throws Throwable {
			if (!isMethodOnIntroducedInterface(mi)) {
				if (!mi.getMethod().getName().startsWith("get")) {
					this.dirty = true;
				}
			}
			return super.invoke(mi);
		}

		/**
			 * @see org.springframework.enterpriseservices.mod.Modifable#isModified()
			 */
		public boolean isModified() {
			logger.info("isModified");
			return this.dirty;
		}

		public void acceptChanges() {
			logger.info("Accepting changes");
			this.dirty = false;
		}
	}

	/**
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		if (this.attributes == null)
			throw new AopConfigException("Must set Attributes property on ModifiableAdvice");
	}
}
