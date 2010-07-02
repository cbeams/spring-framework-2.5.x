
package org.springframework.aop.framework.asm;

import java.lang.reflect.Method;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import org.springframework.aop.framework.AdvisedSupport;

/**
 * @author robh
 */
public abstract class AbstractMethodProxyCodeGenerationStrategy extends AbstractCodeGenerationStrategy{

	public void generate(ClassWriter cw, Method method, AdvisedSupport advised, String proxyInternalName, String targetInternalName, String targetDescriptor) {
		String methodDescriptor = Type.getMethodDescriptor(method);
		String[] exceptionTypes = convertToInternalTypes(method.getExceptionTypes());
		String methodName = method.getName();

		MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, methodName, methodDescriptor, null, exceptionTypes);
		generateMethod(mv, method, advised, proxyInternalName, targetInternalName, targetDescriptor);
	}

	protected abstract void generateMethod(MethodVisitor mv, Method method, AdvisedSupport advised, String proxyInternalName, String targetInternalName, String targetDescriptor);
}
