
 
package org.springframework.benchmark;

import java.io.InputStream;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.load.AbstractTest;

/**
 * 
 * @author Rod Johnson
 */
public abstract class AbstractBeansTest extends AbstractTest {
	
	// TODO simple bean factory, with TestBean
	
	protected final BeanFactory bf;
	
	// prototype, singleton
	
	// Logging should be off
	
	public AbstractBeansTest() throws BeansException {
			InputStream is = getClass().getResourceAsStream("/org/springframework/benchmark/beans.xml");

			this.bf = new XmlBeanFactory(is);
		}

}
