/*
 * $Header: /var/local/springframework.cvs.sourceforge.net/spring/sandbox/src/org/springframework/rules/values/AbstractFormObject.java,v 1.1 2004-07-16 03:10:10 kdonald Exp $
 * $Revision: 1.1 $
 * $Date: 2004-07-16 03:10:10 $
 * 
 * Copyright Computer Science Innovations (CSI), 2004. All rights reserved.
 */
package org.springframework.rules.values;

/**
 * @author Keith Donald
 */
public class AbstractFormObject extends AbstractPropertyChangePublisher {

    private boolean dirty;

    protected void firePropertyChange(String propertyName, boolean oldValue,
            boolean newValue) {
        markDirty();
        super.firePropertyChange(propertyName, oldValue, newValue);
    }

    protected void firePropertyChange(String propertyName, int oldValue,
            int newValue) {
        markDirty();
        super.firePropertyChange(propertyName, oldValue, newValue);
    }

    protected void firePropertyChange(String propertyName, Object oldValue,
            Object newValue) {
        markDirty();
        super.firePropertyChange(propertyName, oldValue, newValue);
    }

    protected void markDirty() {
        markDirty(true);
    }

    private void markDirty(boolean dirty) {
        if (hasChanged(this.dirty, true)) {
            this.dirty = true;
            super.firePropertyChange("dirty", !this.dirty, this.dirty);
        }
    }

    public void clearDirty() {
        markDirty(false);
    }

}