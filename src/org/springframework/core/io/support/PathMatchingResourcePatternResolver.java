package org.springframework.core.io.support;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;

/**
 * ResourcePatternResolver that applies Ant-style path matching, using Spring's
 * PathMatcher class. Also inherits the capability to retrieve multiple class path
 * resources with the same name from ClassPathResourcePatternResolver.
 *
 * <p>Locations can either be suitable for <code>ResourceLoader.getResource</code>
 * (URLs like "file:C:/context.xml", pseudo-URLs like "classpath:/context.xml",
 * relative file paths like "/WEB-INF/context.xml"), or Ant-style patterns
 * like "/WEB-INF/*-context.xml".
 *
 * <p>In the pattern case, the locations have to be resolvable to java.io.File,
 * to allow for searching though the specified directory tree. In particular,
 * this will neither work with WAR files that are not expanded nor with
 * class path resources in a JAR file.
 *
 * @author Juergen Hoeller
 * @since 01.05.2004
 * @see org.springframework.util.PathMatcher
 * @see org.springframework.core.io.ResourceLoader#getResource
 */
public class PathMatchingResourcePatternResolver extends ClassPathResourcePatternResolver {

	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * Create a new PathMatchingResourcePatternResolver with a DefaultResourceLoader.
	 * @see org.springframework.core.io.DefaultResourceLoader
	 */
	public PathMatchingResourcePatternResolver() {
		super();
	}

	/**
	 * Create a new PathMatchingResourcePatternResolver.
	 * @param resourceLoader ResourceLoader to load root directories
	 * and actual resources with
	 */
	public PathMatchingResourcePatternResolver(ResourceLoader resourceLoader) {
		super(resourceLoader);
	}

	public Resource[] getResources(String locationPattern) throws IOException {
		// check for file pattern
		if (PathMatcher.isPattern(locationPattern)) {
			List result = new ArrayList();
			String rootDirPath = determineRootDir(locationPattern);
			String subPattern = locationPattern.substring(rootDirPath.length());
			File rootDir = getResourceLoader().getResource(rootDirPath).getFile().getAbsoluteFile();
			if (logger.isDebugEnabled()) {
				logger.debug("Looking for matching resources in directory tree [" + rootDir.getPath() + "]");
			}
			List matchingFiles = retrieveMatchingFiles(rootDir, subPattern);
			if (logger.isInfoEnabled()) {
				logger.info("Resolved location pattern [" + locationPattern + "] to file paths: " + matchingFiles);
			}
			for (Iterator it = matchingFiles.iterator(); it.hasNext();) {
				File file = (File) it.next();
				result.add(new FileSystemResource(file));
			}
			return (Resource[]) result.toArray(new Resource[result.size()]);
		}

		// else fall back to class path resources respectively a single resource
		else {
			return super.getResources(locationPattern);
		}
	}

	/**
	 * Determine the root directory for the given location.
	 * <p>Used for determining the starting point for file matching,
	 * resolving the root directory location to a java.io.File and
	 * passing it into <code>retrieveMatchingFiles</code>, with the
	 * remainder of the location as pattern.
	 * <p>Will return "/WEB-INF" for the pattern "/WEB-INF/*.xml",
	 * for example.
	 * @param location the location to check
	 * @return the part of the location that denotes the root directory
	 * @see #retrieveMatchingFiles
	 */
	protected String determineRootDir(String location) {
		int patternStart = location.length();
		int asteriskIndex = location.indexOf('*');
		int questionMarkIndex = location.indexOf('?');
		if (asteriskIndex != -1 || questionMarkIndex != -1) {
			patternStart = (asteriskIndex > questionMarkIndex ? asteriskIndex : questionMarkIndex);
		}
		int rootDirEnd = location.lastIndexOf('/', patternStart);
		return (rootDirEnd != -1 ? location.substring(0, rootDirEnd) : "");
	}

	/**
	 * Retrieve files that match the given path pattern,
	 * checking the given directory and its subdirectories.
	 * @param rootDir the directory to start from
	 * @param pattern the pattern to match against,
	 * relative to the root directory
	 * @return the List of matching File instances
	 * @throws IOException if directory contents could not be retrieved
	 */
	protected List retrieveMatchingFiles(File rootDir, String pattern) throws IOException {
		if (!rootDir.isDirectory()) {
			throw new IllegalArgumentException("'rootDir' parameter [" + rootDir + "] does not denote a directory");
		}
		String fullPattern = StringUtils.replace(rootDir.getAbsolutePath(), File.separator, "/");
		if (!pattern.startsWith("/")) {
			fullPattern += "/";
		}
		fullPattern = fullPattern + StringUtils.replace(pattern, File.separator, "/");
		List result = new ArrayList();
		doRetrieveMatchingFiles(fullPattern, rootDir, result);
		return result;
	}

	/**
	 * Recursively retrieve files that match the given pattern,
	 * adding them to the given result list.
	 * @param fullPattern the pattern to match against,
	 * with preprended root directory path
	 * @param dir the current directory
	 * @param result the list of matching files to add to
	 * @throws IOException if directory contents could not be retrieved
	 */
	protected void doRetrieveMatchingFiles(String fullPattern, File dir, List result) throws IOException {
		if (logger.isDebugEnabled()) {
			logger.debug("Searching directory [" + dir.getAbsolutePath() +
									 "] for files matching pattern [" + fullPattern + "]");
		}
		File[] dirContents = dir.listFiles();
		if (dirContents == null) {
			throw new IOException("Could not retrieve contents of directory [" + dir.getAbsolutePath() + "]");
		}
		boolean dirDepthNotFixed = (fullPattern.indexOf("**") != -1);
		for (int i = 0; i < dirContents.length; i++) {
			String currPath = StringUtils.replace(dirContents[i].getAbsolutePath(), File.separator, "/");
			if (dirContents[i].isDirectory() &&
					(dirDepthNotFixed ||
					StringUtils.countOccurrencesOf(currPath, "/") < StringUtils.countOccurrencesOf(fullPattern, "/"))) {
				doRetrieveMatchingFiles(fullPattern, dirContents[i], result);
			}
			if (PathMatcher.match(fullPattern, currPath)) {
				result.add(dirContents[i]);
			}
		}
	}

}
