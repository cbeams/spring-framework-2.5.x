/*
 * Copyright 2002-2004 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.core.io.support;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;

/**
 * ResourcePatternResolver that applies Ant-style path matching, using Spring's
 * PathMatcher class.
 *
 * <p>Locations can either be suitable for <code>ResourceLoader.getResource</code>
 * (URLs like "file:C:/context.xml", pseudo-URLs like "classpath:/context.xml",
 * relative file paths like "/WEB-INF/context.xml"), or Ant-style patterns
 * like "/WEB-INF/*-context.xml".
 *
 * <p>In the pattern case, the locations have to be resolvable to java.io.File,
 * to allow for searching though the specified directory tree. In particular,
 * this will neither work with WAR files that are not expanded nor with class
 * path resources in a JAR file.
 *
 * <p>There is special support for retrieving multiple class path resources with
 * the same name, via the "classpath*" prefix. For example, "classpath*:/beans.xml"
 * will find all beans.xml files in the class path, be it in "classes" directories
 * or in JAR files. This is particularly useful for auto-detecting config files.
 *
 * <p>If neither given a PathMatcher pattern nor a "classpath*:" location, this
 * resolver will return a single resource via the underlying ResourceLoader.
 *
 * @author Juergen Hoeller
 * @since 01.05.2004
 * @see #CLASSPATH_URL_PREFIX
 * @see org.springframework.util.PathMatcher
 * @see org.springframework.core.io.ResourceLoader#getResource
 */
public class PathMatchingResourcePatternResolver implements ResourcePatternResolver {

	protected final Log logger = LogFactory.getLog(getClass());

	private final ResourceLoader resourceLoader;

	private final ClassLoader classLoader;


	/**
	 * Create a new PathMatchingResourcePatternResolver with a DefaultResourceLoader.
	 * <p>ClassLoader access will happen via the thread context class loader on actual
	 * access (applying to the thread that does the "getResources" call)
	 * @see org.springframework.core.io.DefaultResourceLoader
	 */
	public PathMatchingResourcePatternResolver() {
		this.resourceLoader = new DefaultResourceLoader();
		this.classLoader = null;
	}

	/**
	 * Create a new PathMatchingResourcePatternResolver with a DefaultResourceLoader.
	 * @param classLoader the ClassLoader to load classpath resources with,
	 * or null for using the thread context class loader on actual access
	 * (applying to the thread that does the "getResources" call)
	 * @see org.springframework.core.io.DefaultResourceLoader
	 */
	public PathMatchingResourcePatternResolver(ClassLoader classLoader) {
		this.resourceLoader = new DefaultResourceLoader(classLoader);
		this.classLoader = classLoader;
	}

	/**
	 * Create a new PathMatchingResourcePatternResolver.
	 * <p>ClassLoader access will happen via the thread context class loader on actual
	 * access (applying to the thread that does the "getResources" call)
	 * @param resourceLoader ResourceLoader to load root directories
	 * and actual resources with
	 */
	public PathMatchingResourcePatternResolver(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
		this.classLoader = null;
	}

	/**
	 * Create a new PathMatchingResourcePatternResolver.
	 * @param resourceLoader ResourceLoader to load root directories
	 * and actual resources with
	 * @param classLoader the ClassLoader to load classpath resources with,
	 * or null for using the thread context class loader on actual access
	 * (applying to the thread that does the "getResources" call)
	 */
	public PathMatchingResourcePatternResolver(ResourceLoader resourceLoader, ClassLoader classLoader) {
		this.resourceLoader = resourceLoader;
		this.classLoader = classLoader;
	}

	/**
	 * Return the ResourceLoader that this pattern resolver works with.
	 */
	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}

	/**
	 * Return the ClassLoader that this pattern resolver works with,
	 * or null if using the thread context class loader on actual access
	 * (applying to the thread that does the "getResources" call).
	 */
	public ClassLoader getClassLoader() {
		return classLoader;
	}


	public Resource[] getResources(String locationPattern) throws IOException {
		if (locationPattern.startsWith(CLASSPATH_URL_PREFIX)) {
			// a class path resource (multiple resources for same name possible)
			return findAllClassPathResources(locationPattern.substring(CLASSPATH_URL_PREFIX.length()));
		}
		else if (PathMatcher.isPattern(locationPattern)) {
			// a file pattern
			return findPathMatchingFileResources(locationPattern);
		}
		else {
			// fall back to single resource
			return new Resource[] {this.resourceLoader.getResource(locationPattern)};
		}
	}

	/**
	 * Find all class location resources with the given location via the ClassLoader.
	 * @param location the absolute path within the classpath
	 * @return the result as Resource array
	 * @throws IOException in case of I/O errors
	 * @see java.lang.ClassLoader#getResources
	 */
	protected Resource[] findAllClassPathResources(String location) throws IOException {
		String path = location;
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		ClassLoader cl = this.classLoader;
		if (cl == null) {
			// no class loader specified -> use thread context class loader
			cl = Thread.currentThread().getContextClassLoader();
		}
		Enumeration resourceUrls = cl.getResources(path);
		List result = new ArrayList();
		while (resourceUrls.hasMoreElements()) {
			URL url = (URL) resourceUrls.nextElement();
			result.add(new UrlResource(url));
		}
		return (Resource[]) result.toArray(new Resource[result.size()]);
	}

	/**
	 * Find all file resources that match the given location pattern
	 * via the Ant-style PathMatcher utility.
	 * @param locationPattern the location pattern to match
	 * @return the result as Resource array
	 * @throws IOException in case of I/O errors
	 * @see org.springframework.util.PathMatcher
	 */
	protected Resource[] findPathMatchingFileResources(String locationPattern) throws IOException {
		List result = new ArrayList();
		String rootDirPath = determineRootDir(locationPattern);
		String subPattern = locationPattern.substring(rootDirPath.length());
		File rootDir = this.resourceLoader.getResource(rootDirPath).getFile().getAbsoluteFile();
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
