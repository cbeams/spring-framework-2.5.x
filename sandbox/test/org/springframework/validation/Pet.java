/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.validation;

import java.util.Date;

public class Pet {
    private Name name;
    private int age;
    private Date dateOfBirth;

    public Name getName() {
        return name;
    }

    /**
     * @@org.springframework.rcp.validator.rules.Required()
     */
    public String getNickName() {
        return null;
    }

    /**
     * @@org.springframework.rcp.validator.rules.Required()
     */
    public int getAge() {
        return age;
    }

    /**
     * @param name
     */
    public void setName(Name name) {
        this.name = name;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
}
