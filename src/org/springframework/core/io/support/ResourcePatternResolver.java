package org.springframework.core.io.support;

import java.io.IOException;

import org.springframework.core.io.Resource;

/**
 * Strategy interface for resolving a location pattern into Resource objects.
 *
 * <p>Can be used with any sort of location pattern: Input patterns have
 * to match the strategy implementation. This interface just specifies
 * the conversion method rather than a specific pattern format.
 *
 * @author Juergen Hoeller
 * @since 01.05.2004
 * @see PathMatchingResourcePatternResolver
 */
public interface ResourcePatternResolver {

	/**
	 * Resolve the given location pattern into Resource objects.
	 * @param locationPattern the location pattern to resolve
	 * @return the corresponding Resource objects
	 * @throws IOException in case of I/O errors
	 */
	Resource[] getResources(String locationPattern) throws IOException;

}
