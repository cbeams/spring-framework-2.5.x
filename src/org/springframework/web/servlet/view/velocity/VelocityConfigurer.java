/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.web.servlet.view.velocity;

import java.io.IOException;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.VelocityException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.ui.velocity.SpringResourceLoader;
import org.springframework.ui.velocity.VelocityEngineFactory;

/**
 * JavaBean to configure Velocity for web usage, via the "configLocation"
 * and/or "velocityProperties" and/or "resourceLoaderPath" bean properties.
 * The simplest way to use this class is to specify just a "resourceLoaderPath":
 * You do not need any further configuration then.
 *
 * <pre>
 * &lt;bean id="velocityConfig" class="org.springframework.web.servlet.view.velocity.VelocityConfigurer">
 *   &lt;property name="resourceLoaderPath">&lt;value>/WEB-INF/velocity/&lt;/value>&lt;/property>
 * &lt;/bean></pre>
 *
 * This bean must be included in the application context of any application
 * using Spring's VelocityView for web MVC. It exists purely to configure Velocity.
 * It is not meant to be referenced by application components but just internally
 * by VelocityView. Implements VelocityConfig to be found by VelocityView without
 * depending on the bean name the configurer. Each DispatcherServlet can define its
 * own VelocityConfigurer if desired.  The Spring VM (web and form macro library)
 * will be added to the configured VelocityEngine by this class.
 *
 * <p>Note that you can also refer to a preconfigured VelocityEngine instance, for
 * example one set up by VelocityEngineFactoryBean, via the "velocityEngine" property.
 * This allows to shared a VelocityEngine for web and email usage, for example.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Darren Davison
 * @version $Id: VelocityConfigurer.java,v 1.15 2004-07-02 00:40:04 davison Exp $
 * @see #setConfigLocation
 * @see #setVelocityProperties
 * @see #setResourceLoaderPath
 * @see #setVelocityEngine
 * @see org.springframework.ui.velocity.VelocityEngineFactoryBean
 * @see VelocityView
 */
public class VelocityConfigurer extends VelocityEngineFactory
		implements VelocityConfig, InitializingBean, ResourceLoaderAware {

    /** the resource location path for form macro libraries */
    public static final String SPRING_FORM_MACRO_PATH = "classpath:org/springframework/web/servlet/view/velocity/";

    /** the name of the default form macro library for Spring's Velocity web support */
    public static final String SPRING_FORM_MACRO_LIBRARY = "spring.vm";
    
	private VelocityEngine velocityEngine;
    
    /**
     * Provides a SpringResourceLoader in addition to any default or user defined loader
     * in order to load the spring Velocity macros from the classpath.
     * 
     * @see org.springframework.ui.velocity.VelocityEngineFactory#postProcessVelocityEngine
     * @see org.springframework.ui.velocity.SpringResourceLoader
     */
    protected void postProcessVelocityEngine(VelocityEngine velocityEngine)
            throws IOException, VelocityException {
        
        velocityEngine.addProperty(VelocityEngine.RESOURCE_LOADER, 
                SpringResourceLoader.NAME);
		velocityEngine.setProperty(SpringResourceLoader.SPRING_RESOURCE_LOADER_CLASS, 
		        SpringResourceLoader.class.getName());
		velocityEngine.setApplicationAttribute(SpringResourceLoader.SPRING_RESOURCE_LOADER, 
		        new DefaultResourceLoader());
		velocityEngine.setApplicationAttribute(SpringResourceLoader.SPRING_RESOURCE_LOADER_PATH, 
		        SPRING_FORM_MACRO_PATH);
		velocityEngine.addProperty(VelocityEngine.VM_LIBRARY, 
		        SPRING_FORM_MACRO_LIBRARY);		
		logger.info("SpringResourceLoader added to configured VelocityEngine");
    }
    
	/**
	 * Set a preconfigured VelocityEngine to use for the Velocity web config, e.g.
	 * a shared one for web and email usage, set up via VelocityEngineFactoryBean.
	 * If this is not set, VelocityEngineFactory's properties (inherited by this
	 * class) have to be specified.
	 * @see org.springframework.ui.velocity.VelocityEngineFactoryBean
	 */
	public void setVelocityEngine(VelocityEngine velocityEngine) {
		this.velocityEngine = velocityEngine;
	}

	/**
	 * Initialize VelocityEngineFactory's VelocityEngine
	 * if not overridden by a preconfigured VelocityEngine.
	 * @see #createVelocityEngine
	 * @see #setVelocityEngine
	 */
	public void afterPropertiesSet() throws IOException, VelocityException {
		if (this.velocityEngine == null) {
			this.velocityEngine = createVelocityEngine();
		}
	}

	public VelocityEngine getVelocityEngine() {
		return this.velocityEngine;
	}

}
