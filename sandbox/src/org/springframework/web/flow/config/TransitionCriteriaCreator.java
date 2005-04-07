
package org.springframework.web.flow.config;

import org.springframework.web.flow.TransitionCriteria;

/**
 * @author robh
 */
public interface TransitionCriteriaCreator {

	TransitionCriteria create(String on);

}
