package org.springframework.aop.framework.aspectwerkz;

import org.springframework.aop.framework.AbstractAopProxyTests;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AopProxy;

/**
 * @author Rob Harrop
 */
public class AspectWerkzAopProxyTests extends AbstractAopProxyTests{
    protected Object createProxy(AdvisedSupport as) {
        return createAopProxy(as).getProxy();
    }

    protected AopProxy createAopProxy(AdvisedSupport as) {
        return new AspectWerkzAopProxy(as);
    }
}
