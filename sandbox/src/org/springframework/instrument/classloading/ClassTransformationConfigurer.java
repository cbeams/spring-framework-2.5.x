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

package org.springframework.instrument.classloading;

import java.lang.instrument.ClassFileTransformer;

import org.aspectj.weaver.loadtime.ClassPreProcessorAgentAdapter;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Generic configurer for bean-style linking in of
 * {@link java.lang.instrument.ClassFileTransformer} definitions.
 *
 * <p>Can for example be used to register a custom transformer
 * on an InstrumentationLoadTimeWeaver:
 *
 * <pre>
 * &lt;bean id="loadTimeWeaver" class="org.springframework.instrument.classloading.InstrumentationLoadTimeWeaver"/&gt;
 *
 * &lt;bean class="org.springframework.instrument.classloading.ClassTransformationConfigurer"&gt;
 *   &lt;property name="loadTimeWeaver" ref="loadTimeWeaver"/&gt;
 *   &lt;property name="transformers"&gt;
 *     &lt;bean class="mypackage.MyClassTransformer"/&gt;
 *   &lt;/property&gt;
 * &lt;/bean&gt;</pre>
 *
 * A further common usage scenario is activating AspectJ load-time
 * weaving through the built-in "registerAspectjPreProcessor" flag:
 *
 * <pre>
 * &lt;bean class="org.springframework.instrument.classloading.ClassTransformationConfigurer"&gt;
 *   &lt;property name="loadTimeWeaver" ref="loadTimeWeaver"/&gt;
 *   &lt;property name="registerAspectjPreProcessor" value="true"/&gt;
 * &lt;/bean&gt;</pre>
 *
 * Of course, a LoadTimeWeaver definition like the above can be reused
 * for JPA weaving, for example, linked into a
 * {@link org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean}
 * definition:
 *
 * <pre>
 * &lt;bean class="org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean"&gt;
 *   &lt;property name="dataSource" ref="dataSource"/&gt;
 *   &lt;property name="loadTimeWeaver" ref="loadTimeWeaver"/&gt;
 * &lt;/bean&gt;</pre>
 *
 * @author Juergen Hoeller
 * @since 2.1
 * @see #setLoadTimeWeaver
 * @see #setTransformers
 * @see #setRegisterAspectjPreProcessor
 */
public class ClassTransformationConfigurer implements BeanFactoryPostProcessor {

	private LoadTimeWeaver loadTimeWeaver;

	private boolean registerAspectjPreProcessor = false;

	private ClassFileTransformer[] transformers;


	/**
	 * Set the LoadTimeWeaver to register the transformers on.
	 * @see InstrumentationLoadTimeWeaver
	 * @see ReflectiveLoadTimeWeaver
	 * @see org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean#setLoadTimeWeaver
	 */
	public void setLoadTimeWeaver(LoadTimeWeaver loadTimeWeaver) {
		this.loadTimeWeaver = loadTimeWeaver;
	}

	/**
	 * Specify whether to register the AspectJ
	 * {@link org.aspectj.weaver.loadtime.ClassPreProcessorAgentAdapter}
	 * with the given LoadTimeWeaver.
	 * <p>Default is "false". Set this flag to "true" to activate AspectJ
	 * load-time weaving based on AspectJ's "META-INF/aop.xml" descriptor,
	 * weaving application classes with AspectJ aspects.
	 * <p>This is a convenience shortcut for specifying a ClassPreProcessorAgentAdapter
	 * instance via the generic "transformers" property. The pre-processor created
	 * through this flag will be registered <i>before</i> any ClassFileTransformers
	 * specified on the "transformers" property.
	 * @see org.aspectj.weaver.loadtime.ClassPreProcessorAgentAdapter
	 * @see #setTransformers
	 */
	public void setRegisterAspectjPreProcessor(boolean registerAspectjPreProcessor) {
		this.registerAspectjPreProcessor = registerAspectjPreProcessor;
	}

	/**
	 * Specify custom ClassFileTransformers to register on the LoadTimeWeaver
	 * that this configurer operates on.
	 * @see #setLoadTimeWeaver
	 */
	public void setTransformers(ClassFileTransformer[] transformers) {
		this.transformers = transformers;
	}


	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		if (this.loadTimeWeaver == null) {
			throw new IllegalArgumentException("Property 'loadTimeWeaver' is required");
		}

		if (this.registerAspectjPreProcessor) {
			this.loadTimeWeaver.addTransformer(AspectJPreProcessorFactory.createAspectJPreProcessor());
		}

		if (this.transformers != null) {
			for (int i = 0; i < this.transformers.length; i++) {
				this.loadTimeWeaver.addTransformer(this.transformers[i]);
			}
		}
	}


	/**
	 * Inner factory class used to just introduce an AspectJ dependency
	 * when actually registering the AspectJ pre-processor.
	 */
	private static class AspectJPreProcessorFactory {

		public static ClassFileTransformer createAspectJPreProcessor() {
			return new ClassPreProcessorAgentAdapter();
		}
	}

}
