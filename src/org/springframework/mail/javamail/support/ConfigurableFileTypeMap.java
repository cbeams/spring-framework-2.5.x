
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
 * @author Rob Harrop
 */
public class ConfigurableFileTypeMap extends FileTypeMap implements InitializingBean {

	/**
	 *
	 */
	private static final String DEFAULT_MIME_TYPE = "application/octet-stream";

	/**
	 * <code>Pattern</code> for matching an entry part in the mapping file.
	 */
	private static final Pattern PATTERN = Pattern.compile("([\\S]+)");

	private Resource mappingLocation = new ClassPathResource("mime.types", getClass());

	private Map mergedMappings;

	private Properties mappings;


	public String getContentType(File file) {
		return getContentType(file.getName());
	}

	public String getContentType(String fileName) {
		String extension = fileName.substring(fileName.lastIndexOf(".") + 1);

		String mimeType = (String) mergedMappings.get(extension);

		if(mimeType == null) {
			mimeType = DEFAULT_MIME_TYPE;
		}

		return mimeType;
	}

	public void afterPropertiesSet() throws Exception {
		mergedMappings = parseMimeTypeFile(mappingLocation);

		if (mappings != null) {
			mergedMappings.putAll(mappings);
		}
	}

	public void setMappingLocation(Resource mappingLocation) {
		this.mappingLocation = mappingLocation;
	}

	public void setMappings(Properties mappings) {
		this.mappings = mappings;
	}

	private Map parseMimeTypeFile(Resource mappingResource) throws IOException {
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
			if(rdr != null) {
				rdr.close();
			}
		}
	}

}
