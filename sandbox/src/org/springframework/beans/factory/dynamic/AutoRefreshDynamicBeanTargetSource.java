package org.springframework.beans.factory.dynamic;

import org.springframework.beans.factory.BeanFactory;

/**
 * TargetSource that can apply to singletons,
 * reloading them on request from a child factory.
 * @author Rod Johnson
 * @version $Id: AutoRefreshDynamicBeanTargetSource.java,v 1.1 2004-08-04 16:49:47 johnsonr Exp $
 */
public class AutoRefreshDynamicBeanTargetSource extends DynamicBeanTargetSource implements DynamicObject {

	private long expiry;
	
	private ExpirableObject eo;
	
	private long lastCheck = System.currentTimeMillis();
	
	private boolean autoRefresh;
	
	public AutoRefreshDynamicBeanTargetSource(Object initialTarget, BeanFactory factory, String beanName, ExpirableObject eo) {
		super(initialTarget, factory, beanName);
		this.eo = eo;
	}
	
	public void setExpirySeconds(int expirySeconds) {
		this.expiry = 1000 * expirySeconds;
	}
	
	/**
	 * @see org.springframework.aop.TargetSource#getTarget()
	 */
	public synchronized Object getTarget() {
		if (autoRefresh &&
				System.currentTimeMillis() - lastCheck > expiry) {
			if (isModified()) {
				refresh();
			}
			lastCheck = System.currentTimeMillis();
		}
		return this.target;
	}


	/**
	 * @see org.springframework.beans.factory.dynamic.ExpirableObject#isModified()
	 */
	public boolean isModified() {
		return eo.isModified();
	}

	/**
	 * @see org.springframework.beans.factory.dynamic.DynamicObject#getExpiry()
	 */
	public long getExpiry() {
		return expiry;
	}

	/**
	 * @see org.springframework.beans.factory.dynamic.DynamicObject#isAutoRefresh()
	 */
	public boolean isAutoRefresh() {
		return autoRefresh;
	}

	/**
	 * @see org.springframework.beans.factory.dynamic.DynamicObject#setAutoRefresh(boolean)
	 */
	public synchronized void setAutoRefresh(boolean autoRefresh) {
		this.autoRefresh = autoRefresh;
	}
}