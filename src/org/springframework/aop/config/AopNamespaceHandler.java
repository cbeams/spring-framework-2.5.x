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

import org.springframework.aop.aspectj.AspectJAfterReturningAdvice;
import org.springframework.aop.aspectj.AspectJAfterThrowingAdvice;
import org.springframework.aop.aspectj.AspectJAroundAdvice;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.aspectj.AspectJMethodBeforeAdvice;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.support.BeanDefinitionParser;
import org.springframework.beans.factory.xml.support.NamespaceHandlerSupport;
import org.springframework.core.PrioritizedParameterNameDiscoverer;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.List;

/**
 * <code>NamespaceHandler</code> for the <code>aop</code> namespace.
 * <p/>
 * Provides a {@link BeanDefinitionParser} for the <code>&lt;aop:config&gt;</code> tag. A
 * <code>config</code> tag can include nested <code>poincut</code>, <code>advisor</code> and
 * <code>aspect</code> tags.
 * <p/>
 * The <code>pointcut</code> tag allows for creation of named {@link AspectJExpressionPointcut} beans
 * using a simple syntax:
 * <pre>
 * &lt;aop:pointcut id=&quot;getNameCalls&quot; expression=&quot;execution(* *..ITestBean.getName(..))&quot;/&gt;
 * </pre>
 * <p/>
 * Using the <code>advisor</code> tag you can configure an {@link org.springframework.aop.Advisor} and have it
 * applied to all relevant beans in you {@link org.springframework.beans.factory.BeanFactory} automatically.
 * The <code>advisor</code> tag supports both in-line and referenced
 * {@link org.springframework.aop.Pointcut Pointcuts}:
 * <pre>
 * &lt;aop:advisor id=&quot;getAgeAdvisor&quot;
 *              pointcut=&quot;execution(* *..ITestBean.getAge(..))&quot;
 *              advice-ref=&quot;getAgeCounter&quot;/&gt;
 * <p/>
 * &lt;aop:advisor id=&quot;getNameAdvisor&quot;
 *              pointcut-ref=&quot;getNameCalls&quot;
 *              advice-ref=&quot;getNameCounter&quot;/&gt;
 * </pre>
 *
 * @author Rob Harrop
 * @see ConfigBeanDefinitionParser
 * @since 2.0
 */
public class AopNamespaceHandler extends NamespaceHandlerSupport {

	/**
	 * Constructs a new <code>AopNamespaceHandler</code> and registers the
	 * {@link BeanDefinitionParser} for the <code>config</code> tag.
	 */
	public AopNamespaceHandler() {
		registerBeanDefinitionParser("config", new ConfigBeanDefinitionParser());
		registerBeanDefinitionParser("spring-configured", new SpringConfiguredBeanDefinitionParser());
		registerBeanDefinitionParser("aspectj-autoproxy", new AspectJAutoProxyBeanDefinitionParser());
	}

	private static class SpringConfiguredBeanDefinitionParser implements BeanDefinitionParser {

		private static final String BEAN_CONFIGURER = "org.springframework.beans.factory.aspectj.BeanConfigurer";

		private boolean registered;

		public void parse(Element element, BeanDefinitionRegistry registry) {
			if (this.registered) {
				return;
			}

			try {
				Class beanConfigurerClass = ClassUtils.forName(BEAN_CONFIGURER);
				RootBeanDefinition definition = new RootBeanDefinition(beanConfigurerClass);
				definition.setFactoryMethodName("aspectOf");

				String id = BeanDefinitionReaderUtils.generateBeanName(definition, registry, false);
				registry.registerBeanDefinition(id, definition);
				this.registered = true;
			}
			catch (ClassNotFoundException e) {
				throw new IllegalStateException("Unable to locate class [" + BEAN_CONFIGURER + "]. Cannot use @SpringConfigured.");
			}
		}
	}

	private static class AspectJAutoProxyBeanDefinitionParser implements BeanDefinitionParser {

		private static final String ASPECTJ_AUTOPROXY_CREATOR = "org.springframework.aop.framework.autoproxy.AspectJAutoProxyCreator";

		private boolean registered;

