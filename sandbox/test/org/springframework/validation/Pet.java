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
    private String favoriteToy;

    public Name getName() {
        return name;
    }

    /**
     * @@Required()
     */
    public String getNickName() {
        return null;
    }

    /**
     * @@Required()
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

    /**
     * @@org.springframework.validation.rules.Required()
     * @@org.springframework.validation.rules.MaxLength(26)
     */
    public String getFavoriteToy() {
        return favoriteToy;
    }
    
    public void setFavoriteToy(String favoriteToy) {
        this.favoriteToy = favoriteToy;
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
