
package org.springframework.web.flow.config;

import org.springframework.web.flow.TransitionCriteria;

/**
 * Interface for factories that can create <code>TransitionCriteria</code>
 * objects. This is typically used by a flow builder to create the transition
 * criteria objects that guard transition execution. 
 * 
 * @see org.springframework.web.flow.TransitionCriteria
 * 
 * @author Rob Harraop
 * @author Erwin Vervaet
 * @author Keith Donald
 */
public interface TransitionCriteriaCreator {

	/**
	 * Create a new transition criteria object that will match <i>on</i>
	 * given criteria, which are expressed as a string.
	 * @param onCriteria the criteria to match on
	 * @return the transition criteria object
	 */
	TransitionCriteria create(String onCriteria);

}
