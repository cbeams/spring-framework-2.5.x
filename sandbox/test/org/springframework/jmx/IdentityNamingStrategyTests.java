/*
 * Created on Jul 14, 2004
 */
package org.springframework.jmx;

import javax.management.ObjectInstance;

import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.jmx.naming.ObjectNamingStrategy;
import org.springframework.jmx.naming.IdentityNamingStrategy;

/**
 * @author robh
 */
public class IdentityNamingStrategyTests extends AbstractNamingStrategyTests {

    private JmxTestBean bean = new JmxTestBean();

    protected ObjectNamingStrategy getStrategy() throws Exception {
        return new IdentityNamingStrategy();
    }

    protected Object getManagedResource() throws Exception {
        return bean;
    }

    protected String getKey() {
        return "identity";
    }

    protected String getCorrectObjectName() {
        StringBuffer sb = new StringBuffer(256);

        sb.append(bean.getClass().getPackage().getName());
        sb.append(":");
        sb.append("class=");
        sb.append(ClassUtils.getShortName(bean.getClass()));
        sb.append(",hashCode=");
        sb.append(ObjectUtils.getIdentityHexString(bean));

        return sb.toString();
    }
}