package org.springframework.beans.factory.config;

import org.springframework.beans.factory.xml.support.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.support.NamespaceHandlerSupport;
import org.w3c.dom.Element;

/**
 * @author Rob Harrop
 * @since 2.0
 */
public class UtilNamespaceHandler extends NamespaceHandlerSupport {

	public UtilNamespaceHandler() {
		registerBeanDefinitionParser("properties", new PropertiesBeanDefinitionParser());
	}

	public static class PropertiesBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

		protected Class getBeanClass(Element element) {
			return PropertiesFactoryBean.class;
		}
	}
}
