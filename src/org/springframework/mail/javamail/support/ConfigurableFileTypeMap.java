/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.mail.javamail.support;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.FileTypeMap;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Spring-configurable <code>FileTypeMap</code> implementation that will read
 * MIME type to file extension mappings from a standard JavaMail MIME type
 * mapping file.
 *
 * <p>The mapping file should be in the following format:
 *
 * <pre>
 * # map text/html to .htm and .html files
 * text/html  htm html</pre>
 *
 * Lines starting with <code>#</code> are treated as comments and are ignored. All
 * other lines are treated as mappings. Each mapping line should contain the MIME
 * type as the first entry and then each file extension to map to that MIME type
 * as subsequent entries. Each entry is separated by spaces or tabs.
 *
 * <p>By default, the mappings in the mime.types file located in the same package
 * as this class are used. This can be overridden using the
 * <code>mappingLocation</code> property.
 *
 * <p>Additional mappings can be added via the <code>mappings</code> property with
 * each key of an entry being a file extension and the value of the entry
 * being the MIME type mapped to that file extension.
 *
 * <p><b>NOTE:</b> This class will only work on JDK 1.4+, due to the use of the
 * <code>java.util.regex</code> package.
 *
 * @author Rob Harrop
 * @since 1.2
 * @see #setMappingLocation
 * @see #setMappings
 */
public class ConfigurableFileTypeMap extends FileTypeMap implements InitializingBean {

	/**
	 * The default MIME to use when no mapping can be found.
	 */
	public static final String DEFAULT_MIME_TYPE = "application/octet-stream";

	/**
	 * <code>Pattern</code> for matching an entry part in the mapping file.
	 */
	private static final Pattern PATTERN = Pattern.compile("([\\S]+)");


	/**
	 * The <code>Resource</code> to load the mapping file from.
	 */
	private Resource mappingLocation = new ClassPathResource("mime.types", getClass());

	/**
	 * The final mappings merged from those in the mapping file and the entries in
	 * <code>mappings</code>.
	 */
	private Properties mergedMappings;

	/**
	 * Used to configure additional mappings.
	 */
	private Properties mappings;


	/**
	 * Sets the <code>Resource</code> from which mappings are loaded.
	 */
	public void setMappingLocation(Resource mappingLocation) {
		this.mappingLocation = mappingLocation;
	}

	/**
	 * Sets any additional file extension to MIME type mappings. The key of
	 * an entry should be the file extension and the value the MIME type.
	 */
	public void setMappings(Properties mappings) {
		this.mappings = mappings;
	}

	/**
	 * Creates the final merged mapping set.
	 */
	public void afterPropertiesSet() throws IOException {
		this.mergedMappings = parseMappingFile(this.mappingLocation);
		if (this.mappings != null) {
			this.mergedMappings.putAll(this.mappings);
		}
	}


	/**
	 * Get the content type of the supplied <code>File</code> based on the extension.
	 */
	public String getContentType(File file) {
		return getContentType(file.getName());
	}

	/**
	 * Get the content type of the file pointed to by the supplied <code>String</code>.
	 */
	public String getContentType(String fileName) {
		String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
		String mimeType = this.mergedMappings.getProperty(extension);
		return (mimeType != null ? mimeType : DEFAULT_MIME_TYPE);
	}

	/**
	 * Parses the supplied mapping data and puts the mappings into a <code>Properties</code>
	 * instance. Keys in the map correspond to file extensions and values to MIME types.
	 * @param mappingResource the <code>Resource</code> to load the mapping data from
	 * @return the parsed MIME type mappings as <code>Properties</code> instance
	 */
	private Properties parseMappingFile(Resource mappingResource) throws IOException {
		InputStream is = mappingResource.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		try {
			Properties mimeTypes = new Properties();
			String line = null;
			while ((line = reader.readLine()) != null) {
				// Skip empty lines and comments.
				if (line.length() > 0 && line.charAt(0) == '#') {
					continue;
				}
				Matcher matcher = PATTERN.matcher(line);
				// Read the first match which is the content type.
				if (matcher.find()) {
					String mimeType = matcher.group();
					// Read each subsequent match which is a file extension.
					while (matcher.find()) {
						mimeTypes.put(matcher.group(), mimeType);
					}
				}
			}
			return mimeTypes;
		}
		finally {
			reader.close();
		}
	}

}
