/*
 * Copyright 2002-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.aop.config;

import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.aspectj.AspectJExpressionPointcut;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.support.BeanDefinitionParser;
import org.springframework.beans.factory.xml.support.NamespaceHandlerSupport;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.List;

/**
 * @author Rob Harrop
 */
public class AopNamespaceHandler extends NamespaceHandlerSupport {

	public AopNamespaceHandler() {
		registerBeanDefinitionParser("aspects", new AspectsBeanDefinitionParser());
	}

	private static class AspectsBeanDefinitionParser implements BeanDefinitionParser {

		public static final String AUTO_PROXY_CREATOR_BEAN_NAME = ".defaultAdvisorAutoProxyCreator";

		public static final String ASPECT = "aspect";
		public static final String EXPRESSION = "expression";
		public static final String TYPE = "type";
		public static final String ID = "id";
		public static final String POINTCUT = "pointcut";
		public static final String REF = "ref";
		public static final String ADVICE = "advice";

		public void parse(Element element, BeanDefinitionRegistry registry) {
			NodeList childNodes = element.getChildNodes();

			registerAutoProxyCreator(registry);

			for (int i = 0; i < childNodes.getLength(); i++) {
				Node node = childNodes.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE && ASPECT.equals(node.getLocalName())) {
					parseAspect((Element) node, registry);
				}
			}
		}

		private void registerAutoProxyCreator(BeanDefinitionRegistry registry) {
			// TODO: factory into reusable method
			String[] beanDefinitionNames = registry.getBeanDefinitionNames();
			for (int i = 0; i < beanDefinitionNames.length; i++) {
				String beanDefinitionName = beanDefinitionNames[i];
				AbstractBeanDefinition def = (AbstractBeanDefinition) registry.getBeanDefinition(beanDefinitionName);

				if(DefaultAdvisorAutoProxyCreator.class.equals(def.getBeanClass())) {
					// already registered
					return;
				}
			}
			RootBeanDefinition definition = new RootBeanDefinition(DefaultAdvisorAutoProxyCreator.class);
			registry.registerBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME, definition);
		}

		private void parseAspect(Element aspectElement, BeanDefinitionRegistry registry) {
			String aspectName = aspectElement.getAttribute(ID);

			String pointcutBeanName = registerPointcutDefinition(aspectElement, registry);

			List adviceElements = DomUtils.getChildElementsByTagName(aspectElement, ADVICE, true);
			for (int i = 0; i < adviceElements.size(); i++) {
				Element adviceElement = (Element) adviceElements.get(i);

				// create the DefaultPointcutAdvisor definition
				RootBeanDefinition advisorDefinition = new RootBeanDefinition(DefaultPointcutAdvisor.class);

				// create the property values
				MutablePropertyValues mpvs = new MutablePropertyValues();
				advisorDefinition.setPropertyValues(mpvs);

				if (pointcutBeanName != null) {
					mpvs.addPropertyValue(POINTCUT, new RuntimeBeanReference(pointcutBeanName));
				}

				// add the advice ref to the properties
				String adviceRef = adviceElement.getAttribute(REF);
				mpvs.addPropertyValue(ADVICE, new RuntimeBeanReference(adviceRef));

				String beanName = aspectName + "." + adviceRef;
				registry.registerBeanDefinition(beanName, advisorDefinition);
			}
		}

		/**
		 * Parses a <code>&lt;pointcut&gt;</code> element, if one exists, and registers the
		 * corresponding {@link org.springframework.aop.Pointcut} in the {@link BeanDefinitionRegistry}.
		 * The schema constrains the element count for <code>&lt;pointcut&gt;</code> to either 0 or 1.
		 * Returns the bean name of {@link org.springframework.aop.Pointcut} bean definition or
		 * <code>null</code> if no <code>&lt;pointcut&gt;</code> element was found.
		 */
		private String registerPointcutDefinition(Element aspectElement, BeanDefinitionRegistry registry) {
			List pointcutElements = DomUtils.getChildElementsByTagName(aspectElement, POINTCUT, true);

			if (pointcutElements.size() > 1) {
				throw new IllegalStateException("Multiple 'pointcut' elements not allowed inside 'aspect'.");
			}
			else if (pointcutElements.size() == 1) {
				// pointcut defined
				Element pointcutElement = (Element) pointcutElements.get(0);

				BeanDefinition pointcutDefinition = createPointcutDefinition(pointcutElement);
				String id = pointcutElement.getAttribute(ID);

				if (!StringUtils.hasText(id)) {
					id = BeanDefinitionReaderUtils.generateBeanName((AbstractBeanDefinition) pointcutDefinition, registry, false);
				}

				registry.registerBeanDefinition(id, pointcutDefinition);

				return id;
			}

			return null;
		}

		protected BeanDefinition createPointcutDefinition(Element element) {
			RootBeanDefinition beanDefinition = new RootBeanDefinition();

			String type = element.getAttribute(TYPE);

			if ("aspectj".equals(type)) {
				beanDefinition.setBeanClass(AspectJExpressionPointcut.class);
				beanDefinition.setPropertyValues(new MutablePropertyValues());
				beanDefinition.getPropertyValues().addPropertyValue(EXPRESSION, element.getAttribute(EXPRESSION));
			}
			else {
				throw new IllegalArgumentException("Pointcut type [" + type + "] is unrecognised.");
			}

			return beanDefinition;
		}

		private void parseAdvice(Element adviceElement, MutablePropertyValues mpvs) {
			String adviceRef = adviceElement.getAttribute(REF);
			mpvs.addPropertyValue(ADVICE, new RuntimeBeanReference(adviceRef));
		}

	}
}
