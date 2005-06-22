
package org.springframework.aop.framework.asm;

import java.lang.reflect.Method;

import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Constants;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

import org.springframework.aop.framework.AdvisedSupport;

/**
 * @author robh
 */
public class NonStaticTargetSourceCodeGenerationStrategy extends AbstractMethodProxyCodeGenerationStrategy {

	protected void generateMethod(CodeVisitor cv, Method method, AdvisedSupport advised, String proxyInternalName, String targetInternalName, String targetDescriptor) {
		Label openTry = new Label();
		Label closeTry = new Label();
		Label jumpMarker = new Label();
		Label catchException = new Label();
		Label catchRemaining = new Label();
		Label finallyMarker = new Label();

		String descriptor = Type.getMethodDescriptor(method);
		String methodName = method.getName();

		// calculate the initial offset of the locals for the method params
		int localsOffset = calculateInitialLocalsOffset(method.getParameterTypes());

		int localsTarget = localsOffset + 1;
		int localsReturnValue = localsOffset + 2;
		int localsJumpAddress = localsOffset = 3;

		// do code outside try block
		cv.visitInsn(Constants.ACONST_NULL);
		cv.visitVarInsn(Constants.ASTORE, localsTarget);

		// do code in try block
		cv.visitLabel(openTry);

		cv.visitVarInsn(Constants.ALOAD, 0);

		cv.visitFieldInsn(Constants.GETFIELD, proxyInternalName, ADVISED_FIELD_NAME, ADVISED_SUPPORT_DESCRIPTOR);
		cv.visitMethodInsn(Constants.INVOKEVIRTUAL, ADVISED_SUPPORT_INTERNAL_NAME, GET_TARGET_SOURCE_METHOD, GET_TARGET_SOURCE_DESCRIPTOR);
		cv.visitMethodInsn(Constants.INVOKEINTERFACE, TARGET_SOURCE_INTERNAL_NAME, GET_TARGET_METHOD, GET_TARGET_DESCRIPTOR);
		cv.visitTypeInsn(Constants.CHECKCAST, targetInternalName);
		cv.visitVarInsn(Constants.ASTORE, localsTarget);
		cv.visitVarInsn(Constants.ALOAD, localsTarget);

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
		cv.visitMethodInsn(Constants.INVOKEVIRTUAL, targetInternalName, methodName, descriptor);

		// store return value if needed
		if (method.getReturnType() != void.class) {
			cv.visitVarInsn(getStoreOpcodeForType(method.getReturnType()), localsReturnValue);
		}

		// jump to finally
		cv.visitJumpInsn(Constants.GOTO, jumpMarker);

		// close the try block
		cv.visitLabel(closeTry);

		// do the catch block
		cv.visitLabel(catchException);
		cv.visitVarInsn(Constants.ASTORE, localsReturnValue);
		cv.visitJumpInsn(Constants.JSR, finallyMarker);
		cv.visitVarInsn(Constants.ALOAD, localsReturnValue);
		cv.visitInsn(Constants.ATHROW); // should probably wrap me

		// catch any uncaughts - goto finally - throw
		cv.visitLabel(catchRemaining);
		cv.visitVarInsn(Constants.ASTORE, localsReturnValue);
		cv.visitJumpInsn(Constants.JSR, finallyMarker);
		cv.visitVarInsn(Constants.ALOAD, localsReturnValue);
		cv.visitInsn(Constants.ATHROW); // should probably wrap me

		// finally block
		cv.visitLabel(finallyMarker);
		cv.visitVarInsn(Constants.ASTORE, localsJumpAddress);
		cv.visitFieldInsn(Constants.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
		cv.visitLdcInsn("Finally");
		cv.visitMethodInsn(Constants.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
		cv.visitVarInsn(Constants.RET, localsJumpAddress);


		// load this
		cv.visitLabel(jumpMarker);
		cv.visitJumpInsn(Constants.JSR, finallyMarker);

		// return
    if(method.getReturnType() != void.class) {
			cv.visitVarInsn(getLoadOpcodeForType(method.getReturnType()), localsReturnValue);
		}

		cv.visitInsn(getReturnOpcodeForType(method.getReturnType()));

		cv.visitTryCatchBlock(openTry, closeTry, catchException, Type.getInternalName(Exception.class));
		cv.visitTryCatchBlock(openTry, catchRemaining, catchRemaining, null);

		cv.visitMaxs(0, 0);
	}
}
