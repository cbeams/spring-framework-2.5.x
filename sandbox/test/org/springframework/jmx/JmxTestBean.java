/*
 * Created on Jul 5, 2004
 */
package org.springframework.jmx;

/**
 * @@org.springframework.jmx.metadata.support.ManagedResource(description="My Managed
 *                                                                               Bean",
 *                                                                               objectName="spring:bean=test", 
 *                                                                               log=true, 
 *                                                                               logFile="jmx.log",
 *                                                                               currencyTimeLimit=15)
 * @author robh
 */
public class JmxTestBean {

    private String name;
    
    private String nickName;

    private int age;

    private boolean isSuperman;

    /**
     * @@org.springframework.jmx.metadata.support.ManagedAttribute(description="The Age Attribute", currencyTimeLimit=15)
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
     * @@org.springframework.jmx.metadata.support.ManagedOperation(currencyTimeLimit=30)
     */
    public long myOperation() {
        return 1L;
    }

    /**
     * @@org.springframework.jmx.metadata.support.ManagedAttribute(description="The Name Attribute", 
     *                      currencyTimeLimit=20,
     *                      defaultValue="bar")
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @@org.springframework.jmx.metadata.support.ManagedAttribute(defaultValue="foo")
     */
    public String getName() {
        return name;
    }
    
    /**
     * @@org.springframework.jmx.metadata.support.ManagedAttribute(description="The Nick Name Attribute")
     */
    public String getNickName() {
        return this.nickName;
    }
    
    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
    
    /**
     * @@org.springframework.jmx.metadata.support.ManagedAttribute(description="The Is Superman Attribute")
     */
    public void setSuperman(boolean superman) {
        this.isSuperman = superman;
    }
    
    public boolean isSuperman() {
        return isSuperman;
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