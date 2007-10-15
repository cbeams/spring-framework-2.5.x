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

package org.springframework.context.weaving;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.aspectj.weaver.loadtime.ClassPreProcessorAgentAdapter;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.instrument.InstrumentationSavingAgent;
import org.springframework.instrument.classloading.InstrumentationLoadTimeWeaver;
import org.springframework.instrument.classloading.LoadTimeWeaver;

/**
 * Post-processor that registers AspectJ's
 * {@link org.aspectj.weaver.loadtime.ClassPreProcessorAgentAdapter}
 * with the Spring application context's default
 * {@link org.springframework.instrument.classloading.LoadTimeWeaver}.
 *
 * @author Juergen Hoeller
 * @author Ramnivas Laddad
 * @since 2.5
 */
public class AspectJWeavingEnabler implements BeanFactoryPostProcessor, LoadTimeWeaverAware, Ordered {

	private LoadTimeWeaver loadTimeWeaver;


	public void setLoadTimeWeaver(LoadTimeWeaver loadTimeWeaver) {
		this.loadTimeWeaver = loadTimeWeaver;
	}

	public int getOrder() {
		return HIGHEST_PRECEDENCE;
	}


	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		LoadTimeWeaver weaverToUse = this.loadTimeWeaver;
		if (weaverToUse == null && InstrumentationSavingAgent.getInstrumentation() != null) {
			weaverToUse = new InstrumentationLoadTimeWeaver();
		}
		weaverToUse.addTransformer(new AspectJClassBypassingClassFileTransformerDecorator(
					new ClassPreProcessorAgentAdapter()));
	}

	/*
	 * Potentially temporary way to avoid processing AspectJ classes and avoiding the LinkageError
	 * while doing so. OC4J and Tomcat (in Glassfish) definitely need bypasing such classes.
	 * TODO: Investigate further to see why AspectJ itself isn't doing so.
	 */
	private static class AspectJClassBypassingClassFileTransformerDecorator implements ClassFileTransformer {
		private ClassFileTransformer delegate;
		
		public AspectJClassBypassingClassFileTransformerDecorator(ClassFileTransformer delegate) {
			this.delegate = delegate;
		}
		
		@Override
		public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
				ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
			if(className.startsWith("org.aspectj") || className.startsWith("org/aspectj")) {
				return classfileBuffer;
			}
			return delegate.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
		}
	}
}
