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

package org.springframework.context.support;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import org.xml.sax.InputSource;

import org.springframework.beans.factory.xml.BeansDtdResolver;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

/**
 * EntityResolver implementation that tries to resolve entity references
 * relative to the resource base of the application context, if applicable.
 * Extends BeansDtdResolver to also provide DTD lookup in the class path.
 *
 * <p>Allows to use standard XML entities to include XML snippets into an
 * application context definition, for example to split a large XML file
 * into various modules. The include paths can be relative to the
 * application context's resource base as usual, instead of relative
 * to the JVM working directory (the XML parser's default).
 *
 * <p>Note: In addition to relative paths, every URL that specifies a
 * file in the current system root, i.e. the JVM working directory,
 * will be interpreted relative to the application context too.
 *
 * @author Juergen Hoeller
 * @since 31.07.2003
 * @see org.springframework.context.ApplicationContext#getResource
 */
public class ResourceEntityResolver extends BeansDtdResolver {

	private final ApplicationContext applicationContext;

	public ResourceEntityResolver(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public InputSource resolveEntity(String publicId, String systemId) throws IOException {
		InputSource source = super.resolveEntity(publicId, systemId);
		if (source == null && systemId != null) {
			String resourcePath = null;
			try {
				String decodedSystemId = URLDecoder.decode(systemId);
				String givenUrl = new URL(decodedSystemId).toString();
				String systemRootUrl = new File("").toURL().toString();
				// try relative to resource base if currently in system root
				if (givenUrl.startsWith(systemRootUrl)) {
					resourcePath = givenUrl.substring(systemRootUrl.length());
				}
			}
			catch (MalformedURLException ex) {
				// no URL -> try relative to resource base
				resourcePath = systemId;
			}
			if (resourcePath != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Trying to locate entity [" + systemId + "] as application context resource [" +
											 resourcePath + "]");
				}
				Resource resource = this.applicationContext.getResource(resourcePath);
				if (logger.isInfoEnabled()) {
					logger.info("Found entity [" + systemId + "] as application context resource [" + resourcePath + "]");
				}
				source = new InputSource(resource.getInputStream());
				source.setPublicId(publicId);
				source.setSystemId(systemId);
			}
		}
		return source;
	}

}
