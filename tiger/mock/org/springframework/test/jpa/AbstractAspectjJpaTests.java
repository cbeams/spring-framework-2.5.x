/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.test.jpa;

import org.aspectj.weaver.loadtime.ClassPreProcessorAgentAdapter;
import org.springframework.instrument.classloading.ResourceOverridingShadowingClassLoader;

/**
 * Subclass of AbstractJpaTests that activates AspectJ
 * load time weaving and allows the ability to specify
 * a custom location for AspectJ's aop.xml file.
 * @author Rod Johnson
 * @since 2.0
 */
public abstract class AbstractAspectjJpaTests extends AbstractJpaTests {
	
	private static final String DEFAULT_AOP_XML_LOCATION = "META-INF/aop.xml";

	@Override
	protected void customizeResourceOverridingShadowingClassLoader(ClassLoader shadowingClassLoader) {
		ResourceOverridingShadowingClassLoader orxl = (ResourceOverridingShadowingClassLoader) shadowingClassLoader;
		orxl.override(DEFAULT_AOP_XML_LOCATION, getActualAopXmlLocation());
		orxl.addTransformer(new ClassPreProcessorAgentAdapter());
	}
	
	protected String getActualAopXmlLocation() {
		return DEFAULT_AOP_XML_LOCATION;
	}


}
