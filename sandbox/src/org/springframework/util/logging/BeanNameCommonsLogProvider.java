/*
 * Created on Nov 7, 2004
 */
package org.springframework.util.logging;

import org.apache.axis.components.logger.LogFactory;
import org.apache.commons.logging.Log;

/**
 * @author robh
 *
 */
public class BeanNameCommonsLogProvider implements CommonsLogProvider {

	private boolean includeClassName = false;
	
	public Log getLogForBean(Object bean, String beanName) {
		if(includeClassName) {
			return LogFactory.getLog(bean.getClass().getName() + " [" + beanName + "]");
		} else {
			return LogFactory.getLog(beanName);
		}
	}
	
	public void setIncludeClassName(boolean includeClassName) {
		this.includeClassName = includeClassName;
	}

}
