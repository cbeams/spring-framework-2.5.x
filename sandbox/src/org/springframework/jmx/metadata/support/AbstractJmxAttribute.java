/*
 * Created on Jul 23, 2004
 */
package org.springframework.jmx.metadata.support;

/**
 * @author robh
 */
public class AbstractJmxAttribute {

    protected String description;
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return this.description;
    }
}
