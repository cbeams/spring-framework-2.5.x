/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.enterpriseservices.mod;

import java.util.List;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.AopConfigException;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.aop.support.SimpleIntroductionAdvisor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.enterpriseservices.MetadataDriven;
import org.springframework.metadata.Attributes;

/**
 * 
 * @author Rod Johnson
 * @version $Id: ModifiableAdvice.java,v 1.2 2003-12-02 22:28:40 johnsonr Exp $
 */
public class ModifiableAdvice extends SimpleIntroductionAdvisor implements MetadataDriven, InitializingBean {

	protected final Log log = LogFactory.getLog(getClass());
	
	private Attributes attributes;

	public ModifiableAdvice() {
		super(new ModifiableIntroductionInterceptor(), Modifiable.class);
	}


	public void setAttributes(Attributes atts) {
		this.attributes = atts;
	}

	private boolean matches(List atts) {
		log.info("Checking for modifiable attribute; atts.length=" + atts.size());
		for (int i = 0; i < atts.size(); i++) {
			if (atts.get(i) instanceof ModifiableAttribute) {
				log.info("FOUND modifiable attribute " + atts.get(i));
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
		List atts = this.attributes.getAttributes(targetClass);
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
