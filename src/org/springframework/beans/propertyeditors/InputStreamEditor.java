package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.io.IOException;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceEditor;

/**
 * Editor for java.io.InputStream, to directly feed an InputStream
 * property instead of using a String location property.
 * @author Juergen Hoeller
 * @since 01.04.2004
 */
public class InputStreamEditor extends PropertyEditorSupport {

	private final ResourceEditor resourceEditor;

	/**
	 * Create a new InputStreamEditor,
	 * using the default ResourceEditor underneath.
	 */
	public InputStreamEditor() {
		this.resourceEditor = new ResourceEditor();
	}

	/**
	 * Create a new InputStreamEditor,
	 * using the given ResourceEditor underneath.
	 * @param resourceEditor the ResourceEditor to use
	 */
	public InputStreamEditor(ResourceEditor resourceEditor) {
		this.resourceEditor = resourceEditor;
	}

	public void setAsText(String text) throws IllegalArgumentException {
		this.resourceEditor.setAsText(text);
		Resource resource = (Resource) this.resourceEditor.getValue();
		try {
			setValue(resource.getInputStream());
		}
		catch (IOException ex) {
			throw new IllegalArgumentException("Could not retrieve InputStream from " +
			                                   resource + ": " + ex.getMessage());
		}
	}

}
