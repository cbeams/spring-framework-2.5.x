/*
 * Created on Jul 5, 2004
 */
package org.springframework.jmx;

/**
 * @@org.springframework.jmx.metadata.support.ManagedResource(description="My Managed
 *                                                                               Bean",
 *                                                                               objectName="spring:bean=test")
 * @author robh
 */
public class JmxTestBean {

    private String name;

    private int age;

    /**
     * @@org.springframework.jmx.metadata.support.ManagedAttribute(description="The Age Attribute")
     */
    public int getAge() {
        return age;
    }

    /**
     * 
     * @param age
     *            The age to set.
     */
    public void setAge(int age) {
        this.age = age;
    }

    /**
     * @@org.springframework.jmx.metadata.support.ManagedOperation()
     */
    public long myOperation() {
        return 1L;
    }

    /**
     * @@org.springframework.jmx.metadata.support.ManagedAttribute(description="The Name Attribute")
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @@org.springframework.jmx.metadata.support.ManagedAttribute()
     */
    public String getName() {
        return name;
    }

    /**
     * @@org.springframework.jmx.metadata.support.ManagedOperation(description="Add Two Numbers Together")
     */
    public int add(int x, int y) {
        return x + y;
    }
    
    /**
     * Test method that is not exposed by the MetadataAssembler
     *
     */
    public void dontExposeMe() {
        throw new RuntimeException();
    }
}