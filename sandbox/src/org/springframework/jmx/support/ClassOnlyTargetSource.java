package org.springframework.jmx.support;

import org.springframework.aop.TargetSource;

/**
 * @author robh
 */
public class ClassOnlyTargetSource implements TargetSource {
    private final Class clazz;

    public ClassOnlyTargetSource(Class clazz) {
        this.clazz = clazz;
    }

    public Class getTargetClass() {
        return clazz;
    }

    public boolean isStatic() {
        return true;
    }

    public Object getTarget() throws Exception {
        return null;
    }

    public void releaseTarget(Object target) throws Exception {

    }
}
