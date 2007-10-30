/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.transaction.config;

import org.w3c.dom.Element;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.transaction.jta.OC4JJtaTransactionManager;
import org.springframework.transaction.jta.WebLogicJtaTransactionManager;
import org.springframework.transaction.jta.WebSphereUowTransactionManager;
import org.springframework.util.ClassUtils;

/**
 * Parser for the &lt;tx:jta-transaction-manager/&gt; element,
 * autodetecting BEA WebLogic, IBM WebSphere and Oracle OC4J.
 *
 * @author Juergen Hoeller
 * @since 2.5
 */
public class JtaTransactionManagerBeanDefinitionParser extends AbstractSingleBeanDefinitionParser  {

	public static final String DEFAULT_TRANSACTION_MANAGER_BEAN_NAME =
			AnnotationDrivenBeanDefinitionParser.DEFAULT_TRANSACTION_MANAGER_BEAN_NAME;

	private static final boolean weblogicPresent = ClassUtils.isPresent("weblogic.transaction.UserTransaction");

	private static final boolean webspherePresent = ClassUtils.isPresent("com.ibm.wsspi.uow.UOWManager");

	private static final boolean oc4jPresent = ClassUtils.isPresent("oracle.j2ee.transaction.OC4JTransactionManager");


	protected Class getBeanClass(Element element) {
		if (weblogicPresent) {
			return WebLogicJtaTransactionManager.class;
		}
		else if (webspherePresent) {
			return WebSphereUowTransactionManager.class;
		}
		else if (oc4jPresent) {
			return OC4JJtaTransactionManager.class;
		}
		else {
			return JtaTransactionManager.class;
		}
	}

	protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext) {
		return DEFAULT_TRANSACTION_MANAGER_BEAN_NAME;
	}

}
