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

package org.springframework.ui.freemarker;

import java.io.IOException;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;

/**
 * Factory bean that creates a FreeMarker Configuration and provides it as
 * bean reference. This bean is intended for any kind of usage of FreeMarker
 * in application code, e.g. for generating email content. For web views,
 * FreeMarkerConfigurer is used to set up a FreeMarkerConfigurationFactory.
 *
 * <p>See base class FreeMarkerConfigurationFactory for details.
 *
 * <p>Note: Spring's FreeMarker support requires FreeMarker 2.3 or higher.
 *
 * @author Darren Davison
 * @since 3/3/2004
 */
public class FreeMarkerConfigurationFactoryBean extends FreeMarkerConfigurationFactory
		implements FactoryBean, InitializingBean, ResourceLoaderAware {

	private Configuration configuration;

	public void afterPropertiesSet() throws IOException, TemplateException {
		this.configuration = createConfiguration();
	}

	public Object getObject() {
		return this.configuration;
	}

	public Class getObjectType() {
		return Configuration.class;
	}

	public boolean isSingleton() {
		return true;
	}

}
