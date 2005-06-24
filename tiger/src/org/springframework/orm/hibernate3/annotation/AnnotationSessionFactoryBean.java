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

package org.springframework.orm.hibernate3.annotation;

import org.hibernate.HibernateException;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;

import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

/**
 * Subclass of Spring's standard LocalSessionFactoryBean for Hibernate3,
 * supporting JDK 1.5+ annotation metadata for mappings.
 * Requires the Hibernate3 Annotation add-on to be present.
 *
 * <p>Example bean definition:
 *
 * <pre>
 * &lt;bean id="sessionFactory" class="org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean"&gt;
 *   &lt;property name="dataSource"&gt;
 *     &lt;ref bean="dataSource"/&gt;
 *   &lt;/property&gt;
 *   &lt;property name="annotatedClasses"&gt;
 *     &lt;list&gt;
 *       &lt;value&gt;test.package.Foo&lt;/value&gt;
 *       &lt;value&gt;test.package.Bar&lt;/value&gt;
 *     &lt;/list&gt;
 *   &lt;/property&gt;
 *   &lt;property name="annotatedPackages"&gt;
 *     &lt;list&gt;
 *       &lt;value&gt;test.package&lt;/value&gt;
 *     &lt;/list&gt;
 *   &lt;/property&gt;
 * &lt;/bean&gt;</pre>
 *
 * @author Juergen Hoeller
 * @since 1.2.2
 * @see #setDataSource
 * @see #setHibernateProperties
 * @see #setAnnotatedClasses
 * @see #setAnnotatedPackages
 */
public class AnnotationSessionFactoryBean extends LocalSessionFactoryBean {

	private Class[] annotatedClasses;

	private String[] annotatedPackages;


	public AnnotationSessionFactoryBean() {
		setConfigurationClass(AnnotationConfiguration.class);
	}

	public void setConfigurationClass(Class configurationClass) {
		if (configurationClass == null || !AnnotationConfiguration.class.isAssignableFrom(configurationClass)) {
			throw new IllegalArgumentException(
					"AnnotationSessionFactoryBean only supports AnnotationConfiguration or subclasses");
		}
		super.setConfigurationClass(configurationClass);
	}

	/**
	 * Specify annotated classes, for which mappings will be read from
	 * class-level JDK 1.5+ annotation metadata.
	 * @see org.hibernate.cfg.AnnotationConfiguration#addAnnotatedClass(Class)
	 */
	public void setAnnotatedClasses(Class[] annotatedClasses) {
		this.annotatedClasses = annotatedClasses;
	}

	/**
	 * Specify the names of annotated packages, for which package-level
	 * JDK 1.5+ annotation metadata will be read.
	 * @see org.hibernate.cfg.AnnotationConfiguration#addPackage(String)
	 */
	public void setAnnotatedPackages(String[] annotatedPackages) {
		this.annotatedPackages = annotatedPackages;
	}


	/**
	 * Reads metadata from annotated classes and packages into the
	 * AnnotationConfiguration instance.
	 * <p>Calls <code>postProcessAnnotationConfiguration</code> afterwards,
	 * to give subclasses the chance to perform custom post-processing.
	 * @see #postProcessAnnotationConfiguration
	 */
	protected final void postProcessConfiguration(Configuration config) throws HibernateException {
		AnnotationConfiguration annConfig = (AnnotationConfiguration) config;

		if (this.annotatedClasses != null) {
			for (int i = 0; i < this.annotatedClasses.length; i++) {
				annConfig.addAnnotatedClass(this.annotatedClasses[i]);
			}
		}
		if (this.annotatedPackages != null) {
			for (int i = 0; i < this.annotatedPackages.length; i++) {
				annConfig.addPackage(this.annotatedPackages[i]);
			}
		}

		// Perform custom post-processing in subclasses.
		postProcessAnnotationConfiguration(annConfig);
	}

	/**
	 * To be implemented by subclasses that want to to perform custom
	 * post-processing of the AnnotationConfiguration object after this
	 * FactoryBean performed its default initialization.
	 * @param config the current AnnotationConfiguration object
	 * @throws HibernateException in case of Hibernate initialization errors
	 */
	protected void postProcessAnnotationConfiguration(AnnotationConfiguration config)
			throws HibernateException {
	}

}
