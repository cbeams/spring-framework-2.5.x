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
package org.springframework.test;

import static org.springframework.core.annotation.AnnotationUtils.findAnnotationDeclaringClass;
import static org.springframework.core.annotation.AnnotationUtils.isAnnotationDeclaredLocally;
import static org.springframework.core.annotation.AnnotationUtils.isAnnotationInherited;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.test.annotation.ContextConfiguration;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

/**
 * Default implementation of the {@link ContextConfigurationAttributes}
 * interface, which also provides a static factory method for
 * {@link #constructAttributes(Class) constructing configuration attributes} for
 * a specified class.
 *
 * @see #constructAttributes(Class)
 * @author Sam Brannen
 * @version $Revision: 1.1 $
 * @since 2.2
 */
public class DefaultContextConfigurationAttributes implements ContextConfigurationAttributes {

	// ------------------------------------------------------------------------|
	// --- CONSTANTS ----------------------------------------------------------|
	// ------------------------------------------------------------------------|

	/** Class Logger. */
	private static final Log LOG = LogFactory.getLog(DefaultContextConfigurationAttributes.class);

	// ------------------------------------------------------------------------|
	// --- CLASS VARIABLES ----------------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- INSTANCE VARIABLES -------------------------------------------------|
	// ------------------------------------------------------------------------|

	private final Autowire autowireMode;

	private final String contextResourceSuffix;

	private final Class<? extends ContextLoader> contextLoaderClass;

	private final boolean dependencyCheckEnabled;

	private final boolean generateDefaultLocations;

	private final String[] locations;

	// ------------------------------------------------------------------------|
	// --- INSTANCE INITIALIZATION --------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- CONSTRUCTORS -------------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * Constructs a new {@link DefaultContextConfigurationAttributes} instance
	 * from the supplied arguments.
	 *
	 * @param contextLoaderClass
	 * @param locations
	 * @param clazz
	 * @param generateDefaultLocations
	 * @param contextResourceSuffix
	 * @param autowireMode
	 * @param dependencyCheckEnabled
	 * @throws IllegalArgumentException if the supplied
	 *         <code>contextLoaderClass</code>,
	 *         <code>contextResourceSuffix</code>, or
	 *         <code>autowireMode</code> is <code>null</code>.
	 */
	public DefaultContextConfigurationAttributes(final Class<? extends ContextLoader> contextLoaderClass,
			final String[] locations, final Class<?> clazz, final boolean generateDefaultLocations,
			final String contextResourceSuffix, final Autowire autowireMode, final boolean dependencyCheckEnabled) {

		Assert.notNull(contextLoaderClass, "contextLoaderClass can not be null.");
		Assert.notNull(contextResourceSuffix, "contextResourceSuffix can not be null.");
		Assert.notNull(autowireMode, "autowireMode can not be null.");

		this.contextLoaderClass = contextLoaderClass;
		this.contextResourceSuffix = contextResourceSuffix;
		this.autowireMode = autowireMode;
		this.dependencyCheckEnabled = dependencyCheckEnabled;
		this.generateDefaultLocations = generateDefaultLocations;

		// Note: generateDefaultLocations(Class) requires that the
		// contextResourceSuffix already be set. Thus, the locations must be set
		// after contextResourceSuffix.
		this.locations = (ArrayUtils.isEmpty(locations) && generateDefaultLocations) ? generateDefaultLocations(clazz)
				: locations;
	}

	// ------------------------------------------------------------------------|
	// --- CLASS METHODS ------------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * Constructs a new {@link DefaultContextConfigurationAttributes} instance
	 * based on the supplied <code>clazz</code> which must declare or inherit
	 * a {@link ContextConfiguration} annotation.
	 *
	 * @param clazz The class for which the
	 *        {@link ContextConfigurationAttributes} should be constructed.
	 * @return a new ContextConfigurationAttributes instance.
	 * @throws IllegalArgumentException if the supplied class is
	 *         <code>null</code> or if a {@link ContextConfiguration}
	 *         annotation is not present for the supplied class.
	 */
	public static ContextConfigurationAttributes constructAttributes(final Class<?> clazz) {

		Assert.notNull(clazz, "clazz can not be null.");

		final Class<ContextConfiguration> annotationType = ContextConfiguration.class;
		final Class<?> declaringClass = findAnnotationDeclaringClass(annotationType, clazz);
		final ContextConfiguration contextConfiguration = clazz.getAnnotation(annotationType);

		Assert.isTrue(contextConfiguration != null,
				"A ContextConfiguration annotation must be present for the supplied class [" + clazz + "].");
		Assert.state(declaringClass != null, "Could not find an 'annotation declaring class' for annotation type ["
				+ annotationType + "] and class [" + clazz + "].");

		// XXX Remove debug code once AnnotationUtils are properly unit tested.
		if (LOG.isDebugEnabled()) {
			final boolean declaredLocally = isAnnotationDeclaredLocally(annotationType, clazz);
			final boolean inherited = isAnnotationInherited(annotationType, clazz);
			final String nl = System.getProperty("line.separator");
			final StringBuilder builder = new StringBuilder("ContextConfiguration:").append(nl);
			builder.append("\tTest class                     [").append(clazz).append("].").append(nl);
			builder.append("\tAnnotation declaring class     [").append(declaringClass).append("].").append(nl);
			builder.append("\tAnnotation is declared locally [").append(declaredLocally).append("].").append(nl);
			builder.append("\tAnnotation is inherited        [").append(inherited).append("].").append(nl);
			LOG.debug(builder);
		}

		return new DefaultContextConfigurationAttributes(contextConfiguration.contextLoaderClass(),
				contextConfiguration.locations(), declaringClass, contextConfiguration.generateDefaultLocations(),
				contextConfiguration.contextResourceSuffix(), contextConfiguration.autowire(),
				contextConfiguration.dependencyCheck());
	}

