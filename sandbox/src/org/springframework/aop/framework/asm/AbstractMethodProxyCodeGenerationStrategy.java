
package org.springframework.aop.framework.asm;

import java.lang.reflect.Method;

import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.Constants;

import org.springframework.aop.framework.AdvisedSupport;

/**
 * @author robh
 */
public abstract class AbstractMethodProxyCodeGenerationStrategy extends AbstractCodeGenerationStrategy{

	public void generate(ClassWriter cw, Method method, AdvisedSupport advised, String proxyInternalName, String targetInternalName, String targetDescriptor) {
		String methodDescriptor = Type.getMethodDescriptor(method);
		String[] exceptionTypes = convertToInternalTypes(method.getExceptionTypes());
		String methodName = method.getName();

		CodeVisitor cv = cw.visitMethod(Constants.ACC_PUBLIC, methodName, methodDescriptor, exceptionTypes, null);
		generateMethod(cv, method, advised, proxyInternalName, targetInternalName, targetDescriptor);
	}

	protected abstract void generateMethod(CodeVisitor cv, Method method, AdvisedSupport advised, String proxyInternalName, String targetInternalName, String targetDescriptor);
}
