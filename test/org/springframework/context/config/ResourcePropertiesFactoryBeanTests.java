package org.springframework.context.config;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.config.PropertiesFactoryBeanTests;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.util.ClassLoaderUtils;

/**
 * @author Juergen Hoeller
 * @since 01.11.2003
 */
public class ResourcePropertiesFactoryBeanTests extends PropertiesFactoryBeanTests {

	protected PropertiesFactoryBean getPropertiesFactoryBean() {
		ApplicationContext ac = new StaticApplicationContext() {
			protected InputStream getResourceByPath(String path) throws IOException {
				return ClassLoaderUtils.getResourceAsStream(getClass(), path);
			}
		};
		ResourcePropertiesFactoryBean pfb = new ResourcePropertiesFactoryBean();
		pfb.setApplicationContext(ac);
		return pfb;
	}

}