	// ------------------------------------------------------------------------|
	// --- INSTANCE METHODS ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * @see java.lang.Object#equals(Object)
	 */
	@Override
	public boolean equals(final Object object) {

		if (!(object instanceof DefaultContextConfigurationAttributes)) {
			return false;
		}
		final DefaultContextConfigurationAttributes that = (DefaultContextConfigurationAttributes) object;

		// .append(this.contextResourceSuffix,that.contextResourceSuffix)
		// .append(this.generateDefaultLocations,that.generateDefaultLocations)

		return new EqualsBuilder()

		.append(this.locations, that.locations)

		.append(this.autowireMode, that.autowireMode)

		.append(this.dependencyCheckEnabled, that.dependencyCheckEnabled)

		.isEquals();
	}

	// ------------------------------------------------------------------------|

	/**
	 * Generates the default locations array based on the supplied class. For
	 * example, if the supplied class is <code>com.example.MyTest</code>, the
	 * generated locations will contain a single string with a value of
	 * &quot;com/example/MyTest<code>&lt;suffix&gt;</code>&quot;, where
	 * <code>&lt;suffix&gt;</code> is the value returned by
	 * {@link #getContextResourceSuffix()}.
	 *
	 * @param clazz
	 * @return
	 */
	protected String[] generateDefaultLocations(final Class<?> clazz) {

		Assert.notNull(clazz, "clazz can not be null.");
		Assert.state(
				getContextResourceSuffix() != null,
				"getContextResourceSuffix() returned null: the contextResourceSuffix must be initialized prior to calling generateDefaultLocations(Class<?>).");

		return new String[] { ClassUtils.convertClassNameToResourcePath(clazz.getName()) + getContextResourceSuffix() };
	}

	// ------------------------------------------------------------------------|

	/**
	 * @see org.springframework.test.ContextConfigurationAttributes#getAutowireMode()
	 */
	public Autowire getAutowireMode() {

		return this.autowireMode;
	}

	// ------------------------------------------------------------------------|

	/**
	 * Gets the suffix to append to context configuration files.
	 *
	 * @return Returns the contextResourceSuffix.
	 */
	public final String getContextResourceSuffix() {

		return this.contextResourceSuffix;
	}

	// ------------------------------------------------------------------------|

	/**
	 * @see org.springframework.test.ContextConfigurationAttributes#getContextLoaderClass()
	 */
	public Class<? extends ContextLoader> getContextLoaderClass() {

		return this.contextLoaderClass;
	}

	// ------------------------------------------------------------------------|

	/**
	 * @see org.springframework.test.ContextConfigurationAttributes#getLocations()
	 */
	@Override
	public String[] getLocations() {

		return this.locations;
	}

	// ------------------------------------------------------------------------|

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {

		// .append(this.contextResourceSuffix)
		// .append(this.generateDefaultLocations)

		return new HashCodeBuilder(267177669, -523529461)

		.append(this.locations)

		.append(this.autowireMode)

		.append(this.dependencyCheckEnabled)

		.toHashCode();
	}

	// ------------------------------------------------------------------------|

	/**
	 * @see org.springframework.test.ContextConfigurationAttributes#isDependencyCheckEnabled()
	 */
	public boolean isDependencyCheckEnabled() {

		return this.dependencyCheckEnabled;
	}

	// ------------------------------------------------------------------------|

	/**
	 * @see org.springframework.test.ContextConfigurationAttributes#isGenerateDefaultLocations()
	 */
	public boolean isGenerateDefaultLocations() {

		return this.generateDefaultLocations;
	}

	// ------------------------------------------------------------------------|

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		return new ToStringBuilder(this)

		.append("locations", ObjectUtils.nullSafeToString(this.locations))

		.append("contextLoaderClass", this.contextLoaderClass)

		.append("generateDefaultLocations", this.generateDefaultLocations)

		.append("contextResourceSuffix", this.contextResourceSuffix)

		.append("autowireMode", this.autowireMode)

		.append("dependencyCheckEnabled", this.dependencyCheckEnabled)

		.toString();
	}

	// ------------------------------------------------------------------------|

}
