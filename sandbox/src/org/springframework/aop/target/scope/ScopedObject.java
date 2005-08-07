package org.springframework.aop.target.scope;

/**
 * Interface for use as an introduction for scoped objects.
 * Objects created from the ScopedProxyFactoryBean can be
 * cast to this interface, enabling their Handle and other
 * information to be obtained.
 * @author Rod Johnson
 * @since 1.3
 */
public interface ScopedObject extends ScopingConfig {

	Handle getHandle();
	
	/**
	 * Remove this object. No further calls may be made.
	 *
	 */
	void remove();
	
}
