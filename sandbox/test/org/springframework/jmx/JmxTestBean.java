/*
 * Created on Jul 5, 2004
 */
package org.springframework.jmx;

/**
 * @@org.springframework.jmx.assemblers.metadata.ManagedResource(description="My Managed
 *                                                                               Bean",
 *                                                                               objectName="spring:bean=test")
 * @author robh
 */
public class JmxTestBean {

    private String name;

    private int age;

    /**
     * @@org.springframework.jmx.assemblers.metadata.ManagedAttribute()
     */
    public int getAge() {
        return age;
    }

    /**
     * @param age
     *            The age to set.
     */
    public void setAge(int age) {
        this.age = age;
    }

    /**
     * @@org.springframework.jmx.assemblers.metadata.ManagedOperation()
     */
    public long myOperation() {
        return 1L;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @@org.springframework.jmx.assemblers.metadata.ManagedAttribute()
     */
    public String getName() {
        return name;
    }

    /**
     * @@org.springframework.jmx.assemblers.metadata.ManagedOperation(description="Add Two Numbers Together")
     */
    public int add(int x, int y) {
        return x + y;
    }
}