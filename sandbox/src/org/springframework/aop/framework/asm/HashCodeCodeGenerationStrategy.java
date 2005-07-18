package org.springframework.aop.framework.asm;

import org.objectweb.asm.CodeVisitor;
import org.springframework.aop.framework.AdvisedSupport;

import java.lang.reflect.Method;

/**
 * @author robh
 */
public class HashCodeCodeGenerationStrategy extends AbstractMethodProxyCodeGenerationStrategy {
	protected void generateMethod(CodeVisitor cv, Method method, AdvisedSupport advised, String proxyInternalName, String targetInternalName, String targetDescriptor) {
		throw new UnsupportedOperationException();
	}
}
