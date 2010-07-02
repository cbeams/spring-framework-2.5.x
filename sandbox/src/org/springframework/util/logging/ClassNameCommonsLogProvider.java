/*
 * Created on Nov 6, 2004
 */
package org.springframework.util.logging;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author robh
 *
 */
public class ClassNameCommonsLogProvider implements CommonsLogProvider {

	public Log getLogForBean(Object bean, String beanName) {
		return LogFactory.getLog(bean.getClass());
	}

}
