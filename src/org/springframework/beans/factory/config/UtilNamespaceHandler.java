package org.springframework.beans.factory.config;

import org.springframework.beans.factory.xml.support.NamespaceHandlerSupport;
import org.springframework.beans.factory.xml.support.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.support.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.MutablePropertyValues;
import org.w3c.dom.Element;

/**
 * @author Rob Harrop
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
