
package org.springframework.samples.petclinic.jmx;

/**
 * 
 * @author robh
 */
public interface CallMonitor {

    int getCallCount();
    long getCallTime();

    boolean isEnabled();
    void setEnabled(boolean isEnabled);
}

