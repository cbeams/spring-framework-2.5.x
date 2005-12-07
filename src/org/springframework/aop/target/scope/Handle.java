package org.springframework.aop.target.scope;

import java.io.Serializable;

/**
 * Identifier for a scoped object, which may be
 * able to be used to reconnect to it.
 * @author Rod Johnson
 * @since 2.0
 */
public interface Handle extends Serializable {
	
	/**
	 * Can the object be reloaded from the handle?
	 * @return whether the object can be reloaded using the
	 * handle. Returns false if this is not possible,
	 * in which case the handle is only usable to obtain information,
	 * not to reconnect to the object instance.
	 */
	boolean isPersistent();

}
