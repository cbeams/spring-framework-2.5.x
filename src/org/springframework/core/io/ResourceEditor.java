package org.springframework.core.io;

import java.beans.PropertyEditorSupport;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Editor for Resource descriptors, to directly feed a Resource property
 * instead of using a String location property.
 *
 * <p>Will return an UrlResource if the location value is a URL, and a
 * ClassPathResource if it is a non-URL path or a "classpath:" pseudo-URL.
 *
 * @author Juergen Hoeller
 * @since 28.12.2003
 * @see #CLASSPATH_URL_PREFIX
 * @see Resource
 */
public class ResourceEditor extends PropertyEditorSupport {

	/** Pseudo URL prefix for loading from the class path */
	public static final String CLASSPATH_URL_PREFIX = "classpath:";

	public void setAsText(String text) throws IllegalArgumentException {
		if (text.startsWith(CLASSPATH_URL_PREFIX)) {
			setValue(new ClassPathResource(text.substring(CLASSPATH_URL_PREFIX.length())));
		}
		try {
			// try URL
			URL url = new URL(text);
			setValue(new UrlResource(url));
		}
		catch (MalformedURLException ex) {
			// no URL -> try classpath
			setValue(new ClassPathResource(text));
		}
	}

}