		// TODO: introduce once per parse BeanDefinitionParser base class
		public void parse(Element element, BeanDefinitionRegistry registry) {
			if (this.registered) {
				return;
			}

			try {
				Class autoProxyClass = ClassUtils.forName(ASPECTJ_AUTOPROXY_CREATOR);
				RootBeanDefinition definition = new RootBeanDefinition(autoProxyClass);

				String id = BeanDefinitionReaderUtils.generateBeanName(definition, registry, false);
				registry.registerBeanDefinition(id, definition);
				this.registered = true;
			}
			catch (ClassNotFoundException e) {
				throw new IllegalStateException("Unable to locate class [" + ASPECTJ_AUTOPROXY_CREATOR + "]. Cannot use @AspectJ auto-proxying.");
			}
		}
	}

	/**
	 * {@link BeanDefinitionParser} for the <code>&lt;aop:config&gt;</code> tag.
	 */
	private static class ConfigBeanDefinitionParser implements BeanDefinitionParser {

		private static final String ASPECT = "aspect";

		private static final String EXPRESSION = "expression";

		private static final String ID = "id";

		private static final String POINTCUT = "pointcut";

		private static final String ADVICE = "advice";

		private static final String ADVISOR = "advisor";

		private static final String ADVICE_REF = "advice-ref";

		private static final String POINTCUT_REF = "pointcut-ref";

		public static final String BEAN = "bean";

		public static final String KIND = "kind";

		public static final String TYPE = "type";

		public static final String BEFORE = "before";

		public static final String AFTER = "after";

		public static final String AFTER_RETURNING = "afterReturning";

		public static final String AFTER_THROWING = "afterThrowing";

		public static final String AROUND = "around";

		public static final int METHOD_INDEX = 0;

		public static final int POINTCUT_INDEX = 1;

		public static final int ASPECT_INSTANCE_FACTORY_INDEX = 2;

		public static final int PARAMETER_NAME_DISCOVERER = 3;

