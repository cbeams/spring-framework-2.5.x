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
package org.springframework.test.context;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.ApplicationContext;
import org.springframework.test.context.support.GenericXmlContextLoader;

/**
 * ContextConfiguration defines class-level metadata which can be used to
 * instruct client code with regard to how to load and configure an
 * {@link ApplicationContext}. Although the annotated class will generally be
 * an integration or unit test, the use of ContextConfiguration is not
 * necessarily limited to testing scenarios.
 *
 * @see ContextLoader
 * @see ApplicationContext
 * @author Sam Brannen
 * @version $Revision: 1.1 $
 * @since 2.1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Documented
public @interface ContextConfiguration {

	// ------------------------------------------------------------------------|
	// --- ATTRIBUTES ---------------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * The resource locations to use for loading the {@link ApplicationContext}.
	 * </p>
	 * <p>
	 * Defaults to an empty array.
	 * </p>
	 *
	 * @see #generateDefaultLocations()
	 * @see #loaderClass()
	 */
	String[] locations() default {};

	/**
	 * <p>
	 * Whether or not <em>default</em> resource locations should be generated
	 * if no {@link #locations() locations} are explicitly defined.
	 * </p>
	 * <p>
	 * Defaults to <code>true</code>.
	 * </p>
	 *
	 * @see #locations()
	 * @see #loaderClass()
	 * @see #resourceSuffix()
	 */
	boolean generateDefaultLocations() default true;

	/**
	 * <p>
	 * The suffix to append to {@link ApplicationContext} resource locations
	 * when generating default locations.
	 * </p>
	 * <p>
	 * Defaults to &quot;<code>-context.xml</code>&quot;.
	 * </p>
	 *
	 * @see #generateDefaultLocations()
	 */
	String resourceSuffix() default "-context.xml";

	/**
	 * <p>
	 * The {@link ContextLoader} type to use for loading the
	 * {@link ApplicationContext}.
	 * </p>
	 * <p>
	 * Defaults to {@link GenericXmlContextLoader}.
	 * </p>
	 *
	 * @see #locations()
	 * @see #generateDefaultLocations()
	 */
	Class<? extends ContextLoader> loaderClass() default GenericXmlContextLoader.class;

}
