/*
 * $Header: /usr/local/cvs/module/src/java/File.java,v 1.7 2004/01/16 22:23:11
 * keith Exp $ $Revision: 1.1 $ $Date: 2004-09-24 04:44:36 $
 * 
 * Copyright Computer Science Innovations (CSI), 2004. All rights reserved.
 */
package org.springframework.binding.value.support;

import org.springframework.binding.value.IndexAdapter;

public abstract class AbstractIndexAdapter extends AbstractValueModel implements
        IndexAdapter {

    private int index;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        if (hasChanged(this.index, index)) {
            int oldValue = this.index;
            this.index = index;
            firePropertyChange("index", oldValue, this.index);
        }
    }

}