		public void parse(Element element, BeanDefinitionRegistry registry) {
			NodeList childNodes = element.getChildNodes();

			NamespaceHandlerUtils.registerAutoProxyCreatorIfNecessary(registry);

			for (int i = METHOD_INDEX; i < childNodes.getLength(); i++) {
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

		/**
		 * Parses the supplied <code>&lt;advisor&gt;</code> element and registers the resulting
		 * {@link org.springframework.aop.Advisor} and any resultant {@link org.springframework.aop.Pointcut}
		 * with the supplied {@link BeanDefinitionRegistry}.
		 */
		private void parseAdvisor(Element element, BeanDefinitionRegistry registry) {
			RootBeanDefinition beanDefinition = new RootBeanDefinition(DefaultPointcutAdvisor.class);

			MutablePropertyValues mpvs = new MutablePropertyValues();
			beanDefinition.setPropertyValues(mpvs);

			mpvs.addPropertyValue(ADVICE, new RuntimeBeanReference(element.getAttribute(ADVICE_REF)));

			parsePointcutProperty(element, mpvs, registry);

			String id = element.getAttribute(ID);

			if (!StringUtils.hasText(id)) {
				id = BeanDefinitionReaderUtils.generateBeanName(beanDefinition, registry, false);
			}

			registry.registerBeanDefinition(id, beanDefinition);
		}

		private void parseAspect(Element aspectElement, BeanDefinitionRegistry registry) {
			String aspectName = aspectElement.getAttribute(BEAN);

			List pointcuts = DomUtils.getChildElementsByTagName(aspectElement, POINTCUT, true);
			for (int i = 0; i < pointcuts.size(); i++) {
				Element pointcutElement = (Element) pointcuts.get(i);
				parsePointcut(pointcutElement, registry);
			}

			List advice = DomUtils.getChildElementsByTagName(aspectElement, ADVICE, true);
			for (int i = METHOD_INDEX; i < advice.size(); i++) {
				Element adviceElement = (Element) advice.get(i);
				parseAdvice(aspectName, adviceElement, registry);
			}
		}

		private void parseAdvice(String aspectName, Element adviceElement, BeanDefinitionRegistry registry) {

			// create the properties for the advisor
			MutablePropertyValues advisorProperties = new MutablePropertyValues();

			// register the pointcut
			String pointcutBeanName = parsePointcutProperty(adviceElement, advisorProperties, registry);

			// create the method factory bean
			RootBeanDefinition methodDefinition = new RootBeanDefinition(MethodLocatingFactoryBean.class);
			methodDefinition.setPropertyValues(new MutablePropertyValues());
			methodDefinition.getPropertyValues().addPropertyValue("beanName", aspectName);
			methodDefinition.getPropertyValues().addPropertyValue("methodName", adviceElement.getAttribute("method"));

			// create instance factory definition
			RootBeanDefinition instanceFactoryDefinition = new RootBeanDefinition(BeanFactoryAspectInstanceFactory.class);
			instanceFactoryDefinition.setPropertyValues(new MutablePropertyValues());
			instanceFactoryDefinition.getPropertyValues().addPropertyValue("beanName", aspectName);

			// create the advice
			String kind = adviceElement.getAttribute(KIND);
			RootBeanDefinition adviceDefinition = new RootBeanDefinition(getAdviceClass(kind));

			ConstructorArgumentValues cav = new ConstructorArgumentValues();
			cav.addIndexedArgumentValue(METHOD_INDEX, methodDefinition);
			cav.addIndexedArgumentValue(POINTCUT_INDEX, new RuntimeBeanReference(pointcutBeanName));
			cav.addIndexedArgumentValue(ASPECT_INSTANCE_FACTORY_INDEX, instanceFactoryDefinition);

			extendAdviceConstructorArgs(cav, kind);

			adviceDefinition.setConstructorArgumentValues(cav);

			// configure the advisor
			RootBeanDefinition advisorDefinition = new RootBeanDefinition(DefaultPointcutAdvisor.class);
			advisorDefinition.setPropertyValues(advisorProperties);
			advisorDefinition.getPropertyValues().addPropertyValue(ADVICE, adviceDefinition);

			// register the final advisor
			String id = BeanDefinitionReaderUtils.generateBeanName(advisorDefinition, registry, false);
			registry.registerBeanDefinition(id, advisorDefinition);
		}

		private void extendAdviceConstructorArgs(ConstructorArgumentValues cav, String kind) {
			if (AROUND.equals(kind)) {
				RootBeanDefinition discovererDefinition = new RootBeanDefinition(PrioritizedParameterNameDiscoverer.class);
				cav.addIndexedArgumentValue(PARAMETER_NAME_DISCOVERER, discovererDefinition);
			}
		}

		private Class getAdviceClass(String kind) {
			if (BEFORE.equals(kind)) {
				return AspectJMethodBeforeAdvice.class;
			}
			else if (AFTER.equals(kind)) {
				// XXX Rob : should be AspectJAfterAdvice.class
				return AspectJAfterReturningAdvice.class;
			}
			else if (AFTER_RETURNING.equals(kind)) {
				return AspectJAfterReturningAdvice.class;
			}
			else if (AFTER_THROWING.equals(kind)) {
				return AspectJAfterThrowingAdvice.class;
			}
			else if (AROUND.equals(kind)) {
				return AspectJAroundAdvice.class;
			}
			else {
				throw new IllegalArgumentException("Unknown advice kind [" + kind + "].");
			}
		}

		/**
		 * Parses the <code>pointcut</code> or <code>pointcut-ref</code> attributes of the supplied
		 * {@link Element} and add a <code>pointcut</code> property as appropriate. Generates a
		 * {@link org.springframework.beans.factory.config.BeanDefinition} for the pointcut if
		 * necessary and returns its bean name, otherwise returns the bean name of the referred
		 * pointcut.
		 *
		 * @throws IllegalStateException if the {@link Element} includes both <code>pointcut</code> and
		 * <code>pointcut-ref</code> attributes.
		 */
		private String parsePointcutProperty(Element element, MutablePropertyValues mpvs, BeanDefinitionRegistry registry) {
			if (element.hasAttribute(POINTCUT) && element.hasAttribute(POINTCUT_REF)) {
				throw new IllegalStateException("Cannot define both 'pointcut' and 'pointcut-ref' on 'advisor' tag.");
			}
			else if (element.hasAttribute(POINTCUT)) {
				// create a pointcut for the anonymous pc and register it
				BeanDefinition pointcutDefinition = createPointcutDefinition(element.getAttribute(POINTCUT));
				String pointcutName = BeanDefinitionReaderUtils.generateBeanName((AbstractBeanDefinition) pointcutDefinition, registry, false);
				registry.registerBeanDefinition(pointcutName, pointcutDefinition);
				mpvs.addPropertyValue(POINTCUT, new RuntimeBeanReference(pointcutName));
				return pointcutName;
			}
			else if (element.hasAttribute(POINTCUT_REF)) {
				String pointcutRef = element.getAttribute(POINTCUT_REF);
				mpvs.addPropertyValue(POINTCUT, new RuntimeBeanReference(pointcutRef));
				return pointcutRef;
			}
			else {
				throw new IllegalStateException("Must define one of 'pointcut' or 'pointcut-ref' on 'advisor'.");
			}
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
