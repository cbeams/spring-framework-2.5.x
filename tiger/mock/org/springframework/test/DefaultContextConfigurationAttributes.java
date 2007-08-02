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
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

/**
 * Default implementation of the {@link ContextConfigurationAttributes}
 * interface, which also provides a static factory method for
 * {@link #constructAttributes(Class) constructing configuration attributes} for
 * a specified class.
 *
 * @see #constructAttributes(Class)
 * @author Sam Brannen
 * @version $Revision: 1.2 $
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

	// XXX Do we really want/need contextResourceSuffix in config attributes?
	private final String contextResourceSuffix;

	// XXX Do we really want/need contextLoaderClass in config attributes?
	private final Class<? extends ContextLoader> contextLoaderClass;

	private final boolean dependencyCheckEnabled;

	// XXX Do we really want/need generateDefaultLocations in config attributes?
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
	 * @param clazz Class object representing the class with which
	 *        <em>locations</em> are associated
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
		this.locations = generateLocations(locations, clazz, generateDefaultLocations, contextResourceSuffix);
		this.generateDefaultLocations = generateDefaultLocations;
		this.contextResourceSuffix = contextResourceSuffix;
		this.autowireMode = autowireMode;
		this.dependencyCheckEnabled = dependencyCheckEnabled;
	}

	// ------------------------------------------------------------------------|
	// --- CLASS METHODS ------------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * Constructs a new {@link DefaultContextConfigurationAttributes} instance
	 * based on the supplied {@link Class} which must declare or inherit a
	 * {@link ContextConfiguration} annotation.
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
	 * <p>
	 * Generates application context configuration locations. If the supplied
	 * <code>locations</code> are <code>null</code> or <em>empty</em> and
	 * <code>generateDefaultLocations</code> is <code>true</code>, default
	 * locations will be
	 * {@link #generateDefaultLocations(Class, String) generated} for the
	 * specified {@link Class} and <code>contextResourcesSuffix</code>;
	 * otherwise, the supplied <code>locations</code> will be
	 * {@link #modifyLocations(String[], Class) modified} if necessary and
	 * returned.
	 * </p>
	 *
	 * @see #generateDefaultLocations(Class, String)
	 * @see #modifyLocations(String[], Class)
	 * @param locations
	 * @param clazz
	 * @param generateDefaultLocations
	 * @param contextResourceSuffix
	 * @return an array of config locations
	 * @see #getConfigPaths()
	 * @see org.springframework.core.io.ResourceLoader#getResource(String)
	 */
	protected final String[] generateLocations(final String[] locations, final Class<?> clazz,
			final boolean generateDefaultLocations, final String contextResourceSuffix) {

		return (ArrayUtils.isEmpty(locations) && generateDefaultLocations) ? generateDefaultLocations(clazz,
				contextResourceSuffix) : modifyLocations(locations, clazz);
	}

	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Generates the default classpath-based locations array based on the
	 * supplied class. For example, if the supplied class is
	 * <code>com.example.MyTest</code>, the generated locations will contain
	 * a single string with a value of &quot;classpath:/com/example/MyTest<code>&lt;suffix&gt;</code>&quot;,
	 * where <code>&lt;suffix&gt;</code> is the value of the supplied
	 * <code>contextResourceSuffix</code> string.
	 * </p>
	 * <p>
	 * Subclasses can override this method to implement a different
	 * <em>default location generation</em> strategy.
	 * </p>
	 *
	 * @param clazz
	 * @param contextResourceSuffix
	 * @return
	 */
	protected String[] generateDefaultLocations(final Class<?> clazz, final String contextResourceSuffix) {

		Assert.notNull(clazz, "clazz can not be null.");
		Assert.hasText(contextResourceSuffix, "contextResourceSuffix can not be empty.");

		return new String[] { ResourceUtils.CLASSPATH_URL_PREFIX + "/"
				+ ClassUtils.convertClassNameToResourcePath(clazz.getName()) + contextResourceSuffix };
	}

	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Generates a modified version of the supplied locations and returns it.
	 * </p>
	 * <p>
	 * A plain path will be treated as a classpath location, e.g.:
	 * "org/springframework/whatever/foo.xml". Note however that you may prefix
	 * path locations with standard Spring resource prefixes. Therefore, a
	 * config location path prefixed with "classpath:" with behave the same as a
	 * plain path, but a config location such as
	 * "file:/some/path/path/location/appContext.xml" will be treated as a
	 * filesystem location.
	 * </p>
	 * <p>
	 * Subclasses can override this method to implement a different
	 * <em>location modification</em> strategy.
	 * </p>
	 *
	 * @param locations
	 * @param clazz
	 * @return an array of modified config locations
	 */
	protected String[] modifyLocations(final String[] locations, final Class<?> clazz) {

		final String[] modifiedLocations = new String[locations.length];

		for (int i = 0; i < locations.length; i++) {
			final String path = locations[i];
			if (path.startsWith("/")) {
				modifiedLocations[i] = ResourceUtils.CLASSPATH_URL_PREFIX + path;
			}
			else {
				modifiedLocations[i] = ResourceUtils.CLASSPATH_URL_PREFIX
						+ StringUtils.cleanPath(ClassUtils.classPackageAsResourcePath(clazz) + "/" + path);
			}
		}
		return modifiedLocations;
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
