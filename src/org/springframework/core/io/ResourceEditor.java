package org.springframework.core.io;

import java.beans.PropertyEditorSupport;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Editor for Resource descriptors, to convert String locations to Resource
 * properties automatically instead of using a String location property.
 *
 * <p>The path may contain ${...} placeholders, to be resolved as
 * system properties: e.g. ${user.dir}.
 *
 * <p>Will return an UrlResource if the location value is a URL, and a
 * ClassPathResource if it is a non-URL path or a "classpath:" pseudo-URL.
 *
 * @author Juergen Hoeller
 * @since 28.12.2003
 * @see #PLACEHOLDER_PREFIX
 * @see #PLACEHOLDER_SUFFIX
 * @see #CLASSPATH_URL_PREFIX
 * @see Resource
 * @see System#getProperty(String)
 */
public class ResourceEditor extends PropertyEditorSupport {

	public static final String PLACEHOLDER_PREFIX = "${";

	public static final String PLACEHOLDER_SUFFIX = "}";

	/** Pseudo URL prefix for loading from the class path */
	public static final String CLASSPATH_URL_PREFIX = "classpath:";

	public void setAsText(String text) throws IllegalArgumentException {
		String resolvedPath = resolvePath(text);
		if (resolvedPath.startsWith(CLASSPATH_URL_PREFIX)) {
			setValue(new ClassPathResource(resolvedPath.substring(CLASSPATH_URL_PREFIX.length())));
		}
		try {
			// try URL
			URL url = new URL(resolvedPath);
			setValue(new UrlResource(url));
		}
		catch (MalformedURLException ex) {
			// no URL -> try classpath
			setValue(new ClassPathResource(text));
		}
	}

	/**
	 * Resolve the given path, replacing placeholders with corresponding system
	 * property values if necessary.
	 * @param path the original file path
	 * @return the resolved file path
	 */
	protected String resolvePath(String path) {
		int startIndex = path.indexOf(PLACEHOLDER_PREFIX);
		if (startIndex != -1) {
			int endIndex = path.indexOf(PLACEHOLDER_SUFFIX, startIndex + PLACEHOLDER_PREFIX.length());
			if (endIndex != -1) {
				String placeholder = path.substring(startIndex + PLACEHOLDER_PREFIX.length(), endIndex);
				String propVal = System.getProperty(placeholder);
				if (propVal == null) {
					throw new IllegalArgumentException("Could not resolve placeholder '" + placeholder +
					                                   "' in file path [" + path + "] as system property");
				}
				return path.substring(0, startIndex) + propVal + path.substring(endIndex+1);
			}
		}
		return path;
	}

}
