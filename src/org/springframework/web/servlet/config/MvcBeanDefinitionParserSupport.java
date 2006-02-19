package org.springframework.web.servlet.config;

import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

public class MvcBeanDefinitionParserSupport {

	protected void setPropertyIfAvailable(Element el, String attribute, String property, RootBeanDefinition definition) {
		String propertyValue = el.getAttribute(attribute);
		if (StringUtils.hasText(propertyValue)) {
			definition.getPropertyValues().addPropertyValue(property, propertyValue);
		}
	}
	
	

}
