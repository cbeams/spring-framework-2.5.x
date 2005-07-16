
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
public class AdvisedCodeGenerationStrategy extends AbstractMethodProxyCodeGenerationStrategy {

	protected void generateMethod(CodeVisitor cv, Method method, AdvisedSupport advised, String proxyInternalName, String targetInternalName, String targetDescriptor) {

		boolean exposeProxy = advised.isExposeProxy();
		boolean staticTargetSource = advised.getTargetSource().isStatic();
		boolean requiresFinally = (exposeProxy || !(staticTargetSource));

		String methodName = method.getName();

		Class[] parameterTypes = method.getParameterTypes();
		int localThis = 0;

		// used to calculate local variable indexes
		int parametersSize = calculateInitialLocalsOffset(parameterTypes);

		int localCounter = 1 + parametersSize;

		int localOldProxy = Integer.MIN_VALUE;

		if (exposeProxy) {
			localOldProxy = localCounter++;
		}

		int localAdvised = localCounter++;
		int localTargetSource = localCounter++;
		int localTargetClass = localCounter++;
		int localTarget = localCounter++;
		int localArgs = localCounter++;
		int localArgTypes = localCounter++;
		int localMethod = localCounter++;
		int localAdviceChain = localCounter++;
		int localMethodInvocation = localCounter++;
		int localReturnValue = localCounter++;

		// TODO: optimize local usage

		// TODO: need to rework try/catch/finally to match JDT compiler output

		// create a holder for the old proxy if needed
		if (exposeProxy) {
			cv.visitInsn(Constants.ACONST_NULL);
			cv.visitVarInsn(Constants.ASTORE, localOldProxy);
		}

		// load this
		cv.visitVarInsn(Constants.ALOAD, localThis);

		// load advised
		cv.visitFieldInsn(Constants.GETFIELD, proxyInternalName, ADVISED_FIELD_NAME, ADVISED_SUPPORT_DESCRIPTOR);
		cv.visitVarInsn(Constants.ASTORE, localAdvised);

		// load the target source
		cv.visitVarInsn(Constants.ALOAD, localAdvised);
		cv.visitMethodInsn(Constants.INVOKEVIRTUAL, ADVISED_SUPPORT_INTERNAL_NAME, GET_TARGET_SOURCE_METHOD, GET_TARGET_SOURCE_DESCRIPTOR);
		cv.visitVarInsn(Constants.ASTORE, localTargetSource);

		// load the target class
		cv.visitVarInsn(Constants.ALOAD, localTargetSource);
		cv.visitMethodInsn(Constants.INVOKEINTERFACE, TARGET_SOURCE_INTERNAL_NAME, GET_TARGET_CLASS_METHOD, GET_TARGET_CLASS_DESCRIPTOR);
		cv.visitVarInsn(Constants.ASTORE, localTargetClass);

		// try to get target early (otherwise set to null)
		if (staticTargetSource) {
			// load the target directly
			cv.visitVarInsn(Constants.ALOAD, localThis);
			cv.visitFieldInsn(Constants.GETFIELD, proxyInternalName, TARGET_FIELD_NAME, targetDescriptor);
		}
		else {
			cv.visitInsn(Constants.ACONST_NULL);
		}
		cv.visitVarInsn(Constants.ASTORE, localTarget);

		// start the try block
		Label openTry = new Label();
		cv.visitLabel(openTry);

		// load the target from target source inside catch block if needed
		if (!staticTargetSource) {
			// load the target from the target source
			cv.visitVarInsn(Constants.ALOAD, localAdvised);
			cv.visitMethodInsn(Constants.INVOKEVIRTUAL, ADVISED_SUPPORT_INTERNAL_NAME, GET_TARGET_SOURCE_METHOD, GET_TARGET_SOURCE_DESCRIPTOR);
			cv.visitMethodInsn(Constants.INVOKEINTERFACE, TARGET_SOURCE_INTERNAL_NAME, GET_TARGET_METHOD, GET_TARGET_DESCRIPTOR);
			cv.visitTypeInsn(Constants.CHECKCAST, targetInternalName);
			cv.visitVarInsn(Constants.ASTORE, localTarget);
		}

		// bundle up the arguments into an object[]
		bundleArgs(cv, parameterTypes, localArgs);

		// create the arg types array
		bundleArgTypes(cv, parameterTypes, localArgTypes);

		// now get the method via reflection
		cv.visitVarInsn(Constants.ALOAD, localTargetClass);
		cv.visitLdcInsn(methodName);
		cv.visitVarInsn(Constants.ALOAD, localArgTypes);
		cv.visitMethodInsn(Constants.INVOKEVIRTUAL, CLASS_INTERNAL_NAME, GET_METHOD_METHOD, GET_METHOD_DESCRIPTOR);
		cv.visitVarInsn(Constants.ASTORE, localMethod);

		// get the AdvisorChainFactory
		cv.visitVarInsn(Constants.ALOAD, localAdvised);
		cv.visitMethodInsn(Constants.INVOKEVIRTUAL, ADVISED_SUPPORT_INTERNAL_NAME, GET_ADVISOR_CHAIN_FACTORY_METHOD, GET_ADVISOR_CHAIN_FACTORY_DESCRIPTOR);

		// get the advice chain
		cv.visitVarInsn(Constants.ALOAD, localAdvised);
		cv.visitInsn(Constants.ACONST_NULL);
		cv.visitVarInsn(Constants.ALOAD, localMethod);
		cv.visitVarInsn(Constants.ALOAD, localTargetClass);
		cv.visitMethodInsn(Constants.INVOKEINTERFACE, "org/springframework/aop/framework/AdvisorChainFactory", "getInterceptorsAndDynamicInterceptionAdvice", "(Lorg/springframework/aop/framework/Advised;Ljava/lang/Object;Ljava/lang/reflect/Method;Ljava/lang/Class;)Ljava/util/List;");
		cv.visitVarInsn(Constants.ASTORE, localAdviceChain);

		// create the ReflectiveMethodInvocation object
		cv.visitTypeInsn(Constants.NEW, "org/springframework/aop/framework/ReflectiveMethodInvocation");
		cv.visitInsn(Constants.DUP);

		// load the args
		cv.visitVarInsn(Constants.ALOAD, localThis);
		cv.visitVarInsn(Constants.ALOAD, localTarget);
		cv.visitVarInsn(Constants.ALOAD, localMethod);
		cv.visitVarInsn(Constants.ALOAD, localArgs);
		cv.visitVarInsn(Constants.ALOAD, localTargetClass);
		cv.visitVarInsn(Constants.ALOAD, localAdviceChain);

		// create invoke the constructor
		cv.visitMethodInsn(Constants.INVOKESPECIAL, "org/springframework/aop/framework/ReflectiveMethodInvocation", "<init>", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Method;[Ljava/lang/Object;Ljava/lang/Class;Ljava/util/List;)V");
		cv.visitVarInsn(Constants.ASTORE, localMethodInvocation);

		// expose proxy if needed
		if (exposeProxy) {
			cv.visitVarInsn(Constants.ALOAD, localThis);
			cv.visitMethodInsn(Constants.INVOKESTATIC, AOP_CONTEXT_INTERNAL_NAME, SET_CURRENT_PROXY_METHOD, SET_CURRENT_PROXY_DESCRIPTOR);
			cv.visitVarInsn(Constants.ASTORE, localOldProxy);
		}

		// invoke proceed
		cv.visitVarInsn(Constants.ALOAD, localMethodInvocation);
		cv.visitMethodInsn(Constants.INVOKEINTERFACE, "org/aopalliance/intercept/MethodInvocation", "proceed", "()Ljava/lang/Object;");

		// cast return value if required else pop
		Class returnType = method.getReturnType();


		if (returnType == void.class) {
			cv.visitInsn(Constants.POP);
		}
		else {
			if (returnType.isPrimitive()) {
				visitUnwrapPrimtiveType(cv, returnType);
			}
			else {
				cv.visitTypeInsn(Constants.CHECKCAST, Type.getInternalName(returnType));
			}

			if (requiresFinally) {
				cv.visitVarInsn(getStoreOpcodeForType(returnType), localReturnValue);
			}
		}


		// close the try block
		Label closeTry = new Label();
		cv.visitLabel(closeTry);

		// create the first finally block if needed
		if (requiresFinally) {
			if (exposeProxy) {
				cv.visitVarInsn(Constants.ALOAD, localOldProxy);
				cv.visitMethodInsn(Constants.INVOKESTATIC, AOP_CONTEXT_INTERNAL_NAME, SET_CURRENT_PROXY_METHOD, SET_CURRENT_PROXY_DESCRIPTOR);
				cv.visitInsn(Constants.POP);
			}

			if (!staticTargetSource) {
				cv.visitVarInsn(Constants.ALOAD, localTargetSource);
				cv.visitVarInsn(Constants.ALOAD, localTarget);
				cv.visitMethodInsn(Constants.INVOKEINTERFACE, TARGET_SOURCE_INTERNAL_NAME, "releaseTarget", "(Ljava/lang/Object;)V");
			}

			if (returnType != void.class) {
				cv.visitVarInsn(getLoadOpcodeForType(returnType), localReturnValue);
			}
		}

		// exit block
		Label exit = new Label();

		// return if required
		if (returnType != void.class) {
			cv.visitInsn(getReturnOpcodeForType(returnType));
		}
		else {
			cv.visitJumpInsn(Constants.GOTO, exit);
		}

		// create the catch blocks for the exceptions on the method
		int stackThrowable = 1 + parametersSize;

		if (exposeProxy) {
			stackThrowable++;
		}


		// create the catch block for NoSuchMethodException
		Label catchNoSuchMethodException = new Label();
		cv.visitLabel(catchNoSuchMethodException);
		cv.visitVarInsn(Constants.ASTORE, stackThrowable);
		cv.visitTypeInsn(Constants.NEW, "java/lang/IllegalStateException");
		cv.visitInsn(Constants.DUP);
		cv.visitLdcInsn("Unable to access joinpoint via reflection");
		cv.visitVarInsn(Constants.ALOAD, stackThrowable);
		cv.visitMethodInsn(Constants.INVOKESPECIAL, "java/lang/IllegalStateException", "<init>", "(Ljava/lang/String;Ljava/lang/Throwable;)V");
		cv.visitInsn(Constants.ATHROW);

		// create the catch block for remaining Throwable
		Class[] exceptionTypes = method.getExceptionTypes();
		Label[] handlerLabels = new Label[exceptionTypes.length];

		for (int i = 0; i < exceptionTypes.length; i++) {
			Label handlerLabel = new Label();
			cv.visitLabel(handlerLabel);
			cv.visitVarInsn(Constants.ASTORE, stackThrowable);
			cv.visitVarInsn(Constants.ALOAD, stackThrowable);
			cv.visitInsn(Constants.ATHROW);
			handlerLabels[i] = handlerLabel;
		}

		// catch runtime exceptions and throw them as they are
		Label catchRuntimeException = new Label();
		cv.visitLabel(catchRuntimeException);
		cv.visitVarInsn(Constants.ASTORE, stackThrowable);
		cv.visitVarInsn(Constants.ALOAD, stackThrowable);
		cv.visitInsn(Constants.ATHROW);

		Label catchThrowable = new Label();
		cv.visitLabel(catchThrowable);
		cv.visitVarInsn(Constants.ASTORE, stackThrowable);
		cv.visitTypeInsn(Constants.NEW, "java/lang/reflect/UndeclaredThrowableException");
		cv.visitInsn(Constants.DUP);
		cv.visitVarInsn(Constants.ALOAD, stackThrowable);
		cv.visitMethodInsn(Constants.INVOKESPECIAL, "java/lang/reflect/UndeclaredThrowableException", "<init>", "(Ljava/lang/Throwable;)V");
		cv.visitInsn(Constants.ATHROW);

		Label catchForFinally = null;
		Label execptionalFinally = null;

		if (requiresFinally) {
			catchForFinally = new Label();
			cv.visitLabel(catchForFinally);
			cv.visitVarInsn(Constants.ASTORE, stackThrowable);

			execptionalFinally = new Label();
			cv.visitLabel(execptionalFinally);

			if (exposeProxy) {
				cv.visitVarInsn(Constants.ALOAD, localOldProxy);
				cv.visitMethodInsn(Constants.INVOKESTATIC, AOP_CONTEXT_INTERNAL_NAME, SET_CURRENT_PROXY_METHOD, SET_CURRENT_PROXY_DESCRIPTOR);
				cv.visitInsn(Constants.POP);
			}

			if (!staticTargetSource) {

				// TODO: need embedded try/catch block here
				cv.visitVarInsn(Constants.ALOAD, localTargetSource);
				cv.visitVarInsn(Constants.ALOAD, localTarget);
				cv.visitMethodInsn(Constants.INVOKEINTERFACE, TARGET_SOURCE_INTERNAL_NAME, "releaseTarget", "(Ljava/lang/Object;)V");
			}

			cv.visitVarInsn(Constants.ALOAD, stackThrowable);
			cv.visitInsn(Constants.ATHROW);
		}

		// close
		cv.visitLabel(exit);

		// return now for void
		if (returnType == void.class) {
			cv.visitInsn(getReturnOpcodeForType(method.getReturnType()));
		}

		for (int i = 0; i < handlerLabels.length; i++) {
			cv.visitTryCatchBlock(openTry, closeTry, handlerLabels[i], Type.getInternalName(exceptionTypes[i]));
		}

		cv.visitTryCatchBlock(openTry, closeTry, catchNoSuchMethodException, Type.getInternalName(NoSuchMethodException.class));
		cv.visitTryCatchBlock(openTry, closeTry, catchRuntimeException, Type.getInternalName(RuntimeException.class));
		cv.visitTryCatchBlock(openTry, closeTry, catchThrowable, Type.getInternalName(Throwable.class));

		if (requiresFinally) {
			cv.visitTryCatchBlock(openTry, closeTry, catchForFinally, null);

			// TODO: test expose proxy with declared exceptions
			cv.visitTryCatchBlock(catchNoSuchMethodException, execptionalFinally, catchForFinally, null);
		}

		cv.visitMaxs(0, 0);
	}

	private void bundleArgTypes(CodeVisitor cv, Class[] parameterTypes, int stackIndex) {
		int size = parameterTypes.length;

		if (size == 0) {
			cv.visitInsn(Constants.ACONST_NULL);
			cv.visitVarInsn(Constants.ASTORE, stackIndex);
		}
		else {
			visitIntegerInsn(size, cv);
			cv.visitTypeInsn(Constants.ANEWARRAY, CLASS_INTERNAL_NAME);
			cv.visitVarInsn(Constants.ASTORE, stackIndex);

			for (int i = 0; i < parameterTypes.length; i++) {
				Class parameterType = parameterTypes[i];

				// load the array
				cv.visitVarInsn(Constants.ALOAD, stackIndex);

				// load the index
				visitIntegerInsn(i, cv);

				// load the class
				if (parameterType.isPrimitive()) {
					visitGetPrimitiveType(cv, parameterType);
				}
				else {
					cv.visitLdcInsn(Type.getType(parameterType));
				}

				// store in array
				cv.visitInsn(Constants.AASTORE);
			}
		}
	}

	private void bundleArgs(CodeVisitor cv, Class[] parameterTypes, int stackIndex) {
		// create the object array
		int size = parameterTypes.length;

		if (size == 0) {
			cv.visitInsn(Constants.ACONST_NULL);
			cv.visitVarInsn(Constants.ASTORE, stackIndex);
		}
		else {
			visitIntegerInsn(size, cv);
			cv.visitTypeInsn(Constants.ANEWARRAY, OBJECT_INTERNAL_NAME);
			cv.visitVarInsn(Constants.ASTORE, stackIndex);

			int stackCount = 1;
			for (int i = 0; i < parameterTypes.length; i++) {
				Class parameterType = parameterTypes[i];

				// load the array
				cv.visitVarInsn(Constants.ALOAD, stackIndex);

				// load the index
				visitIntegerInsn(i, cv);

				// load the argument
				cv.visitVarInsn(getLoadOpcodeForType(parameterType), stackCount);

				if (parameterType.isPrimitive()) {
					// wrap primtive
					visitWrapPrimitive(cv, parameterType);
				}

				// store in array
				cv.visitInsn(Constants.AASTORE);

				stackCount += getLocalsSizeForType(parameterType);

			}
		}
	}
}
