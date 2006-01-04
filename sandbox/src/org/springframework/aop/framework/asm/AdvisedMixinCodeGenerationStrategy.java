
package org.springframework.aop.framework.asm;

import java.lang.reflect.Method;

import org.objectweb.asm.Type;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import org.springframework.aop.framework.AdvisedSupport;

/**
 * @author robh
 */
public class AdvisedMixinCodeGenerationStrategy extends AbstractMethodProxyCodeGenerationStrategy {

	protected void generateMethod(MethodVisitor cv, Method method, AdvisedSupport advised, String proxyInternalName, String targetInternalName, String targetDescriptor) {
		String methodName = method.getName();
		String methodDescriptor = Type.getMethodDescriptor(method);
		Class returnType = method.getReturnType();

		cv.visitVarInsn(Opcodes.ALOAD, 0);
		cv.visitFieldInsn(Opcodes.GETFIELD, proxyInternalName, ADVISED_FIELD_NAME, ADVISED_SUPPORT_DESCRIPTOR);

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

		cv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, ADVISED_SUPPORT_INTERNAL_NAME, methodName, methodDescriptor);
		cv.visitInsn(getReturnOpcodeForType(returnType));
		cv.visitMaxs(0, 0);
	}
}
