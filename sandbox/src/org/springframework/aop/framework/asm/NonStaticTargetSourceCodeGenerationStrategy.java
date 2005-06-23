
package org.springframework.aop.framework.asm;

import java.lang.reflect.Method;

import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Constants;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

import org.springframework.aop.framework.AdvisedSupport;

import freemarker.template.utility.UndeclaredThrowableException;

/**
 * @author robh
 */
public class NonStaticTargetSourceCodeGenerationStrategy extends AbstractMethodProxyCodeGenerationStrategy {

	protected void generateMethod(CodeVisitor cv, Method method, AdvisedSupport advised, String proxyInternalName, String targetInternalName, String targetDescriptor) {
		int initialLocalsOffset = calculateInitialLocalsOffset(method.getParameterTypes());

		if (method.getReturnType() == void.class) {
			doWithVoidReturn(cv, method, advised, proxyInternalName, targetInternalName, targetDescriptor, initialLocalsOffset);
		}
		else {
			doWithReturn(cv, method, advised, proxyInternalName, targetInternalName, targetDescriptor, initialLocalsOffset);
		}

		cv.visitMaxs(0, 0);
		/*Label openTry = new Label();
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

		cv.visitMaxs(0, 0);*/
	}

	private void doWithVoidReturn(CodeVisitor cv, Method method, AdvisedSupport advised, String proxyInternalName, String targetInternalName, String targetDescriptor, int initialLocalsOffset) {
    String descriptor = Type.getMethodDescriptor(method);
		String methodName = method.getName();

		int localsCounter = initialLocalsOffset;
		int localsTarget = ++localsCounter;
		int localsTargetSource = ++localsCounter;
		int localsCaughtException = ++localsCounter;
		int localsJumpReturnAddress = ++localsCounter;
		int localsUncaughtException = ++localsCounter;

		visitInitTargetAndTargetSource(cv, proxyInternalName, localsTarget, localsTargetSource);

		// define the needed labels
		Label L0 = new Label();
		Label L1 = new Label();
		Label L2 = new Label();
		Label L3 = new Label();
		Label L4 = new Label();
		Label L5 = new Label();
		Label L6 = new Label();
		Label L7 = new Label();
		Label L8 = new Label();

		// start the try block
		cv.visitLabel(L0);

		// get the target from the target source
		cv.visitVarInsn(Constants.ALOAD, localsTargetSource);
		cv.visitMethodInsn(Constants.INVOKEVIRTUAL, TARGET_SOURCE_INTERNAL_NAME, GET_TARGET_METHOD, GET_TARGET_DESCRIPTOR);

		// cast to appropriate type
		cv.visitTypeInsn(Constants.CHECKCAST, targetInternalName); // TODO: try removing this cast for optimization

		// save for later
		cv.visitVarInsn(Constants.ASTORE, localsTarget);

		// get the target back
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
		
		// invoke
		cv.visitMethodInsn(Constants.INVOKEVIRTUAL, targetInternalName, methodName, descriptor);

		// goto normal finally block
		cv.visitJumpInsn(Constants.GOTO, L1);

		// start the catch block
		cv.visitLabel(L2);

		// wrap and throw the exception
		cv.visitVarInsn(Constants.ASTORE, localsCaughtException);
		cv.visitTypeInsn(Constants.NEW, UNDECLARED_THROWABLE_EXCEPTION_INTERNAL_NAME);
		cv.visitInsn(Constants.DUP);
		cv.visitVarInsn(Constants.ALOAD, localsCaughtException);
		cv.visitMethodInsn(Constants.INVOKESPECIAL, UNDECLARED_THROWABLE_EXCEPTION_INTERNAL_NAME, CONSTRUCTOR_INTERNAL_NAME, SINGLE_ARG_EXCEPTION_CONSTRUCTOR_DESCRIPTOR);
		cv.visitInsn(Constants.ATHROW);

		// start the catch remaining block
		cv.visitLabel(L3);
		cv.visitVarInsn(Constants.ASTORE, localsUncaughtException);
		cv.visitJumpInsn(Constants.JSR, L4);
		cv.visitVarInsn(Constants.ALOAD, localsUncaughtException);
		cv.visitInsn(Constants.ATHROW);

		// start the finally block
		cv.visitVarInsn(Constants.ALOAD, localsTargetSource);
		cv.visitVarInsn(Constants.ALOAD, localsTarget);
		cv.visitMethodInsn(Constants.INVOKEINTERFACE, TARGET_SOURCE_INTERNAL_NAME, RELEASE_TARGET_METHOD, RELEASE_TARGET_DESCRIPTOR);
    cv.visitJumpInsn(Constants.GOTO, L6);

		// start the nested catch block
		cv.visitLabel(L7);
		cv.visitInsn(Constants.POP); // todo: should wrap again here

		// return from all JSRs
		cv.visitLabel(L6);
		cv.visitVarInsn(Constants.RET, localsJumpReturnAddress);

    // visit the standard finally jump marker
		cv.visitLabel(L1);

		// jump to finally block
		cv.visitJumpInsn(Constants.JSR, L4);

		// mark the exit and return
		cv.visitLabel(L8);
		cv.visitInsn(Constants.RETURN);

		// mark the try/catch blocks
		cv.visitTryCatchBlock(L0, L2, L2, EXCEPTION_INTERNAL_NAME);
		cv.visitTryCatchBlock(L0, L3, L3, null);
		cv.visitTryCatchBlock(L1, L8, L3, null);
		cv.visitTryCatchBlock(L5, L7, L7, EXCEPTION_INTERNAL_NAME);


	}

	private void doWithReturn(CodeVisitor cv, Method method, AdvisedSupport advised, String proxyInternalName, String targetInternalName, String targetDescriptor, int initialLocalsOffset) {
		int localsCounter = initialLocalsOffset;
		int localsTarget = ++localsCounter;
		int localsTargetSource = ++localsCounter;

		visitInitTargetAndTargetSource(cv, proxyInternalName, localsTarget, localsTargetSource);

	}

	private void visitInitTargetAndTargetSource(CodeVisitor cv, String proxyInternalName, int localsTarget, int localsTargetSource) {
		cv.visitInsn(Constants.ACONST_NULL);
		cv.visitVarInsn(Constants.ASTORE, localsTarget);
		cv.visitVarInsn(Constants.ASTORE, 0); // load this
		cv.visitFieldInsn(Constants.GETFIELD, proxyInternalName, ADVISED_FIELD_NAME, ADVISED_SUPPORT_DESCRIPTOR);
		cv.visitMethodInsn(Constants.INVOKEVIRTUAL, ADVISED_SUPPORT_INTERNAL_NAME, GET_TARGET_SOURCE_METHOD, GET_TARGET_SOURCE_DESCRIPTOR);
		cv.visitVarInsn(Constants.ASTORE, localsTargetSource);
	}
}
