package org.springframework.aop.framework.autoproxy.metadata;

import org.springframework.beans.TestBean;

/**
 * Extension of TestBean class to add a modifiable class attribute.
 * 
 * <p>The attribute syntax is that of Commons Attributes.
 * Attribute is in the same package, so we don't need FQN.
 *
 * @@ModifiableAttribute()
 *
 * @author Rod Johnson
 * @version $Id: ModifiableTestBean.java,v 1.3 2004-02-23 10:44:06 jhoeller Exp $
 */
public class ModifiableTestBean extends TestBean {

}
