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
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Rob Harrop
 */
public class AopNamespaceHandler extends NamespaceHandlerSupport {

	public AopNamespaceHandler() {
		registerBeanDefinitionParser("config", new ConfigBeanDefinitionParser());
	}

	private static class ConfigBeanDefinitionParser implements BeanDefinitionParser {

		private static final String ASPECT = "aspect";

		private static final String EXPRESSION = "expression";

		private static final String ID = "id";

		private static final String POINTCUT = "pointcut";

		private static final String ADVICE = "advice";

		private static final String ADVISOR = "advisor";

		private static final String ADVICE_REF = "advice-ref";

		private static final String POINTCUT_REF = "pointcut-ref";

		public void parse(Element element, BeanDefinitionRegistry registry) {
			NodeList childNodes = element.getChildNodes();

			NamespaceHandlerUtils.registerAutoProxyCreatorIfNecessary(registry);

			for (int i = 0; i < childNodes.getLength(); i++) {
				Node node = childNodes.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					String localName = node.getLocalName();
					if (POINTCUT.equals(localName)) {
						parsePointcut((Element) node, registry);
					}
					else if (ADVISOR.equals(localName)) {
						parseAdvisor((Element) node, registry);
					}
					else if (ASPECT.equals(localName)) {
						parseAspect((Element) node, registry);
					}
				}
			}
		}

		/**
		 * Parses the supplied <code>&lt;pointcut&gt;</code> and registers the resulting
		 * {@link org.springframework.aop.Pointcut} with the {@link BeanDefinitionRegistry}.
		 */
		private void parsePointcut(Element pointcutElement, BeanDefinitionRegistry registry) {
			BeanDefinition pointcutDefinition = createPointcutDefinition(pointcutElement.getAttribute(EXPRESSION));
			String id = pointcutElement.getAttribute(ID);

			if (!StringUtils.hasText(id)) {
				id = BeanDefinitionReaderUtils.generateBeanName((AbstractBeanDefinition) pointcutDefinition, registry, false);
			}

			registry.registerBeanDefinition(id, pointcutDefinition);
		}


		private void parseAdvisor(Element element, BeanDefinitionRegistry registry) {
			RootBeanDefinition beanDefinition = new RootBeanDefinition(DefaultPointcutAdvisor.class);

			MutablePropertyValues mpvs = new MutablePropertyValues();
			beanDefinition.setPropertyValues(mpvs);

			mpvs.addPropertyValue(ADVICE, new RuntimeBeanReference(element.getAttribute(ADVICE_REF)));

			if (element.hasAttribute(POINTCUT) && element.hasAttribute(POINTCUT_REF)) {
				throw new IllegalStateException("Cannot define both 'pointcut' and 'pointcut-ref' on 'advisor' tag.");
			}
			else if (element.hasAttribute(POINTCUT)) {
				BeanDefinition pointcutDefinition = createPointcutDefinition(element.getAttribute(POINTCUT));
				mpvs.addPropertyValue(POINTCUT, pointcutDefinition);
			}
			else if (element.hasAttribute(POINTCUT_REF)) {
				mpvs.addPropertyValue(POINTCUT, new RuntimeBeanReference(element.getAttribute(POINTCUT_REF)));
			}
			else {
				throw new IllegalStateException("Must define one of 'pointcut' or 'pointcut-ref' on 'advisor'.");
			}

			String id = element.getAttribute(ID);

			if(!StringUtils.hasText(id)) {
				id = BeanDefinitionReaderUtils.generateBeanName(beanDefinition, registry, false);
			}

			registry.registerBeanDefinition(id, beanDefinition);
		}

		private void parseAspect(Element aspectElement, BeanDefinitionRegistry registry) {
			/*String aspectName = aspectElement.getAttribute(ID);

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
			}*/
		}

		protected BeanDefinition createPointcutDefinition(String expression) {
			RootBeanDefinition beanDefinition = new RootBeanDefinition();
			beanDefinition.setBeanClass(AspectJExpressionPointcut.class);
			beanDefinition.setPropertyValues(new MutablePropertyValues());
			beanDefinition.getPropertyValues().addPropertyValue(EXPRESSION, expression);
			return beanDefinition;
		}

	}
}
