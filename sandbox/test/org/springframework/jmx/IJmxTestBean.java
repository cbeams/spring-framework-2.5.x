/*
 * Created on Jul 22, 2004
 */
package org.springframework.jmx;

/**
 * @author robh
 */
public interface IJmxTestBean {

    public int add(int x, int y);
    public long myOperation();
    
    public int getAge();
    public void setAge(int age);
    
    public void setName(String name);
    public String getName();

    // used to test invalid methods that exist in the proxy interface
    public void dontExposeMe();
}
