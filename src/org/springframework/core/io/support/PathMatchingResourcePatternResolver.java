/*
 * Copyright 2002-2006 the original author or authors.
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
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.CollectionFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.PathMatcher;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

/**
 * ResourcePatternResolver implementation that applies Ant-style path matching,
 * using Spring's PathMatcher utility.
 *
 * <p>Locations can either be suitable for <code>ResourceLoader.getResource</code>
 * (URLs like "file:C:/context.xml", pseudo-URLs like "classpath:/context.xml",
 * relative file paths like "/WEB-INF/context.xml"), or Ant-style patterns
 * like "/WEB-INF/*-context.xml".
 *
 * <p>In the pattern case, the location has to be resolvable to <code>java.io.File</code>
 * or to a "jar:" URL (leading to a <code>java.net.JarURLConnection</code>)
 * to allow for searching though the specified directory tree. In particular,
 * this is not guaranteed to work with a WAR file that is not expanded.
 *
 * <p>There is special support for retrieving multiple class path resources with the
 * same name, via the "classpath*" prefix. For example, "classpath*:META-INF/beans.xml"
 * will find all "beans.xml" files in the class path, be it in "classes" directories
 * or in JAR files. This is particularly useful for autodetecting config files
 * of the same name at the same location within each jar file.
 *
 * <p>The "classpath*:" prefix can also be combined with a PathMatcher pattern, for
 * example "classpath*:META-INF/*-beans.xml". In this case, all matching resources
 * in the class path will be found, even if multiple resources of the same name
 * exist in different jar files.
 *
 * <p><b>WARNING:</b> Note that "classpath*:" will only work reliably with at least
 * one root directory before the pattern starts, unless the actual target files
 * reside in the file system. This means that a pattern like "classpath*:*.xml"
 * will <i>not</i> retrieve files from the root of jar files but rather only from
 * the root of expanded directories. This originates from a limitation in the JDK's
 * <code>ClassLoader.getResources</code> method which only returns file system
 * locations for a passed-in empty String (indicating potential roots to search).
 *
 * <p>Warning: Ant-style patterns with "classpath:" resources are not guaranteed to
 * find matching resources if the root package to search is available in multiple
 * class path locations. Preferably, use "classpath*:" with the same Ant-style
 * pattern in such a case, which will search <i>all</i> class path locations that
 * contain the root package.
 *
 * <p>If neither given a PathMatcher pattern nor a "classpath*:" location, this
 * resolver will return a single resource via the underlying ResourceLoader.
 *
 * @author Juergen Hoeller
 * @since 1.0.2
 * @see #CLASSPATH_ALL_URL_PREFIX
 * @see org.springframework.util.AntPathMatcher
 * @see org.springframework.core.io.ResourceLoader#getResource(String)
 * @see java.lang.ClassLoader#getResources(String)
 */
public class PathMatchingResourcePatternResolver implements ResourcePatternResolver {

	/** URL protocol for an entry from a jar file: "jar" */
	private static final String URL_PROTOCOL_JAR = "jar";

	/** URL protocol for an entry from a zip file: "zip" */
	private static final String URL_PROTOCOL_ZIP = "zip";

	/** URL protocol for an entry from a WebSphere jar file: "wsjar" */
	private static final String URL_PROTOCOL_WSJAR = "wsjar";

	/** Separator between JAR URL and file path within the JAR */
	private static final String JAR_URL_SEPARATOR = "!/";


	protected final Log logger = LogFactory.getLog(getClass());

	private final ResourceLoader resourceLoader;

	private ClassLoader classLoader;

	private PathMatcher pathMatcher = new AntPathMatcher();


	/**
	 * Create a new PathMatchingResourcePatternResolver with a DefaultResourceLoader.
	 * <p>ClassLoader access will happen via the thread context class loader.
	 * @see org.springframework.core.io.DefaultResourceLoader
	 */
	public PathMatchingResourcePatternResolver() {
		this(new DefaultResourceLoader(), null);
	}

