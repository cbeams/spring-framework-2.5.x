package org.springframework.context.config;

import java.beans.PropertyEditorSupport;

import org.springframework.context.ApplicationContext;

/**
 * ApplicationContext-aware Editor for Resource descriptors.
 *
 * <p>Delegates to the ApplicationContext's getResource method for resolving
 * resource locations to Resource instances. Resource loading behavior is
 * specific to the context implementation.
 *
 * @author Juergen Hoeller
 * @since 28.12.2003
 * @see org.springframework.context.ApplicationContext#getResource
 */
public class ContextResourceEditor extends PropertyEditorSupport {

	private final ApplicationContext applicationContext;

	/**
	 * Create a new ContextResourceEditor for the given context.
	 * @param applicationContext context to resolve resources with
	 */
	public ContextResourceEditor(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public void setAsText(String text) throws IllegalArgumentException {
		setValue(this.applicationContext.getResource(text));
	}

}
