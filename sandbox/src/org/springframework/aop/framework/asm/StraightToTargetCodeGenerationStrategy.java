
package org.springframework.aop.framework.asm;

import java.lang.reflect.Method;

import org.objectweb.asm.Type;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import org.springframework.aop.framework.AdvisedSupport;

/**
 * @author robh
 */
public class StraightToTargetCodeGenerationStrategy extends AbstractMethodProxyCodeGenerationStrategy {

	protected void generateMethod(MethodVisitor cv, Method method, AdvisedSupport advised, String proxyInternalName, String targetInternalName, String targetDescriptor) {
		String descriptor = Type.getMethodDescriptor(method);
		String methodName = method.getName();

		// load this
		cv.visitVarInsn(Opcodes.ALOAD, 0);

		if (advised.getTargetSource().isStatic()) {
			cv.visitFieldInsn(Opcodes.GETFIELD, proxyInternalName, TARGET_FIELD_NAME, targetDescriptor);
		}

		// load args
		Class[] parameterTypes = method.getParameterTypes();
		int stackCount = 1;
		int argCount = parameterTypes.length;
		if (argCount > 0) {
			for (int x = 0; x < argCount; x++) {
				Class parameterType = parameterTypes[x];
				int frameSpaceSize = getLocalsSizeForType(parameterType);
				cv.visitVarInsn(getLoadOpcodeForType(parameterTypes[x]), stackCount);
				stackCount += frameSpaceSize;
			}
		}

		// invoke super
		cv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, targetInternalName, methodName, descriptor);

		// return
		cv.visitInsn(getReturnOpcodeForType(method.getReturnType()));
		cv.visitMaxs(0, 0);
	}
}
