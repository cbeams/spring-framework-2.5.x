/*
 * Created on Jul 23, 2004
 */
package org.springframework.jmx.metadata.support;

/**
 * @author robh
 */
public class AbstractJmxAttribute {

    protected String description;
    private int currencyTimeLimit;
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setCurrencyTimeLimit(int currencyTimeLimit) {
        this.currencyTimeLimit = currencyTimeLimit;
    }

    public String getDescription() {
        return this.description;
    }
    
    public int getCurrencyTimeLimit() {
        return currencyTimeLimit;
    }
}
