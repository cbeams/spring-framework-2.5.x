
package org.springframework.aop.framework.asm;

import java.lang.reflect.Method;

import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Constants;
import org.objectweb.asm.Type;

import org.springframework.aop.framework.AdvisedSupport;

/**
 * @author robh
 */
public class NonStaticTargetSourceCodeGenerationStrategy extends AbstractMethodProxyCodeGenerationStrategy {

	protected void generateMethod(CodeVisitor cv, Method method, AdvisedSupport advised, String proxyInternalName, String targetInternalName, String targetDescriptor) {
		String descriptor = Type.getMethodDescriptor(method);
		String methodName = method.getName();

		// load this
		cv.visitVarInsn(Constants.ALOAD, 0);

		cv.visitFieldInsn(Constants.GETFIELD, proxyInternalName, ADVISED_FIELD_NAME, ADVISED_SUPPORT_DESCRIPTOR);
		cv.visitMethodInsn(Constants.INVOKEVIRTUAL, ADVISED_SUPPORT_INTERNAL_NAME, GET_TARGET_SOURCE_METHOD, GET_TARGET_SOURCE_DESCRIPTOR);
		cv.visitMethodInsn(Constants.INVOKEINTERFACE, TARGET_SOURCE_INTERNAL_NAME, GET_TARGET_METHOD, GET_TARGET_DESCRIPTOR);
		cv.visitTypeInsn(Constants.CHECKCAST, targetInternalName);

		// load args
		Class[] parameterTypes = method.getParameterTypes();
		int stackCount = 1;
		int argCount = parameterTypes.length;
		if (argCount > 0) {
			for (int x = 0; x < argCount; x++) {
				Class parameterType = parameterTypes[x];
				int frameSpaceSize = getFrameSpaceSize(parameterType);
				cv.visitVarInsn(getLoadOpcodeForType(parameterTypes[x]), stackCount);
				stackCount += frameSpaceSize;
			}
		}

		// invoke super
		cv.visitMethodInsn(Constants.INVOKEVIRTUAL, targetInternalName, methodName, descriptor);

		// return
		cv.visitInsn(getReturnOpcodeForType(method.getReturnType()));
		cv.visitMaxs(0, 0);
	}
}
