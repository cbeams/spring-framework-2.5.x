package org.springframework.transaction.support;

/**
 * Adapter for the TransactionSynchronization interface.
 * Contains empty implementations of all interface methods,
 * for easy overriding of single methods.
 * @author Juergen Hoeller
 * @since 22.01.2004
 */
public class TransactionSynchronizationAdapter implements TransactionSynchronization {

	public void suspend() {
	}

	public void resume() {
	}

	public void beforeCommit() {
	}

	public void beforeCompletion() {
	}

	public void afterCompletion(int status) {
	}

}
