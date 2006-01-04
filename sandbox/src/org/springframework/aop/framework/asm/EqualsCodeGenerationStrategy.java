package org.springframework.aop.framework.asm;

import org.objectweb.asm.MethodVisitor;
import org.springframework.aop.framework.AdvisedSupport;

import java.lang.reflect.Method;

/**
 * @author robh
 */
public class EqualsCodeGenerationStrategy extends AbstractMethodProxyCodeGenerationStrategy {

	protected void generateMethod(MethodVisitor cv, Method method, AdvisedSupport advised, String proxyInternalName, String targetInternalName, String targetDescriptor) {
		throw new UnsupportedOperationException();
	}
}
