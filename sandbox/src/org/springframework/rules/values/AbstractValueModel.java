/*
 * $Header: /var/local/springframework.cvs.sourceforge.net/spring/sandbox/src/org/springframework/rules/values/AbstractValueModel.java,v 1.1 2004-06-12 07:27:09 kdonald Exp $
 * $Revision: 1.1 $
 * $Date: 2004-06-12 07:27:09 $
 * 
 * Copyright Computer Science Innovations (CSI), 2004. All rights reserved.
 */
package org.springframework.rules.values;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.DefaultObjectStyler;

/**
 * Base helper implementation of a value model.
 * 
 * @author Keith Donald
 */
public abstract class AbstractValueModel implements ValueModel {
    protected Log logger = LogFactory.getLog(ValueModel.class);

    private List listeners = new ArrayList();

    /**
     * @see org.springframework.rules.values.ValueChangeable#addValueListener(org.springframework.rules.values.ValueListener)
     */
    public void addValueListener(ValueListener l) {
        if (logger.isDebugEnabled()) {
            logger.debug("[Adding value listener " + l + "]");
        }
        listeners.add(l);
    }

    /**
     * @see org.springframework.rules.values.ValueChangeable#removeValueListener(org.springframework.rules.values.ValueListener)
     */
    public void removeValueListener(ValueListener l) {
        if (logger.isDebugEnabled()) {
            logger.debug("[Removing value listener " + l + "]");
        }
        listeners.remove(l);
    }

    protected void fireValueChanged() {
        if (logger.isDebugEnabled()) {
            logger.debug("[Firing value changed event; newValue="
                    + DefaultObjectStyler.evaluate(get()) + "]");
        }
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            ((ValueListener)it.next()).valueChanged();
        }
    }

}