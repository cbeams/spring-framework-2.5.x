/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.rules.reporting;

/**
 * @author  Keith Donald
 */
public class Severity {
    public static final Severity WARNING = new Severity(0);
    public static final Severity ERROR = new Severity(1);
    private int value;
    
    private Severity(int magnitude) {
        this.value = magnitude;
    }
    
    public int getValue() {
        return value;
    }
}
