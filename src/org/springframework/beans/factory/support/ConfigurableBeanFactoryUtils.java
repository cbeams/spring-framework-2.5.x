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

package org.springframework.beans.factory.support;

import java.io.InputStream;
import java.net.URL;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.propertyeditors.InputStreamEditor;
import org.springframework.beans.propertyeditors.URLEditor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceEditor;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourceArrayPropertyEditor;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * Helper methods to populate a ConfigurableBeanFactory with resource editors.
 * Used by AbstractApplicationContext and XmlViewResolver.
 *
 * @author Juergen Hoeller
 * @since 1.2
 * @see org.springframework.context.support.AbstractApplicationContext#refresh
 * @see org.springframework.web.servlet.view.XmlViewResolver#initFactory
 */
public abstract class ConfigurableBeanFactoryUtils {

	/**
	 * Populate the given bean factory with the following resource editors:
	 * ResourceEditor, URLEditor, InputStreamEditor.
	 * @param beanFactory the bean factory to populate
	 * @param resourceLoader the ResourceLoader to create editors for
	 * (usually an ApplicationContext)
	 * @see org.springframework.core.io.ResourceEditor
	 * @see org.springframework.beans.propertyeditors.URLEditor
	 * @see org.springframework.beans.propertyeditors.InputStreamEditor
	 * @see org.springframework.context.ApplicationContext
	 */
	public static void registerResourceEditors(
			ConfigurableBeanFactory beanFactory, ResourceLoader resourceLoader) {

		ResourceEditor baseEditor = new ResourceEditor(resourceLoader);
		beanFactory.registerCustomEditor(Resource.class, baseEditor);
		beanFactory.registerCustomEditor(URL.class, new URLEditor(baseEditor));
		beanFactory.registerCustomEditor(InputStream.class, new InputStreamEditor(baseEditor));
	}

	/**
	 * Populate the given bean factory with the following resource editors:
	 * ResourceEditor, URLEditor, InputStreamEditor, ResourceArrayPropertyEditor.
	 * @param beanFactory the bean factory to populate
	 * @param resourcePatternResolver the ResourcePatternResolver to create
	 * editors for (usually an ApplicationContext)
	 * @see org.springframework.core.io.ResourceEditor
	 * @see org.springframework.beans.propertyeditors.URLEditor
	 * @see org.springframework.beans.propertyeditors.InputStreamEditor
	 * @see org.springframework.core.io.support.ResourceArrayPropertyEditor
	 * @see org.springframework.context.ApplicationContext
	 */
	public static void registerResourceEditors(
			ConfigurableBeanFactory beanFactory, ResourcePatternResolver resourcePatternResolver) {

		registerResourceEditors(beanFactory, (ResourceLoader) resourcePatternResolver);
		beanFactory.registerCustomEditor(Resource[].class,
				new ResourceArrayPropertyEditor(resourcePatternResolver));
	}

}