	/**
	 * Create a new PathMatchingResourcePatternResolver with a DefaultResourceLoader.
	 * @param classLoader the ClassLoader to load classpath resources with,
	 * or <code>null</code> for using the thread context class loader
	 * @see org.springframework.core.io.DefaultResourceLoader
	 */
	public PathMatchingResourcePatternResolver(ClassLoader classLoader) {
		this(new DefaultResourceLoader(classLoader), classLoader);
	}

	/**
	 * Create a new PathMatchingResourcePatternResolver.
	 * <p>ClassLoader access will happen via the thread context class loader.
	 * @param resourceLoader the ResourceLoader to load root directories and
	 * actual resources with
	 */
	public PathMatchingResourcePatternResolver(ResourceLoader resourceLoader) {
		this(resourceLoader, null);
	}

	/**
	 * Create a new PathMatchingResourcePatternResolver.
	 * @param resourceLoader the ResourceLoader to load root directories and
	 * actual resources with
	 * @param classLoader the ClassLoader to load classpath resources with,
	 * or <code>null</code> for using the thread context class loader
	 */
	public PathMatchingResourcePatternResolver(ResourceLoader resourceLoader, ClassLoader classLoader) {
		Assert.notNull(resourceLoader, "ResourceLoader must not be null");
		this.resourceLoader = resourceLoader;
		this.classLoader = (classLoader != null ? classLoader : ClassUtils.getDefaultClassLoader());
	}


