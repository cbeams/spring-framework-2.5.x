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

package org.springframework.beans.factory.xml.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Default implementation of the {@link NamespaceHandler}. Resolves namespace URIs
 * to implementation classes based on the mappings contained in mapping file.
 * <p/>
 * By default, this implementation looks for the mapping file at
 * <code>META-INF/spring.handlers</code>, but this can be changed using the
 * {@link #DefaultNamespaceHandlerResolver(String)} constructor.
 *
 * @author Rob Harrop
 * @since 1.3
 * @see NamespaceHandler
 * @see org.springframework.beans.factory.xml.DefaultXmlBeanDefinitionParser
 */
public class DefaultNamespaceHandlerResolver implements NamespaceHandlerResolver {

    // TODO: revisit this error handling for this whole class

    /**
     * The default location to look for the internal handler mapping file.
     */
    private static final String DEFAULT_SPRING_HANDLER_MAPPINGS_LOCATION = "META-INF/spring.handlers";

    /**
     * <code>Log</code> instance of this class.
     */
    protected final Log logger = LogFactory.getLog(getClass());

    /**
     * The currently configured mapping file location.
     */
    private String springHandlerMappingsLocation = DEFAULT_SPRING_HANDLER_MAPPINGS_LOCATION;

    /**
     * Stores the mapping of namespace URI -> NamespaceHandler instances.
     */
    private Map handlerMappings;

    /**
     * Create a new <code>DefaultNamespaceHandlerResolver</code> using the
     * default mapping file location.
     * @see #DEFAULT_SPRING_HANDLER_MAPPINGS_LOCATION
     */
    public DefaultNamespaceHandlerResolver() {
        initHandlerMappings();
    }

    /**
     * Create a new <code>DefaultNamespaceHandlerResolver</code> using the
     * supplied mapping file location.
     */
    public DefaultNamespaceHandlerResolver(String springHandlerMappingsLocation) {
        Assert.notNull(springHandlerMappingsLocation, "The [springHandlerMappingsLocation] argument cannot be null.");
        this.springHandlerMappingsLocation = springHandlerMappingsLocation;
        initHandlerMappings();
    }

    /**
     * Loads the namespace URI -> <code>NamespaceHandler</code> class mappings from the configured
     * mapping file. Converts the class names into actual class instances and checks that
     * they implement the <code>NamespaceHandler</code> interface. Pre-instantiates an instance
     * of each <code>NamespaceHandler</code> and maps that instance to the corresponding
     * namespace URI.
     */
    private void initHandlerMappings() {
        Properties mappings = loadSpringMappings();
        this.handlerMappings = new HashMap(mappings.size());
        for (Enumeration en = mappings.propertyNames(); en.hasMoreElements();) {
            String namespaceUri = (String) en.nextElement();
            String className = mappings.getProperty(namespaceUri);

            try {
                Class handlerClass = ClassUtils.forName(className);

                if (!NamespaceHandler.class.isAssignableFrom(handlerClass)) {
                    throw new IllegalArgumentException("Class [" + className +
                            "] does not implements the NamespaceHandler interface.");
                }

                this.handlerMappings.put(namespaceUri, BeanUtils.instantiateClass(handlerClass));
            }
            catch (ClassNotFoundException e) {
                throw new RuntimeException("Unable to locate NamespaceHandler class ["
                        + className + "] for namespace URI [" + namespaceUri + "].", e);
            }
        }
    }

    /**
     * Loads the mapping file into a {@link Properties} instance.
     */
    private Properties loadSpringMappings() {
        Resource resource = new ClassPathResource(this.springHandlerMappingsLocation);
        InputStream is = null;
        try {
            is = resource.getInputStream();
            Properties mappings = new Properties();
            mappings.load(is);
            return mappings;
        }
        catch (IOException e) {
            throw new RuntimeException("Unable to load Spring mappings file [" +
                    this.springHandlerMappingsLocation + "].", e);
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                }
                catch (IOException ex) {
                    logger.warn("Unable to close InputStream for resource [" + resource + "].");
                }
            }
        }
    }

    /**
     * Locates the {@link NamespaceHandler} for the supplied namespace URI
     * from the configured mappings.
     */
    public NamespaceHandler resolve(String namespaceUri) {
        NamespaceHandler namespaceHandler = (NamespaceHandler) this.handlerMappings.get(namespaceUri);

        if (namespaceUri == null) {
            throw new IllegalArgumentException("Unable to locate NamespaceHandler for namespace URI [" + namespaceUri + "].");
        }

        return namespaceHandler;
    }
}
