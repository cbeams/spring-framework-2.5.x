/*
 * $Header: /var/local/springframework.cvs.sourceforge.net/spring/sandbox/src/org/springframework/rules/values/AspectAdapter.java,v 1.2 2004-06-12 16:32:18 kdonald Exp $
 * $Revision: 1.2 $
 * $Date: 2004-06-12 16:32:18 $
 * 
 * Copyright Computer Science Innovations (CSI), 2004. All rights reserved.
 */
package org.springframework.rules.values;

/**
 * Adapts access to a domain model aspect (generally a property) to the value
 * model interface.  The aspect access strategy is pluggable.
 * 
 * @author Keith Donald
 */
public class AspectAdapter extends AbstractValueModel {
    private String aspect;

    private MutableAspectAccessStrategy aspectAccessStrategy;

    public AspectAdapter(MutableAspectAccessStrategy aspectAccessStrategy,
            String aspect) {
        this(null, aspectAccessStrategy, aspect);
    }

    public AspectAdapter(final ValueModel domainObjectHolder,
            MutableAspectAccessStrategy aspectAccessStrategy, String aspect) {
        if (domainObjectHolder != null) {
            if (logger.isDebugEnabled()) {
                logger
                        .debug("[Aspect Adapter attaching to mutable domain object holder.]");
            }
            domainObjectHolder.addValueListener(new ValueListener() {
                public void valueChanged() {
                    if (logger.isDebugEnabled()) {
                        logger
                                .debug("[Notifying any dependents value may have changed; target object changed]");
                    }
                    AspectAdapter.this.fireValueChanged();
                }
            });
        }
        this.aspectAccessStrategy = aspectAccessStrategy;
        this.aspect = aspect;
    }

    public void addValueListener(ValueListener l) {
        super.addValueListener(l);
        aspectAccessStrategy.addValueListener(l, aspect);
    }

    public void removeValueListener(ValueListener l) {
        super.removeValueListener(l);
        aspectAccessStrategy.removeValueListener(l, aspect);
    }

    public Object get() {
        return aspectAccessStrategy.getValue(aspect);
    }

    public void set(Object value) {
        aspectAccessStrategy.setValue(aspect, value);
    }
}