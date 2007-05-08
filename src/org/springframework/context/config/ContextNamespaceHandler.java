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

package org.springframework.context.config;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.context.annotation.AnnotationConfigParser;
import org.springframework.context.annotation.AnnotationScanParser;
import org.springframework.core.JdkVersion;

/**
 * Handler for the context namespace.
 * 
 * @author Mark Fisher
 * @since 2.1
 */
public class ContextNamespaceHandler extends NamespaceHandlerSupport {

	private static final String ANNOTATION_CONFIG_ELEMENT = "annotation-config";
	
	private static final String ANNOTATION_SCAN_ELEMENT = "annotation-scan";

	
	public void init() {
		if (JdkVersion.isAtLeastJava15()) {
			this.registerBeanDefinitionParser(ANNOTATION_CONFIG_ELEMENT, new AnnotationConfigParser());
			this.registerBeanDefinitionParser(ANNOTATION_SCAN_ELEMENT, new AnnotationScanParser());
		}
	}

}
