package org.springframework.jndi.support;

import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.util.xml.DomUtils;
import org.springframework.jndi.JndiObjectFactoryBean;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Rob Harrop
 * @since 2.0
 */
public class JndiNamespaceHandler extends NamespaceHandlerSupport {

	public JndiNamespaceHandler() {
		registerBeanDefinitionParser("lookup", new LookupBeanDefinitionParser());
	}

	private static class LookupBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

		public static final String ENVIRONMENT = "environment";

		protected Class getBeanClass(Element element) {
			return JndiObjectFactoryBean.class;
		}

		protected void postProcess(RootBeanDefinition definition, Element element) {
			NodeList childNodes = element.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++) {
				Node node = childNodes.item(i);
				if (ENVIRONMENT.equals(node.getLocalName())) {
					definition.getPropertyValues().addPropertyValue("jndiEnvironment", DomUtils.getTextValue((Element) node));
				}
			}
		}
	}
}
