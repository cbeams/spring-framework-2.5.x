/*
 * Created on Nov 5, 2004
 */
package org.springframework.util.logging;

import org.apache.commons.logging.Log;

/**
 * @author robh
 *
 */
public interface CommonsLogProvider {

	Log getLogForBean(Object bean, String beanName);
}
