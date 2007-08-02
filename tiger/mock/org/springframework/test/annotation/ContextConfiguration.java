/*
 * Copyright 2007 the original author or authors.
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
package org.springframework.test.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.context.ApplicationContext;
import org.springframework.test.ContextLoader;
import org.springframework.test.GenericContextLoader;

/**
 * ContextConfiguration defines class-level metadata which can be used to
 * instruct client code how to load and configure an {@link ApplicationContext}.
 * Although the annotated class would generally be an integration or unit test,
 * the use of ContextConfiguration is not limited to testing scenarios.
 *
 * @see ContextLoader
 * @see ApplicationContext
 * @author Sam Brannen
 * @version $Revision: 1.2 $
 * @since 2.2
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface ContextConfiguration {

	// ------------------------------------------------------------------------|
	// --- ATTRIBUTES ---------------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * The locations to use for loading the {@link ApplicationContext}.
	 * Defaults to an empty array.
	 *
	 * @see #generateDefaultLocations()
	 * @see #contextLoaderClass()
	 */
	String[] locations() default {};

	/**
	 * Whether or not <em>default</em> locations should be generated if no
	 * {@link #locations() locations} are explicitly defined. Defaults to
	 * <code>true</code>.
	 *
	 * @see #locations()
	 * @see #contextLoaderClass()
	 * @see #contextResourceSuffix()
	 */
	boolean generateDefaultLocations() default true;

	/**
	 * The suffix to append to {@link ApplicationContext} resource paths when
	 * generating default locations. Defaults to &quot;<code>-context.xml</code>&quot;.
	 *
	 * @see #generateDefaultLocations()
	 */
	String contextResourceSuffix() default "-context.xml";

	/**
	 * The {@link ContextLoader} type to use for loading the
	 * {@link ApplicationContext}. Defaults to {@link GenericContextLoader}.
	 *
	 * @see #locations()
	 * @see #generateDefaultLocations()
	 */
	Class<? extends ContextLoader> contextLoaderClass() default GenericContextLoader.class;

	/**
	 * Are dependencies to be injected via autowiring? Defaults to
	 * {@link Autowire#NO no}.
	 */
	Autowire autowire() default Autowire.NO;

	/**
	 * Is dependency checking to be performed for configured objects? Defaults
	 * to <code>false</code>.
	 */
	boolean dependencyCheck() default false;

}