	/**
	 * Return the ResourceLoader that this pattern resolver works with.
	 */
	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}

	/**
	 * Return the ClassLoader that this pattern resolver works with
	 * (never <code>null</code>).
	 */
	public ClassLoader getClassLoader() {
		return classLoader;
	}

	/**
	 * Set the PathMatcher implementation to use for this
	 * resource pattern resolver. Default is AntPathMatcher.
	 * @see org.springframework.util.AntPathMatcher
	 */
	public void setPathMatcher(PathMatcher pathMatcher) {
		Assert.notNull(pathMatcher, "PathMatcher must not be null");
		this.pathMatcher = pathMatcher;
	}

	/**
	 * Return the PathMatcher that this resource pattern resolver uses.
	 */
	public PathMatcher getPathMatcher() {
		return pathMatcher;
	}


	public Resource getResource(String location) {
		return getResourceLoader().getResource(location);
	}

	public Resource[] getResources(String locationPattern) throws IOException {
		Assert.notNull(locationPattern, "Location pattern must not be null");
		if (locationPattern.startsWith(CLASSPATH_ALL_URL_PREFIX)) {
			// a class path resource (multiple resources for same name possible)
			if (getPathMatcher().isPattern(locationPattern.substring(CLASSPATH_ALL_URL_PREFIX.length()))) {
				// a class path resource pattern
				return findPathMatchingResources(locationPattern);
			}
			else {
				// all class path resources with the given name
				return findAllClassPathResources(locationPattern.substring(CLASSPATH_ALL_URL_PREFIX.length()));
			}
		}
		else {
			if (getPathMatcher().isPattern(locationPattern)) {
				// a file pattern
				return findPathMatchingResources(locationPattern);
			}
			else {
				// a single resource with the given name
				return new Resource[] {getResourceLoader().getResource(locationPattern)};
			}
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
		Enumeration resourceUrls = getClassLoader().getResources(path);
		Set result = CollectionFactory.createLinkedSetIfPossible(16);
		while (resourceUrls.hasMoreElements()) {
			URL url = (URL) resourceUrls.nextElement();
			result.add(new UrlResource(url));
		}
		return (Resource[]) result.toArray(new Resource[result.size()]);
	}

	/**
	 * Find all resources that match the given location pattern via the
	 * Ant-style PathMatcher. Supports resources in jar files and zip files
	 * and in the file system.
	 * @param locationPattern the location pattern to match
	 * @return the result as Resource array
	 * @throws IOException in case of I/O errors
	 * @see #doFindPathMatchingJarResources
	 * @see #doFindPathMatchingFileResources
	 * @see org.springframework.util.PathMatcher
	 */
	protected Resource[] findPathMatchingResources(String locationPattern) throws IOException {
		String rootDirPath = determineRootDir(locationPattern);
		String subPattern = locationPattern.substring(rootDirPath.length());
		Resource[] rootDirResources = getResources(rootDirPath);
		Set result = CollectionFactory.createLinkedSetIfPossible(16);
		for (int i = 0; i < rootDirResources.length; i++) {
			Resource rootDirResource = rootDirResources[i];
			if (isJarResource(rootDirResource)) {
				result.addAll(doFindPathMatchingJarResources(rootDirResource, subPattern));
			}
			else {
				result.addAll(doFindPathMatchingFileResources(rootDirResource, subPattern));
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Resolved location pattern [" + locationPattern + "] to resources " + result);
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
	 * @param location the location to checkn
	 * @return the part of the location that denotes the root directory
	 * @see #retrieveMatchingFiles
	 */
	protected String determineRootDir(String location) {
		int prefixEnd = location.indexOf(":") + 1;
		int rootDirEnd = location.length();
		while (rootDirEnd > prefixEnd && getPathMatcher().isPattern(location.substring(prefixEnd, rootDirEnd))) {
			rootDirEnd = location.lastIndexOf('/', rootDirEnd - 2) + 1;
		}
		if (rootDirEnd == 0) {
			rootDirEnd = prefixEnd;
		}
		return location.substring(0, rootDirEnd);
	}

	/**
	 * Return whether the given resource handle indicates a jar resource
	 * that the <code>doFindPathMatchingJarResources</code> method can handle.
	 * <p>The default implementation checks against the URL protocols
	 * "jar", "zip" and "wsjar" (the latter are used by BEA WebLogic Server
	 * and IBM WebSphere, respectively, but can be treated like jar files).
	 * @param resource the resource handle to check
	 * (usually the root directory to start path matching from)
	 * @see #doFindPathMatchingJarResources
	 */
	protected boolean isJarResource(Resource resource) throws IOException {
		String protocol = resource.getURL().getProtocol();
		return (URL_PROTOCOL_JAR.equals(protocol) ||
				URL_PROTOCOL_ZIP.equals(protocol) ||
				URL_PROTOCOL_WSJAR.equals(protocol));
	}

	/**
	 * Find all resources in jar files that match the given location pattern
	 * via the Ant-style PathMatcher.
	 * @param rootDirResource the root directory as Resource
	 * @param subPattern the sub pattern to match (below the root directory)
	 * @return the Set of matching Resource instances
	 * @throws IOException in case of I/O errors
	 * @see java.net.JarURLConnection
	 * @see org.springframework.util.PathMatcher
	 */
	protected Set doFindPathMatchingJarResources(Resource rootDirResource, String subPattern) throws IOException {
		URLConnection con = rootDirResource.getURL().openConnection();
		JarFile jarFile = null;
		String jarFileUrl = null;
		String rootEntryPath = null;

		if (con instanceof JarURLConnection) {
			// Should usually be the case for traditional JAR files.
			JarURLConnection jarCon = (JarURLConnection) con;
			jarFile = jarCon.getJarFile();
			jarFileUrl = jarCon.getJarFileURL().toExternalForm();
			rootEntryPath = jarCon.getJarEntry().getName();
		}
		else {
			// No JarURLConnection -> need to resort to URL file parsing.
			// We'll assume URLs of the format "jar:path!/entry", with the protocol
			// being arbitrary as long as following the entry format.
			// We'll also handle paths with and without leading "file:" prefix.
			String urlFile = rootDirResource.getURL().getFile();
			int separatorIndex = urlFile.indexOf(JAR_URL_SEPARATOR);
			jarFileUrl = urlFile.substring(0, separatorIndex);
			if (jarFileUrl.startsWith(ResourceUtils.FILE_URL_PREFIX)) {
				jarFileUrl = jarFileUrl.substring(ResourceUtils.FILE_URL_PREFIX.length());
			}
			jarFile = new JarFile(jarFileUrl);
			jarFileUrl = ResourceUtils.FILE_URL_PREFIX + jarFileUrl;
			rootEntryPath = urlFile.substring(separatorIndex + JAR_URL_SEPARATOR.length());
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Looking for matching resources in jar file [" + jarFileUrl + "]");
		}
		if (!rootEntryPath.endsWith("/")) {
			// Root entry path must end with slash to allow for proper matching.
			// The Sun JRE does not return a slash here, but BEA JRockit does.
			rootEntryPath = rootEntryPath + "/";
		}
		Set result = CollectionFactory.createLinkedSetIfPossible(8);
		for (Enumeration entries = jarFile.entries(); entries.hasMoreElements();) {
			JarEntry entry = (JarEntry) entries.nextElement();
			String entryPath = entry.getName();
			if (entryPath.startsWith(rootEntryPath)) {
				String relativePath = entryPath.substring(rootEntryPath.length());
				if (getPathMatcher().match(subPattern, relativePath)) {
					result.add(rootDirResource.createRelative(relativePath));
				}
			}
		}
		return result;
	}

	/**
	 * Find all resources in the file system that match the given location pattern
	 * via the Ant-style PathMatcher.
	 * @param rootDirResource the root directory as Resource
	 * @param subPattern the sub pattern to match (below the root directory)
	 * @return the Set of matching Resource instances
	 * @throws IOException in case of I/O errors
	 * @see #retrieveMatchingFiles
	 * @see org.springframework.util.PathMatcher
	 */
	protected Set doFindPathMatchingFileResources(Resource rootDirResource, String subPattern) throws IOException {
		File rootDir = rootDirResource.getFile().getAbsoluteFile();
		if (logger.isDebugEnabled()) {
			logger.debug("Looking for matching resources in directory tree [" + rootDir.getPath() + "]");
		}
		Set matchingFiles = retrieveMatchingFiles(rootDir, subPattern);
		Set result = CollectionFactory.createLinkedSetIfPossible(matchingFiles.size());
		for (Iterator it = matchingFiles.iterator(); it.hasNext();) {
			File file = (File) it.next();
			result.add(new FileSystemResource(file));
		}
		return result;
	}

	/**
	 * Retrieve files that match the given path pattern,
	 * checking the given directory and its subdirectories.
	 * @param rootDir the directory to start from
	 * @param pattern the pattern to match against,
	 * relative to the root directory
	 * @return the Set of matching File instances
	 * @throws IOException if directory contents could not be retrieved
	 */
	protected Set retrieveMatchingFiles(File rootDir, String pattern) throws IOException {
		if (!rootDir.isDirectory()) {
			throw new IllegalArgumentException("'rootDir' parameter [" + rootDir + "] does not denote a directory");
		}
		String fullPattern = StringUtils.replace(rootDir.getAbsolutePath(), File.separator, "/");
		if (!pattern.startsWith("/")) {
			fullPattern += "/";
		}
		fullPattern = fullPattern + StringUtils.replace(pattern, File.separator, "/");
		Set result = CollectionFactory.createLinkedSetIfPossible(8);
		doRetrieveMatchingFiles(fullPattern, rootDir, result);
		return result;
	}

	/**
	 * Recursively retrieve files that match the given pattern,
	 * adding them to the given result list.
	 * @param fullPattern the pattern to match against,
	 * with preprended root directory path
	 * @param dir the current directory
	 * @param result the Set of matching File instances to add to
	 * @throws IOException if directory contents could not be retrieved
	 */
	protected void doRetrieveMatchingFiles(String fullPattern, File dir, Set result) throws IOException {
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
			if (getPathMatcher().match(fullPattern, currPath)) {
				result.add(dirContents[i]);
			}
		}
	}

}
