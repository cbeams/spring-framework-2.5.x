package org.springframework.jmx;

/**
 * @author robh
 */
public class ExceptionOnInitBean {
    private boolean exceptOnInit = false;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setExceptOnInit(boolean exceptOnInit) {
        this.exceptOnInit = exceptOnInit;
    }

    public ExceptionOnInitBean() {
        if (exceptOnInit) {
            throw new RuntimeException("I am being init'd!");
        }

    }


}
