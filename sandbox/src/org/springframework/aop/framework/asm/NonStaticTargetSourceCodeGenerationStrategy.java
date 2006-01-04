
package org.springframework.aop.framework.asm;

import java.lang.reflect.Method;

import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.MethodVisitor;

import org.springframework.aop.framework.AdvisedSupport;

/**
 * @author robh
 */
public class NonStaticTargetSourceCodeGenerationStrategy extends AbstractMethodProxyCodeGenerationStrategy {

	protected void generateMethod(MethodVisitor mv, Method method, AdvisedSupport advised, String proxyInternalName, String targetInternalName, String targetDescriptor) {
		int initialLocalsOffset = calculateInitialLocalsOffset(method.getParameterTypes());

		if (method.getReturnType() == void.class) {
			doWithVoidReturn(mv, method, proxyInternalName, targetInternalName, initialLocalsOffset);
		}
		else {
			doWithReturn(mv, method, advised, proxyInternalName, targetInternalName, targetDescriptor, initialLocalsOffset);
		}

		mv.visitMaxs(0, 0);
	}

	private void doWithVoidReturn(MethodVisitor cv, Method method, String proxyInternalName, String targetInternalName, int initialLocalsOffset) {

		int localsCounter = initialLocalsOffset;
		int localsTarget = ++localsCounter;
		int localsTargetSource = ++localsCounter;
		int localsCaughtException = ++localsCounter;
		int localsJumpReturnAddress = ++localsCounter;
		int localsUncaughtException = ++localsCounter;

		visitInitTargetAndTargetSource(cv, proxyInternalName, localsTarget, localsTargetSource);

		// define the needed labels
		Label startTry = new Label();
		Label finallyJumpPoint = new Label();
		Label startCatch = new Label();
		Label startSyntheticCatch = new Label();
		Label startFinally = new Label();
		Label startNestedTry = new Label();
		Label finallyReturnPoint = new Label();
		Label startNestedCatch = new Label();
		Label exitPoint = new Label();

		// start the try block
		cv.visitLabel(startTry);

		// load target from target source
		visitLoadTargetFromTargetSource(cv, targetInternalName, localsTargetSource, localsTarget);

		// invoke the target method
		visitInvokeTargetMethod(cv, targetInternalName, method, localsTarget);

		// goto normal finally block
		cv.visitJumpInsn(Opcodes.GOTO, finallyJumpPoint);

		// start the catch block
		cv.visitLabel(startCatch);

		// wrap and throw the exception
		visitWrapAndThrow(cv, null, localsCaughtException);

		// start the catch remaining block
		cv.visitLabel(startSyntheticCatch);
		visitExecuteFinallyAndThrow(cv, startFinally, localsUncaughtException);

		// start the finally block
		cv.visitLabel(startFinally);
		cv.visitVarInsn(Opcodes.ASTORE, localsJumpReturnAddress);
		cv.visitLabel(startNestedTry);
		visitReleaseTarget(cv, localsTargetSource, localsTarget);
		cv.visitJumpInsn(Opcodes.GOTO, finallyReturnPoint);

		// start the nested catch block
		cv.visitLabel(startNestedCatch);
		visitWrapAndThrow(cv, "Unable to release target source", localsCaughtException);

		// return from all JSRs
		cv.visitLabel(finallyReturnPoint);
		cv.visitVarInsn(Opcodes.RET, localsJumpReturnAddress);

		// visit the standard finally jump marker
		cv.visitLabel(finallyJumpPoint);

		// jump to finally block
		cv.visitJumpInsn(Opcodes.JSR, startFinally);

		// mark the exit and return
		cv.visitLabel(exitPoint);
		cv.visitInsn(Opcodes.RETURN);

		// mark the try/catch blocks
		cv.visitTryCatchBlock(startTry, startCatch, startCatch, EXCEPTION_INTERNAL_NAME);
		cv.visitTryCatchBlock(startTry, startSyntheticCatch, startSyntheticCatch, null);
		cv.visitTryCatchBlock(finallyJumpPoint, exitPoint, startSyntheticCatch, null);
		cv.visitTryCatchBlock(startNestedTry, startNestedCatch, startNestedCatch, EXCEPTION_INTERNAL_NAME);


	}


