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
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.FileTypeMap;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * <p>Spring-configurable <code>FileTypeMap</code> implementation that will read
 * MIME type to file extension mappings from a standard JavaMail MIME type
 * mapping file.</p>
 * <p>The mapping file should be in the following format:
 * <pre>
 * # map text/html to .htm and .html files
 * text/html  htm html
 * </pre>
 * Lines starting with <code>#</code> are treated as comments and are ignored. All
 * other lines are treated as mappings. Each mapping line should contain the MIME
 * type as the first entry and then each file extension to map to that MIME type
 * as subsequent entries. Each entry is separated by spaces or tabs.
 * </p>
 * <p>
 * By default, the mappings in the mime.types file located in the same package
 * as this class are used. This can be overriden using the
 * <code>mappingLocation</code> property.
 * </p>
 * <p>
 * Additional mappings can be added via the <code>mappings</code> property with
 * each key of an entry being a file extension and the value of the entry
 * being the MIME type mapped to that file extension.
 * </p>
 * @see #setMappingLocation(org.springframework.core.io.Resource)
 * @see #setMappings(java.util.Properties)
 * @author Rob Harrop
 */
public class ConfigurableFileTypeMap extends FileTypeMap implements InitializingBean {

	/**
	 * The default MIME to use when no mapping can be found.
	 */
	private static final String DEFAULT_MIME_TYPE = "application/octet-stream";

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
	private Map mergedMappings;

	/**
	 * Used to configure additional mappings.
	 */
	private Properties mappings;


	/**
	 * Gets the content type of the supplied <code>File</code> based on the extension.
	 */
	public String getContentType(File file) {
		return getContentType(file.getName());
	}

	/**
	 * Gets the content type of the file pointed to by the supplied <code>String</code>.
	 */
	public String getContentType(String fileName) {
		String extension = fileName.substring(fileName.lastIndexOf(".") + 1);

		String mimeType = (String) mergedMappings.get(extension);

		if (mimeType == null) {
			mimeType = DEFAULT_MIME_TYPE;
		}

		return mimeType;
	}

	/**
	 * Creates the final merged mapping set.
	 */
	public void afterPropertiesSet() throws Exception {
		mergedMappings = parseMappingFile(mappingLocation);

		if (mappings != null) {
			mergedMappings.putAll(mappings);
		}
	}

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
	 * Parses the supplied mapping data and puts the mappings into a <code>Map</code>
	 * instance. Keys in the map correspond to file extensions and values to MIME
	 * types.
	 * @param mappingResource the <code>Resource</code> to load the mapping data from.
	 */
	private Map parseMappingFile(Resource mappingResource) throws IOException {
		BufferedReader rdr = null;

		try {
			rdr = new BufferedReader(new InputStreamReader(mappingResource.getInputStream()));

			Map mimeTypes = new HashMap();
			String line = null;

			while ((line = rdr.readLine()) != null) {

				// skip empty lines and comments
				if (line.length() > 0 && line.charAt(0) == '#') {
					continue;
				}

				Matcher m = PATTERN.matcher(line);

				// read the first match which is the
				// content type
				if (m.find()) {
					String mimeType = m.group();

					// read each subsequent match
					// which is a file extension
					while (m.find()) {
						mimeTypes.put(m.group(), mimeType);
					}
				}
			}

			return mimeTypes;
		}
		finally {
			if (rdr != null) {
				rdr.close();
			}
		}
	}

}
