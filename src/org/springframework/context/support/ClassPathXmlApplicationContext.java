package org.springframework.context.support;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.util.ClassLoaderUtils;

/**
 * Standalone XML application context, taking the context definition
 * files from the classpath. Mainly useful for test harnesses,
 * but also for application contexts embedded within JARs.
 *
 * <p>Note: Generally treats (file) paths as class path resources,
 * when using ApplicationContext.getResourceAsStream.
 * Only supports full classpath names including package specification,
 * like "/mypackage/myresource.dat".
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.context.ApplicationContext#getResourceAsStream
 * @see #getResourceByPath
 */
public class ClassPathXmlApplicationContext extends FileSystemXmlApplicationContext {

	public ClassPathXmlApplicationContext(String locations) throws ApplicationContextException, IOException {
		super(locations);
	}

	public ClassPathXmlApplicationContext(String[] locations) throws ApplicationContextException, IOException {
		super(locations);
	}

	protected ApplicationContext createParentContext(String[] locations) throws IOException {
		return new ClassPathXmlApplicationContext(locations);
	}

	/**
	 * This implementation treats paths as class path resources.
	 * Only supports full class path names including package specification,
	 * like "/mypackage/myresource.dat". A root slash gets prepended to
	 * the path if not already contained.
	 */
	protected InputStream getResourceByPath(String path) throws IOException {
		if (!path.startsWith("/")) {
			// always use root, as relative loading doesn't make sense
			path = "/" + path;
		}
		return ClassLoaderUtils.getResourceAsStream(getClass(), path);
	}

	/**
	 * This implementation returns null, as there is no base path for
	 * class path resources.
	 */
	public String getResourceBasePath() {
		return null;
	}

}