	private void doWithReturn(MethodVisitor cv, Method method, AdvisedSupport advised, String proxyInternalName, String targetInternalName, String targetDescriptor, int initialLocalsOffset) {
		String descriptor = Type.getMethodDescriptor(method);
		String methodName = method.getName();

		int localsCounter = initialLocalsOffset;
		int localsTarget = ++localsCounter;
		int localsTargetSource = ++localsCounter;
		int localsCaughtException = ++localsCounter;
		int localsJumpReturnAddress = ++localsCounter;
		int localsUncaughtException = ++localsCounter;
		int localsReturnValue = ++localsCounter;

		visitInitTargetAndTargetSource(cv, proxyInternalName, localsTarget, localsTargetSource);

		// define needed labels
		Label startTry = new Label();
		Label startFinally = new Label();
		Label endTry = new Label();
		Label startCatch = new Label();
		Label startSyntheticCatch = new Label();
		Label startNestedTry = new Label();
		Label exitPoint = new Label();
		Label startNestedCatch = new Label();

		// start the try block
		cv.visitLabel(startTry);

		// load target from target source
		visitLoadTargetFromTargetSource(cv, targetInternalName, localsTargetSource, localsTarget);

		// invoke the target method
		visitInvokeTargetMethod(cv, targetInternalName, method, localsTarget);

		// store the return value
		cv.visitVarInsn(Opcodes.ASTORE, localsReturnValue);

		// jump to finally block
		cv.visitJumpInsn(Opcodes.JSR, startFinally);

		// mark the end of the try block (including the call to finally)
		cv.visitLabel(endTry);

		// load the return value and return
		Class returnType = method.getReturnType();
		cv.visitVarInsn(getLoadOpcodeForType(returnType), localsReturnValue);
		cv.visitInsn(getReturnOpcodeForType(returnType));

		// mark the start of the defined catch block
		cv.visitLabel(startCatch);
		visitWrapAndThrow(cv, null, localsCaughtException);

		// mark the start of the synthetic catch block
		cv.visitLabel(startSyntheticCatch);
		visitExecuteFinallyAndThrow(cv, startFinally, localsUncaughtException);

		// visit the finally block
		cv.visitLabel(startFinally);
		cv.visitVarInsn(Opcodes.ASTORE, localsJumpReturnAddress);

		// start the nested try block
		cv.visitLabel(startNestedTry);
		visitReleaseTarget(cv, localsTargetSource, localsTarget);
		cv.visitJumpInsn(Opcodes.GOTO, exitPoint);

		// start the nested catch block
		cv.visitLabel(startNestedCatch);
		visitWrapAndThrow(cv, "Unable to release target source", localsCaughtException);

		// mark the exit point
		cv.visitLabel(exitPoint);
		cv.visitVarInsn(Opcodes.RET, localsJumpReturnAddress);

		// visit try/catch blocks
		cv.visitTryCatchBlock(startTry, startCatch, startCatch, EXCEPTION_INTERNAL_NAME);
		cv.visitTryCatchBlock(startTry, endTry, startSyntheticCatch, null);
		cv.visitTryCatchBlock(startCatch, startSyntheticCatch, startSyntheticCatch, null);
		cv.visitTryCatchBlock(startNestedTry, startNestedCatch, startNestedCatch, EXCEPTION_INTERNAL_NAME);


	}

	private void visitReleaseTarget(MethodVisitor cv, int localsTargetSource, int localsTarget) {
		cv.visitVarInsn(Opcodes.ALOAD, localsTargetSource);
		cv.visitVarInsn(Opcodes.ALOAD, localsTarget);
		cv.visitMethodInsn(Opcodes.INVOKEINTERFACE, TARGET_SOURCE_INTERNAL_NAME, RELEASE_TARGET_METHOD, RELEASE_TARGET_DESCRIPTOR);
	}

	private void visitExecuteFinallyAndThrow(MethodVisitor cv, Label finallyBlock, int localsException) {
		cv.visitVarInsn(Opcodes.ASTORE, localsException);
		cv.visitJumpInsn(Opcodes.JSR, finallyBlock);
		cv.visitVarInsn(Opcodes.ALOAD, localsException);
		cv.visitInsn(Opcodes.ATHROW);
	}

	private void visitWrapAndThrow(MethodVisitor cv, String message, int localsWrappedException) {
		cv.visitVarInsn(Opcodes.ASTORE, localsWrappedException);
		cv.visitTypeInsn(Opcodes.NEW, UNDECLARED_THROWABLE_EXCEPTION_INTERNAL_NAME);
		cv.visitInsn(Opcodes.DUP);

		String exceptionConstructorDescriptor = SINGLE_ARG_EXCEPTION_CONSTRUCTOR_DESCRIPTOR;
		if (message != null) {
			cv.visitLdcInsn(message);
			exceptionConstructorDescriptor = EXCEPTION_CONSTRUCTOR_DESCRIPTOR;
		}

		cv.visitVarInsn(Opcodes.ALOAD, localsWrappedException);
		cv.visitMethodInsn(Opcodes.INVOKESPECIAL, UNDECLARED_THROWABLE_EXCEPTION_INTERNAL_NAME, CONSTRUCTOR_INTERNAL_NAME, exceptionConstructorDescriptor);
		cv.visitInsn(Opcodes.ATHROW);
	}

	private void visitInvokeTargetMethod(MethodVisitor cv, String targetInternalName, Method method, int localsTarget) {
		// get the target back
		cv.visitVarInsn(Opcodes.ALOAD, localsTarget);

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
		String methodName = method.getName();
		String methodDescriptor = Type.getMethodDescriptor(method);
		cv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, targetInternalName, methodName, methodDescriptor);
	}

	private void visitLoadTargetFromTargetSource(MethodVisitor cv, String targetInternalName, int localsTargetSource, int localsTarget) {
		// get the target from the target source
		cv.visitVarInsn(Opcodes.ALOAD, localsTargetSource);
		cv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TARGET_SOURCE_INTERNAL_NAME, GET_TARGET_METHOD, GET_TARGET_DESCRIPTOR);

		// cast to appropriate type
		cv.visitTypeInsn(Opcodes.CHECKCAST, targetInternalName); // TODO: try removing this cast for optimization

		// save for later
		cv.visitVarInsn(Opcodes.ASTORE, localsTarget);
	}

	private void visitInitTargetAndTargetSource(MethodVisitor cv, String proxyInternalName, int localsTarget, int localsTargetSource) {
		cv.visitInsn(Opcodes.ACONST_NULL);
		cv.visitVarInsn(Opcodes.ASTORE, localsTarget);
		cv.visitVarInsn(Opcodes.ALOAD, 0); // load this
		cv.visitFieldInsn(Opcodes.GETFIELD, proxyInternalName, ADVISED_FIELD_NAME, ADVISED_SUPPORT_DESCRIPTOR);
		cv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, ADVISED_SUPPORT_INTERNAL_NAME, GET_TARGET_SOURCE_METHOD, GET_TARGET_SOURCE_DESCRIPTOR);
		cv.visitVarInsn(Opcodes.ASTORE, localsTargetSource);
	}
}
