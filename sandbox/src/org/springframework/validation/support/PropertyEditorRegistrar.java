/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.validation.support;

import org.springframework.validation.DataBinder;

/**
 * @author Keith Donald
 */
public interface PropertyEditorRegistrar {
	public void registerCustomEditors(DataBinder binder);
}