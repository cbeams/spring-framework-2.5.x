package org.springframework.context.support;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Person {
    private Long id;
    private String firstname;
    private String secondname;

    @Id
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getSecondname() {
        return secondname;
    }

    public void setSecondname(String secondname) {
        this.secondname = secondname;
    }
}
