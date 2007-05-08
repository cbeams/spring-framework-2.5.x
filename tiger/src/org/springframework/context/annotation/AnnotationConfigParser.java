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

package org.springframework.context.annotation;

import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.ClassUtils;
import org.w3c.dom.Element;

/**
 * Parser for the &lt;context:annotation-config/&gt; element.
 * 
 * @author Mark Fisher
 * @since 2.1
 */
public class AnnotationConfigParser implements BeanDefinitionParser {

	public BeanDefinition parse(Element element, ParserContext parserContext) {
		registerPostProcessors(parserContext);
		return null;
	}
	
	protected void registerPostProcessors(ParserContext parserContext) {
		BeanDefinition autowiredPostProcessorDefinition = new RootBeanDefinition(AutowiredAnnotationBeanPostProcessor.class);
		parserContext.getReaderContext().registerWithGeneratedName(autowiredPostProcessorDefinition);

		// check for JSR-250 support, and if present add the CommonAnnotationBeanPostProcessor
		if (ClassUtils.isPresent("javax.annotation.Resource")) {
			BeanDefinition commonPostProcessorDefinition = new RootBeanDefinition(CommonAnnotationBeanPostProcessor.class);
			parserContext.getReaderContext().registerWithGeneratedName(commonPostProcessorDefinition);
		}
	}

}
