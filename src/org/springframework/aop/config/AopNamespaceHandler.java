package org.springframework.aop.config;

import org.springframework.beans.factory.xml.support.NamespaceHandlerSupport;
import org.springframework.beans.factory.xml.support.BeanDefinitionParser;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

/**
 * @author Rob Harrop
 */
public class AopNamespaceHandler extends NamespaceHandlerSupport {

	public AopNamespaceHandler() {
	}

	private static class AspectsBeanDefinitionParser implements BeanDefinitionParser {

		public static final String ASPECT = "aspect";

		public void parse(Element element, BeanDefinitionRegistry registry) {
			NodeList childNodes = element.getChildNodes();
			for(int i = 0; i < childNodes.getLength(); i++) {
			  Node node = childNodes.item(i);
				if(node.getNodeType() == Node.ELEMENT_NODE && ASPECT.equals(node.getNodeName())) {
					parseAspect((Element)node, registry);
				}
			}
		}

		private void parseAspect(Element aspectElement, BeanDefinitionRegistry registry) {
			RootBeanDefinition beanDefinition = new RootBeanDefinition(DefaultPointcutAdvisor.class);

			NodeList children = aspectElement.getChildNodes();
			for(int i = 0; i < children.getLength(); i++) {
			  Node node = children.item(i);
			  
			}
		}
	}
}
