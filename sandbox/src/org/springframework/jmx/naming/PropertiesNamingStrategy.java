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
import org.springframework.jmx.exceptions.ObjectNamingException;

/**
 * @author Rob Harrop
 */
public class PropertiesNamingStrategy implements ObjectNamingStrategy,
        InitializingBean {

    private String propertiesFile = "objectnames.properties";

    private Properties properties;

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

    public ObjectName getObjectName(Object managedResource, String key)
            throws ObjectNamingException {
        
        String objectName = null;
        
        try {
            objectName = properties.getProperty(key);
            return ObjectName.getInstance(objectName);
        } catch(MalformedObjectNameException ex) {
            throw new ObjectNamingException("The name associated with key: " + key + " [" + objectName + "] is malformed.", ex);
        }
    }

    public void setPropertiesFile(String propertiesFile) {
        this.propertiesFile = propertiesFile;
    }

    public String getPropertiesFile() {
        return this.propertiesFile;
    }

}