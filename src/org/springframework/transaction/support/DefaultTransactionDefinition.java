package org.springframework.transaction.support;

import org.springframework.transaction.TransactionDefinition;
import org.springframework.util.Constants;

/**
 * Default implementation of the TransactionDefinition interface,
 * offering bean-style configuration and sensible default values
 * (PROPAGATION_REQUIRED, ISOLATION_DEFAULT, TIMEOUT_DEFAULT, readOnly=false).
 *
 * <p>Base class for both TransactionTemplate and DefaultTransactionAttribute.
 *
 * @author Juergen Hoeller
 * @since 08.05.2003
 * @see org.springframework.transaction.support.TransactionTemplate
 * @see org.springframework.transaction.interceptor.DefaultTransactionAttribute
 */
public class DefaultTransactionDefinition implements TransactionDefinition {

	/** Prefix for transaction timeout values in description strings */
	public static final String TIMEOUT_PREFIX = "timeout_";

	/** Marker for read-only transactions in description strings */
	public static final String READ_ONLY_MARKER = "readOnly";

	/** Constants instance for TransactionDefinition */
	private static final Constants constants = new Constants(TransactionDefinition.class);

	private int propagationBehavior = PROPAGATION_REQUIRED;

	private int isolationLevel = ISOLATION_DEFAULT;

	private int timeout = TIMEOUT_DEFAULT;

	private boolean readOnly = false;

	public DefaultTransactionDefinition() {
	}

	public DefaultTransactionDefinition(int propagationBehavior) {
		this.propagationBehavior = propagationBehavior;
	}

	/**
	 * Set the propagation behavior by the name of the respective constant in
	 * TransactionDefinition, e.g. "PROPAGATION_REQUIRED".
	 * @param constantName name of the constant
	 * @throws java.lang.IllegalArgumentException if an invalid constant was specified
	 * @see org.springframework.transaction.TransactionDefinition#PROPAGATION_REQUIRED
	 */
	public final void setPropagationBehaviorName(String constantName) throws IllegalArgumentException {
		if (constantName == null || !constantName.startsWith(PROPAGATION_CONSTANT_PREFIX)) {
			throw new IllegalArgumentException("Only propagation constants allowed");
		}
		setPropagationBehavior(constants.asNumber(constantName).intValue());
	}

	public final void setPropagationBehavior(int propagationBehavior) {
		if (!constants.getValues(PROPAGATION_CONSTANT_PREFIX).contains(new Integer(propagationBehavior))) {
			throw new IllegalArgumentException("Only values of propagation constants allowed");
		}
		this.propagationBehavior = propagationBehavior;
	}

	public final int getPropagationBehavior() {
		return propagationBehavior;
	}

	/**
	 * Set the isolation level by the name of the respective constant in
	 * TransactionDefinition, e.g. "ISOLATION_DEFAULT".
	 * @param constantName name of the constant
	 * @throws java.lang.IllegalArgumentException if an invalid constant was specified
	 * @see org.springframework.transaction.TransactionDefinition#ISOLATION_DEFAULT
	 */
	public final void setIsolationLevelName(String constantName) throws IllegalArgumentException {
		if (constantName == null || !constantName.startsWith(ISOLATION_CONSTANT_PREFIX)) {
			throw new IllegalArgumentException("Only isolation constants allowed");
		}
		setIsolationLevel(constants.asNumber(constantName).intValue());
	}

	public final void setIsolationLevel(int isolationLevel) {
		if (!constants.getValues(ISOLATION_CONSTANT_PREFIX).contains(new Integer(isolationLevel))) {
			throw new IllegalArgumentException("Only values of isolation constants allowed");
		}
		this.isolationLevel = isolationLevel;
	}

	public final int getIsolationLevel() {
		return isolationLevel;
	}

	public final void setTimeout(int timeout) {
		if (timeout < TIMEOUT_DEFAULT) {
			throw new IllegalArgumentException("Timeout must be a positive integer or TIMEOUT_DEFAULT");
		}
		this.timeout = timeout;
	}

	public final int getTimeout() {
		return timeout;
	}

	public final void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public final boolean isReadOnly() {
		return readOnly;
	}

	/**
	 * This implementation of equals compares the toString results.
	 * @see #toString
	 */
	public boolean equals(Object other) {
		return (other instanceof TransactionDefinition) && toString().equals(other.toString());
	}

	/**
	 * This implementation of hashCode returns toString's hash code.
	 * @see #toString
	 */
	public int hashCode() {
		return toString().hashCode();
	}

	/**
	 * Return an identifying description of this transaction definition.
	 * Available for subclasses.
	 */
	protected final StringBuffer getDefinitionDescription() {
		StringBuffer desc = new StringBuffer();
		desc.append(constants.toCode(new Integer(this.propagationBehavior), PROPAGATION_CONSTANT_PREFIX));
		desc.append(',');
		desc.append(constants.toCode(new Integer(this.isolationLevel), ISOLATION_CONSTANT_PREFIX));
		if (this.timeout != TIMEOUT_DEFAULT) {
			desc.append(',');
			desc.append(TIMEOUT_PREFIX + this.timeout);
		}
		if (this.readOnly) {
			desc.append(',');
			desc.append(READ_ONLY_MARKER);
		}
		return desc;
	}

	/**
	 * Return an identifying description of this transaction definition.
	 * The format matches the one used by TransactionAttributeEditor,
	 * to be able to feed toString results into TransactionAttribute properties.
	 * <p>Has to be overridden in subclasses for correct equals and hashCode
	 * behavior. Alternatively, equals and hashCode can be overridden themselves.
	 * @see org.springframework.transaction.interceptor.TransactionAttributeEditor
	 */
	public String toString() {
		return getDefinitionDescription().toString();
	}

}
