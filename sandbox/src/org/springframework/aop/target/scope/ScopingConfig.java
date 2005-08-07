package org.springframework.aop.target.scope;

/**
 * Configuration interface supported by scoping-related
 * classes, as concrete inheritance cannot be used as they
 * require separate superclasses.
 * @author Rod Johnson
 * @since 1.3
 */
public interface ScopingConfig {

	String getSessionKey();

	String getTargetBeanName();

	ScopeMap getScopeMap();

}
