package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.io.File;

/**
 * Editor for java.io.File, to directly feed a File property
 * instead of using a String file name property.
 * @author Juergen Hoeller
 * @since 09.12.2003
 * @see java.io.File
 */
public class FileEditor extends PropertyEditorSupport {

	public void setAsText(String text) throws IllegalArgumentException {
		setValue(new File(text));
	}

	public String getAsText() {
		return ((File) getValue()).getAbsolutePath();
	}

}
