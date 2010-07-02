package org.springframework.util.logging;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Rob Harrop
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
