package org.springframework.jmx;

/**
 * @author robh
 */
public interface ICustomJmxBean {
    public int add(int x, int y);
    public long myOperation();

    public String getName();
    public void setName(String name);

    public int getAge();
}
