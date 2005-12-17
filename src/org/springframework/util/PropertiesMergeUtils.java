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

package org.springframework.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

/**
 * @author Rob Harrop
 */
public abstract class PropertiesMergeUtils {

	public static Properties findMergedProperties(String propertiesLocation, ClassLoader classLoader) throws IOException {
		Properties properties = new Properties();

		Enumeration urls = classLoader.getResources(propertiesLocation);
		while (urls.hasMoreElements()) {
			URL url = (URL) urls.nextElement();
			InputStream inputStream = null;
			try {
				inputStream = url.openStream();
				properties.load(inputStream);
			}
			finally {
				if (inputStream != null) {
					inputStream.close();
				}
			}
		}
		return properties;
	}

}
