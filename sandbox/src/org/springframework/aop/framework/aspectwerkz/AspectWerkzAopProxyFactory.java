package org.springframework.aop.framework.aspectwerkz;

import org.springframework.aop.framework.AopProxyFactory;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AopConfigException;

/**
 * @author robh
 */
public class AspectWerkzAopProxyFactory implements AopProxyFactory{

    public AopProxy createAopProxy(AdvisedSupport advisedSupport) throws AopConfigException {
        return new AspectWerkzAopProxy(advisedSupport);
    }
}
