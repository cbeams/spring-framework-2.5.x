
package org.springframework.aop.framework.asm;

import java.lang.reflect.Method;

import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Constants;
import org.objectweb.asm.Type;

import org.springframework.aop.framework.AdvisedSupport;

/**
 * @author robh
 */
public class AdvisedMixinCodeGenerationStrategy extends AbstractMethodProxyCodeGenerationStrategy {

	protected void generateMethod(CodeVisitor cv, Method method, AdvisedSupport advised, String proxyInternalName, String targetInternalName, String targetDescriptor) {
		String methodName = method.getName();
		String methodDescriptor = Type.getMethodDescriptor(method);
		Class returnType = method.getReturnType();

		cv.visitVarInsn(Constants.ALOAD, 0);
		cv.visitFieldInsn(Constants.GETFIELD, proxyInternalName, ADVISED_FIELD_NAME, ADVISED_SUPPORT_DESCRIPTOR);

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

		cv.visitMethodInsn(Constants.INVOKEVIRTUAL, ADVISED_SUPPORT_INTERNAL_NAME, methodName, methodDescriptor);
		cv.visitInsn(getReturnOpcodeForType(returnType));
		cv.visitMaxs(0, 0);
	}
}
