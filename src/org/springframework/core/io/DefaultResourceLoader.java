package org.springframework.core.io;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Default implementation of the ResourceLoader interface.
 * Used by ResourceEditor, but also suitable for standalone usage.
 *
 * <p>Will return an UrlResource if the location value is a URL, and a
 * ClassPathResource if it is a non-URL path or a "classpath:" pseudo-URL.
 *
 * @author Juergen Hoeller
 * @since 10.03.2004
 * @see #CLASSPATH_URL_PREFIX
 * @see ResourceEditor
 */
public class DefaultResourceLoader implements ResourceLoader {

	public Resource getResource(String location) {
		if (location.startsWith(CLASSPATH_URL_PREFIX)) {
			return new ClassPathResource(location.substring(CLASSPATH_URL_PREFIX.length()));
		}
		else {
			try {
				// try URL
				URL url = new URL(location);
				return new UrlResource(url);
			}
			catch (MalformedURLException ex) {
				// no URL -> try class path
				return new ClassPathResource(location);
			}
		}
	}

}
