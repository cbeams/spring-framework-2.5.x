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
package org.springframework.jmx.naming;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.ObjectNameManager;
import org.springframework.jmx.exceptions.ObjectNamingException;

/**
 * <code>ObjectNamingStrategy</code> implementation that reads
 * object names from a properties file. The key used to look up
 * an <code>ObjectName</code> for a bean if the key used to in the <code>Map</code> of beans
 * passed to <code>JmxMBeanAdapter</code>.
 * @author Rob Harrop
 */
public class PropertiesNamingStrategy implements ObjectNamingStrategy,
        InitializingBean {

    /**
     * The default properties file name.
     */
    private String propertiesFile = "objectnames.properties";

    /**
     * Store the properties
     */
    private Properties properties;

    /**
     * Loads the properties from the file
     * after the all properties on the bean have
     * been set.
     */
    public void afterPropertiesSet() throws Exception {
        properties = new Properties();
        try {
            properties.load(new FileInputStream(propertiesFile));
        } catch (FileNotFoundException ex) {
            throw new ObjectNamingException(
                    "Unable to locate the properties file containing the name map. Path supplied: "
                            + propertiesFile, ex);
        }
    }

    /**
     * Attempts to retreive the ObjectName from the 
     * properties file.
     */
    public ObjectName getObjectName(Object managedResource, String key)
            throws ObjectNamingException {
        
        String objectName = null;
        
        try {
            objectName = properties.getProperty(key);
            return ObjectNameManager.getInstance(objectName);
        } catch(MalformedObjectNameException ex) {
            throw new ObjectNamingException("The name associated with key: " + key + " [" + objectName + "] is malformed.", ex);
        }
    }

    /**
     * Specify the path to the properties file.
     * @param propertiesFile
     */
    public void setPropertiesFile(String propertiesFile) {
        this.propertiesFile = propertiesFile;
    }

    /**
     * Retrieve the path to the properties file.
     * @return
     */
    public String getPropertiesFile() {
        return this.propertiesFile;
    }

}