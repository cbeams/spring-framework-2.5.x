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
package org.springframework.test.context.support;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.test.annotation.TransactionConfiguration;
import org.springframework.test.context.TransactionConfigurationAttributes;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.Assert;

/**
 * <p>
 * Default implementation of the {@link TransactionConfigurationAttributes}
 * interface, which also provides a static factory method for
 * {@link #constructAttributes(Class) constructing} configuration attributes for
 * a specified class.
 * </p>
 *
 * @see TransactionConfiguration
 * @see #constructAttributes(Class)
 * @author Sam Brannen
 * @version $Revision: 1.1 $
 * @since 2.1
 */
public class DefaultTransactionConfigurationAttributes implements TransactionConfigurationAttributes {

	// ------------------------------------------------------------------------|
	// --- CONSTANTS ----------------------------------------------------------|
	// ------------------------------------------------------------------------|

	/** serialVersionUID. */
	private static final long	serialVersionUID	= -9029260490495684447L;

	/** Class Logger. */
	private static final Log	LOG					= LogFactory.getLog(DefaultTransactionConfigurationAttributes.class);

	// ------------------------------------------------------------------------|
	// --- INSTANCE VARIABLES -------------------------------------------------|
	// ------------------------------------------------------------------------|

	private final String		transactionManagerName;

	private final boolean		defaultRollback;

	// ------------------------------------------------------------------------|
	// --- CONSTRUCTORS -------------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * Constructs a new {@link DefaultTransactionConfigurationAttributes}
	 * instance from the supplied arguments.
	 *
	 * @param transactionManagerName The name of the
	 *        {@link PlatformTransactionManager} that is to be used to drive
	 *        transactions.
	 * @param defaultRollback Boolean flag denoting whether or not transactions
	 *        should be rolled back by default.
	 * @throws IllegalArgumentException if the supplied transaction manager name
	 *         is <code>null</code>.
	 */
	public DefaultTransactionConfigurationAttributes(final String transactionManagerName, final boolean defaultRollback) {

		Assert.notNull(transactionManagerName, "transactionManagerName can not be null.");
		this.transactionManagerName = transactionManagerName;
		this.defaultRollback = defaultRollback;
	}

	// ------------------------------------------------------------------------|
	// --- CLASS METHODS ------------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * Constructs a new {@link DefaultTransactionConfigurationAttributes}
	 * instance based on the supplied {@link Class} which may optionally declare
	 * or inherit a {@link TransactionConfiguration} annotation. If a
	 * {@link TransactionConfiguration} annotation is not present for the
	 * supplied class, the <em>default values</em> for attributes defined in
	 * {@link TransactionConfiguration} will be used instead.
	 *
	 * @param clazz The class for which the
	 *        {@link TransactionConfigurationAttributes} should be constructed.
	 * @return a new TransactionConfigurationAttributes instance.
	 * @throws IllegalArgumentException if the supplied class is
	 *         <code>null</code>.
	 */
	public static TransactionConfigurationAttributes constructAttributes(final Class<?> clazz) {

		Assert.notNull(clazz, "clazz can not be null.");
		final Class<TransactionConfiguration> annotationType = TransactionConfiguration.class;
		final TransactionConfiguration config = clazz.getAnnotation(annotationType);

		if (LOG.isDebugEnabled()) {
			LOG.debug("Retrieved TransactionConfiguration [" + config + "] for test class [" + clazz + "].");
		}

		String transactionManagerName;
		boolean defaultRollback;

		if (config != null) {
			transactionManagerName = config.transactionManager();
			defaultRollback = config.defaultRollback();
		}
		else {
			transactionManagerName = (String) AnnotationUtils.getDefaultValue(annotationType, "transactionManager");
			defaultRollback = (Boolean) AnnotationUtils.getDefaultValue(annotationType, "defaultRollback");
		}

		return new DefaultTransactionConfigurationAttributes(transactionManagerName, defaultRollback);
	}

	// ------------------------------------------------------------------------|
	// --- INSTANCE METHODS ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	@Override
	public boolean equals(final Object object) {

		if (!(object instanceof DefaultTransactionConfigurationAttributes)) {
			return false;
		}
		final DefaultTransactionConfigurationAttributes that = (DefaultTransactionConfigurationAttributes) object;

		return new EqualsBuilder()

		.append(this.transactionManagerName, that.transactionManagerName)

		.append(this.defaultRollback, that.defaultRollback)

		.isEquals();
	}

	// ------------------------------------------------------------------------|

	@Override
	public int hashCode() {

		return new HashCodeBuilder(268173669, -543589461)

		.append(this.transactionManagerName)

		.append(this.defaultRollback)

		.toHashCode();
	}

	// ------------------------------------------------------------------------|

	@Override
	public String toString() {

		return new ToStringBuilder(this)

		.append("transactionManagerName", this.transactionManagerName)

		.append("defaultRollback", this.defaultRollback)

		.toString();
	}

	// ------------------------------------------------------------------------|

	@Override
	public String getTransactionManagerName() {

		return this.transactionManagerName;
	}

	// ------------------------------------------------------------------------|

	@Override
	public boolean isDefaultRollback() {

		return this.defaultRollback;
	}

	// ------------------------------------------------------------------------|

}
