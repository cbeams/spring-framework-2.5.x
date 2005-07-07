
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
		int initialLocalsOffset = calculateInitialLocalsOffset(method.getParameterTypes());

		if (method.getReturnType() == void.class) {
			doWithVoidReturn(cv, method, proxyInternalName, targetInternalName, initialLocalsOffset);
		}
		else {
			doWithReturn(cv, method, advised, proxyInternalName, targetInternalName, targetDescriptor, initialLocalsOffset);
		}

		cv.visitMaxs(0, 0);
	}

	private void doWithVoidReturn(CodeVisitor cv, Method method, String proxyInternalName, String targetInternalName, int initialLocalsOffset) {

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
		cv.visitJumpInsn(Constants.GOTO, finallyJumpPoint);

		// start the catch block
		cv.visitLabel(startCatch);

		// wrap and throw the exception
		visitWrapAndThrow(cv, null, localsCaughtException);

		// start the catch remaining block
		cv.visitLabel(startSyntheticCatch);
		visitExecuteFinallyAndThrow(cv, startFinally, localsUncaughtException);

		// start the finally block
		cv.visitLabel(startFinally);
		cv.visitVarInsn(Constants.ASTORE, localsJumpReturnAddress);
		cv.visitLabel(startNestedTry);
		visitReleaseTarget(cv, localsTargetSource, localsTarget);
		cv.visitJumpInsn(Constants.GOTO, finallyReturnPoint);

		// start the nested catch block
		cv.visitLabel(startNestedCatch);
		visitWrapAndThrow(cv, "Unable to release target source", localsCaughtException);

		// return from all JSRs
		cv.visitLabel(finallyReturnPoint);
		cv.visitVarInsn(Constants.RET, localsJumpReturnAddress);

		// visit the standard finally jump marker
		cv.visitLabel(finallyJumpPoint);

		// jump to finally block
		cv.visitJumpInsn(Constants.JSR, startFinally);

		// mark the exit and return
		cv.visitLabel(exitPoint);
		cv.visitInsn(Constants.RETURN);

		// mark the try/catch blocks
		cv.visitTryCatchBlock(startTry, startCatch, startCatch, EXCEPTION_INTERNAL_NAME);
		cv.visitTryCatchBlock(startTry, startSyntheticCatch, startSyntheticCatch, null);
		cv.visitTryCatchBlock(finallyJumpPoint, exitPoint, startSyntheticCatch, null);
		cv.visitTryCatchBlock(startNestedTry, startNestedCatch, startNestedCatch, EXCEPTION_INTERNAL_NAME);


	}


	private void doWithReturn(CodeVisitor cv, Method method, AdvisedSupport advised, String proxyInternalName, String targetInternalName, String targetDescriptor, int initialLocalsOffset) {
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
		cv.visitVarInsn(Constants.ASTORE, localsReturnValue);

		// jump to finally block
		cv.visitJumpInsn(Constants.JSR, startFinally);

		// mark the end of the try block (including the call to finally)
		cv.visitLabel(endTry);

		// load the return value and return
		Class<?> returnType = method.getReturnType();
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
		cv.visitVarInsn(Constants.ASTORE, localsJumpReturnAddress);

		// start the nested try block
		cv.visitLabel(startNestedTry);
		visitReleaseTarget(cv, localsTargetSource, localsTarget);
		cv.visitJumpInsn(Constants.GOTO, exitPoint);

		// start the nested catch block
		cv.visitLabel(startNestedCatch);
		visitWrapAndThrow(cv, "Unable to release target source", localsCaughtException);

		// mark the exit point
		cv.visitLabel(exitPoint);
		cv.visitVarInsn(Constants.RET, localsJumpReturnAddress);

		// visit try/catch blocks
		cv.visitTryCatchBlock(startTry, startCatch, startCatch, EXCEPTION_INTERNAL_NAME);
		cv.visitTryCatchBlock(startTry, endTry, startSyntheticCatch, null);
		cv.visitTryCatchBlock(startCatch, startSyntheticCatch, startSyntheticCatch, null);
		cv.visitTryCatchBlock(startNestedTry, startNestedCatch, startNestedCatch, EXCEPTION_INTERNAL_NAME);


	}

	private void visitReleaseTarget(CodeVisitor cv, int localsTargetSource, int localsTarget) {
		cv.visitVarInsn(Constants.ALOAD, localsTargetSource);
		cv.visitVarInsn(Constants.ALOAD, localsTarget);
		cv.visitMethodInsn(Constants.INVOKEINTERFACE, TARGET_SOURCE_INTERNAL_NAME, RELEASE_TARGET_METHOD, RELEASE_TARGET_DESCRIPTOR);
	}

	private void visitExecuteFinallyAndThrow(CodeVisitor cv, Label finallyBlock, int localsException) {
		cv.visitVarInsn(Constants.ASTORE, localsException);
		cv.visitJumpInsn(Constants.JSR, finallyBlock);
		cv.visitVarInsn(Constants.ALOAD, localsException);
		cv.visitInsn(Constants.ATHROW);
	}

	private void visitWrapAndThrow(CodeVisitor cv, String message, int localsWrappedException) {
		cv.visitVarInsn(Constants.ASTORE, localsWrappedException);
		cv.visitTypeInsn(Constants.NEW, UNDECLARED_THROWABLE_EXCEPTION_INTERNAL_NAME);
		cv.visitInsn(Constants.DUP);

		String exceptionConstructorDescriptor = SINGLE_ARG_EXCEPTION_CONSTRUCTOR_DESCRIPTOR;
		if (message != null) {
			cv.visitLdcInsn(message);
			exceptionConstructorDescriptor = EXCEPTION_CONSTRUCTOR_DESCRIPTOR;
		}

		cv.visitVarInsn(Constants.ALOAD, localsWrappedException);
		cv.visitMethodInsn(Constants.INVOKESPECIAL, UNDECLARED_THROWABLE_EXCEPTION_INTERNAL_NAME, CONSTRUCTOR_INTERNAL_NAME, exceptionConstructorDescriptor);
		cv.visitInsn(Constants.ATHROW);
	}

	private void visitInvokeTargetMethod(CodeVisitor cv, String targetInternalName, Method method, int localsTarget) {
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
		String methodName = method.getName();
		String methodDescriptor = Type.getMethodDescriptor(method);
		cv.visitMethodInsn(Constants.INVOKEVIRTUAL, targetInternalName, methodName, methodDescriptor);
	}

	private void visitLoadTargetFromTargetSource(CodeVisitor cv, String targetInternalName, int localsTargetSource, int localsTarget) {
		// get the target from the target source
		cv.visitVarInsn(Constants.ALOAD, localsTargetSource);
		cv.visitMethodInsn(Constants.INVOKEVIRTUAL, TARGET_SOURCE_INTERNAL_NAME, GET_TARGET_METHOD, GET_TARGET_DESCRIPTOR);

		// cast to appropriate type
		cv.visitTypeInsn(Constants.CHECKCAST, targetInternalName); // TODO: try removing this cast for optimization

		// save for later
		cv.visitVarInsn(Constants.ASTORE, localsTarget);
	}

	private void visitInitTargetAndTargetSource(CodeVisitor cv, String proxyInternalName, int localsTarget, int localsTargetSource) {
		cv.visitInsn(Constants.ACONST_NULL);
		cv.visitVarInsn(Constants.ASTORE, localsTarget);
		cv.visitVarInsn(Constants.ALOAD, 0); // load this
		cv.visitFieldInsn(Constants.GETFIELD, proxyInternalName, ADVISED_FIELD_NAME, ADVISED_SUPPORT_DESCRIPTOR);
		cv.visitMethodInsn(Constants.INVOKEVIRTUAL, ADVISED_SUPPORT_INTERNAL_NAME, GET_TARGET_SOURCE_METHOD, GET_TARGET_SOURCE_DESCRIPTOR);
		cv.visitVarInsn(Constants.ASTORE, localsTargetSource);
	}
}
