/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.validation;

import java.util.StringTokenizer;

/**
 * @author keith
 */
public class Name {
    private String firstName;
    private String lastName;

    public Name() {
        
    }
    
    public Name(String name) {
        setName(name);
    }

    public String getFirstName() {
        return firstName;
    }

    
    /**
     * @@org.springframework.rcp.validator.rules.Required()
     */
    public String getLastName() {
        return lastName;
    }
    
    public void setName(String name) {
        StringTokenizer token = new StringTokenizer(name);
        firstName = token.nextToken();
        lastName = token.nextToken();
    }
}
