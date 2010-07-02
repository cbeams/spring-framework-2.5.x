
package org.springframework.aop.framework.asm;

import java.lang.reflect.Method;

import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import org.springframework.aop.framework.AdvisedSupport;

/**
 * @author robh
 */
public class AdvisedCodeGenerationStrategy extends AbstractMethodProxyCodeGenerationStrategy {

	protected void generateMethod(MethodVisitor mv, Method method, AdvisedSupport advised, String proxyInternalName, String targetInternalName, String targetDescriptor) {

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
			mv.visitInsn(Opcodes.ACONST_NULL);
			mv.visitVarInsn(Opcodes.ASTORE, localOldProxy);
		}

		// load this
		mv.visitVarInsn(Opcodes.ALOAD, localThis);

		// load advised
		mv.visitFieldInsn(Opcodes.GETFIELD, proxyInternalName, ADVISED_FIELD_NAME, ADVISED_SUPPORT_DESCRIPTOR);
		mv.visitVarInsn(Opcodes.ASTORE, localAdvised);

		// load the target source
		mv.visitVarInsn(Opcodes.ALOAD, localAdvised);
		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, ADVISED_SUPPORT_INTERNAL_NAME, GET_TARGET_SOURCE_METHOD, GET_TARGET_SOURCE_DESCRIPTOR);
		mv.visitVarInsn(Opcodes.ASTORE, localTargetSource);

		// load the target class
		mv.visitVarInsn(Opcodes.ALOAD, localTargetSource);
		mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, TARGET_SOURCE_INTERNAL_NAME, GET_TARGET_CLASS_METHOD, GET_TARGET_CLASS_DESCRIPTOR);
		mv.visitVarInsn(Opcodes.ASTORE, localTargetClass);

		// try to get target early (otherwise set to null)
		if (staticTargetSource) {
			// load the target directly
			mv.visitVarInsn(Opcodes.ALOAD, localThis);
			mv.visitFieldInsn(Opcodes.GETFIELD, proxyInternalName, TARGET_FIELD_NAME, targetDescriptor);
		}
		else {
			mv.visitInsn(Opcodes.ACONST_NULL);
		}
		mv.visitVarInsn(Opcodes.ASTORE, localTarget);

		// start the try block
		Label openTry = new Label();
		mv.visitLabel(openTry);

		// load the target from target source inside catch block if needed
		if (!staticTargetSource) {
			// load the target from the target source
			mv.visitVarInsn(Opcodes.ALOAD, localAdvised);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, ADVISED_SUPPORT_INTERNAL_NAME, GET_TARGET_SOURCE_METHOD, GET_TARGET_SOURCE_DESCRIPTOR);
			mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, TARGET_SOURCE_INTERNAL_NAME, GET_TARGET_METHOD, GET_TARGET_DESCRIPTOR);
			mv.visitTypeInsn(Opcodes.CHECKCAST, targetInternalName);
			mv.visitVarInsn(Opcodes.ASTORE, localTarget);
		}

		// bundle up the arguments into an object[]
		bundleArgs(mv, parameterTypes, localArgs);

		// create the arg types array
		bundleArgTypes(mv, parameterTypes, localArgTypes);

		// now get the method via reflection
		mv.visitVarInsn(Opcodes.ALOAD, localTargetClass);
		mv.visitLdcInsn(methodName);
		mv.visitVarInsn(Opcodes.ALOAD, localArgTypes);
		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, CLASS_INTERNAL_NAME, GET_METHOD_METHOD, GET_METHOD_DESCRIPTOR);
		mv.visitVarInsn(Opcodes.ASTORE, localMethod);

		// get the AdvisorChainFactory
		mv.visitVarInsn(Opcodes.ALOAD, localAdvised);
		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, ADVISED_SUPPORT_INTERNAL_NAME, GET_ADVISOR_CHAIN_FACTORY_METHOD, GET_ADVISOR_CHAIN_FACTORY_DESCRIPTOR);

		// get the advice chain
		mv.visitVarInsn(Opcodes.ALOAD, localAdvised);
		mv.visitInsn(Opcodes.ACONST_NULL);
		mv.visitVarInsn(Opcodes.ALOAD, localMethod);
		mv.visitVarInsn(Opcodes.ALOAD, localTargetClass);
		mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "org/springframework/aop/framework/AdvisorChainFactory", "getInterceptorsAndDynamicInterceptionAdvice", "(Lorg/springframework/aop/framework/Advised;Ljava/lang/Object;Ljava/lang/reflect/Method;Ljava/lang/Class;)Ljava/util/List;");
		mv.visitVarInsn(Opcodes.ASTORE, localAdviceChain);

		// create the ReflectiveMethodInvocation object
		mv.visitTypeInsn(Opcodes.NEW, "org/springframework/aop/framework/ReflectiveMethodInvocation");
		mv.visitInsn(Opcodes.DUP);

		// load the args
		mv.visitVarInsn(Opcodes.ALOAD, localThis);
		mv.visitVarInsn(Opcodes.ALOAD, localTarget);
		mv.visitVarInsn(Opcodes.ALOAD, localMethod);
		mv.visitVarInsn(Opcodes.ALOAD, localArgs);
		mv.visitVarInsn(Opcodes.ALOAD, localTargetClass);
		mv.visitVarInsn(Opcodes.ALOAD, localAdviceChain);

		// create invoke the constructor
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "org/springframework/aop/framework/ReflectiveMethodInvocation", "<init>", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Method;[Ljava/lang/Object;Ljava/lang/Class;Ljava/util/List;)V");
		mv.visitVarInsn(Opcodes.ASTORE, localMethodInvocation);

		// expose proxy if needed
		if (exposeProxy) {
			mv.visitVarInsn(Opcodes.ALOAD, localThis);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, AOP_CONTEXT_INTERNAL_NAME, SET_CURRENT_PROXY_METHOD, SET_CURRENT_PROXY_DESCRIPTOR);
			mv.visitVarInsn(Opcodes.ASTORE, localOldProxy);
		}

		// invoke proceed
		mv.visitVarInsn(Opcodes.ALOAD, localMethodInvocation);
		mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "org/aopalliance/intercept/MethodInvocation", "proceed", "()Ljava/lang/Object;");

		// cast return value if required else pop
		Class returnType = method.getReturnType();


		if (returnType == void.class) {
			mv.visitInsn(Opcodes.POP);
		}
		else {
			if (returnType.isPrimitive()) {
				visitUnwrapPrimtiveType(mv, returnType);
			}
			else {
				mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(returnType));
			}

			if (requiresFinally) {
				mv.visitVarInsn(getStoreOpcodeForType(returnType), localReturnValue);
			}
		}


		// close the try block
		Label closeTry = new Label();
		mv.visitLabel(closeTry);

		// create the first finally block if needed
		if (requiresFinally) {
			if (exposeProxy) {
				mv.visitVarInsn(Opcodes.ALOAD, localOldProxy);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, AOP_CONTEXT_INTERNAL_NAME, SET_CURRENT_PROXY_METHOD, SET_CURRENT_PROXY_DESCRIPTOR);
				mv.visitInsn(Opcodes.POP);
			}

			if (!staticTargetSource) {
				mv.visitVarInsn(Opcodes.ALOAD, localTargetSource);
				mv.visitVarInsn(Opcodes.ALOAD, localTarget);
				mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, TARGET_SOURCE_INTERNAL_NAME, "releaseTarget", "(Ljava/lang/Object;)V");
			}

			if (returnType != void.class) {
				mv.visitVarInsn(getLoadOpcodeForType(returnType), localReturnValue);
			}
		}

		// exit block
		Label exit = new Label();

		// return if required
		if (returnType != void.class) {
			mv.visitInsn(getReturnOpcodeForType(returnType));
		}
		else {
			mv.visitJumpInsn(Opcodes.GOTO, exit);
		}

		// create the catch blocks for the exceptions on the method
		int stackThrowable = 1 + parametersSize;

		if (exposeProxy) {
			stackThrowable++;
		}


		// create the catch block for NoSuchMethodException
		Label catchNoSuchMethodException = new Label();
		mv.visitLabel(catchNoSuchMethodException);
		mv.visitVarInsn(Opcodes.ASTORE, stackThrowable);
		mv.visitTypeInsn(Opcodes.NEW, "java/lang/IllegalStateException");
		mv.visitInsn(Opcodes.DUP);
		mv.visitLdcInsn("Unable to access joinpoint via reflection");
		mv.visitVarInsn(Opcodes.ALOAD, stackThrowable);
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/IllegalStateException", "<init>", "(Ljava/lang/String;Ljava/lang/Throwable;)V");
		mv.visitInsn(Opcodes.ATHROW);

		// create the catch block for remaining Throwable
		Class[] exceptionTypes = method.getExceptionTypes();
		Label[] handlerLabels = new Label[exceptionTypes.length];

		for (int i = 0; i < exceptionTypes.length; i++) {
			Label handlerLabel = new Label();
			mv.visitLabel(handlerLabel);
			mv.visitVarInsn(Opcodes.ASTORE, stackThrowable);
			mv.visitVarInsn(Opcodes.ALOAD, stackThrowable);
			mv.visitInsn(Opcodes.ATHROW);
			handlerLabels[i] = handlerLabel;
		}

		// catch runtime exceptions and throw them as they are
		Label catchRuntimeException = new Label();
		mv.visitLabel(catchRuntimeException);
		mv.visitVarInsn(Opcodes.ASTORE, stackThrowable);
		mv.visitVarInsn(Opcodes.ALOAD, stackThrowable);
		mv.visitInsn(Opcodes.ATHROW);

		Label catchThrowable = new Label();
		mv.visitLabel(catchThrowable);
		mv.visitVarInsn(Opcodes.ASTORE, stackThrowable);
		mv.visitTypeInsn(Opcodes.NEW, "java/lang/reflect/UndeclaredThrowableException");
		mv.visitInsn(Opcodes.DUP);
		mv.visitVarInsn(Opcodes.ALOAD, stackThrowable);
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/reflect/UndeclaredThrowableException", "<init>", "(Ljava/lang/Throwable;)V");
		mv.visitInsn(Opcodes.ATHROW);

		Label catchForFinally = null;
		Label execptionalFinally = null;

		if (requiresFinally) {
			catchForFinally = new Label();
			mv.visitLabel(catchForFinally);
			mv.visitVarInsn(Opcodes.ASTORE, stackThrowable);

			execptionalFinally = new Label();
			mv.visitLabel(execptionalFinally);

			if (exposeProxy) {
				mv.visitVarInsn(Opcodes.ALOAD, localOldProxy);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, AOP_CONTEXT_INTERNAL_NAME, SET_CURRENT_PROXY_METHOD, SET_CURRENT_PROXY_DESCRIPTOR);
				mv.visitInsn(Opcodes.POP);
			}

			if (!staticTargetSource) {

				// TODO: need embedded try/catch block here
				mv.visitVarInsn(Opcodes.ALOAD, localTargetSource);
				mv.visitVarInsn(Opcodes.ALOAD, localTarget);
				mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, TARGET_SOURCE_INTERNAL_NAME, "releaseTarget", "(Ljava/lang/Object;)V");
			}

			mv.visitVarInsn(Opcodes.ALOAD, stackThrowable);
			mv.visitInsn(Opcodes.ATHROW);
		}

		// close
		mv.visitLabel(exit);

		// return now for void
		if (returnType == void.class) {
			mv.visitInsn(getReturnOpcodeForType(method.getReturnType()));
		}

		for (int i = 0; i < handlerLabels.length; i++) {
			mv.visitTryCatchBlock(openTry, closeTry, handlerLabels[i], Type.getInternalName(exceptionTypes[i]));
		}

		mv.visitTryCatchBlock(openTry, closeTry, catchNoSuchMethodException, Type.getInternalName(NoSuchMethodException.class));
		mv.visitTryCatchBlock(openTry, closeTry, catchRuntimeException, Type.getInternalName(RuntimeException.class));
		mv.visitTryCatchBlock(openTry, closeTry, catchThrowable, Type.getInternalName(Throwable.class));

		if (requiresFinally) {
			mv.visitTryCatchBlock(openTry, closeTry, catchForFinally, null);

			// TODO: test expose proxy with declared exceptions
			mv.visitTryCatchBlock(catchNoSuchMethodException, execptionalFinally, catchForFinally, null);
		}

		mv.visitMaxs(0, 0);
	}

	private void bundleArgTypes(MethodVisitor mv, Class[] parameterTypes, int stackIndex) {
		int size = parameterTypes.length;

		if (size == 0) {
			mv.visitInsn(Opcodes.ACONST_NULL);
			mv.visitVarInsn(Opcodes.ASTORE, stackIndex);
		}
		else {
			visitIntegerInsn(size, mv);
			mv.visitTypeInsn(Opcodes.ANEWARRAY, CLASS_INTERNAL_NAME);
			mv.visitVarInsn(Opcodes.ASTORE, stackIndex);

			for (int i = 0; i < parameterTypes.length; i++) {
				Class parameterType = parameterTypes[i];

				// load the array
				mv.visitVarInsn(Opcodes.ALOAD, stackIndex);

				// load the index
				visitIntegerInsn(i, mv);

				// load the class
				if (parameterType.isPrimitive()) {
					visitGetPrimitiveType(mv, parameterType);
				}
				else {
					mv.visitLdcInsn(Type.getType(parameterType));
				}

				// store in array
				mv.visitInsn(Opcodes.AASTORE);
			}
		}
	}

	private void bundleArgs(MethodVisitor mv, Class[] parameterTypes, int stackIndex) {
		// create the object array
		int size = parameterTypes.length;

		if (size == 0) {
			mv.visitInsn(Opcodes.ACONST_NULL);
			mv.visitVarInsn(Opcodes.ASTORE, stackIndex);
		}
		else {
			visitIntegerInsn(size, mv);
			mv.visitTypeInsn(Opcodes.ANEWARRAY, OBJECT_INTERNAL_NAME);
			mv.visitVarInsn(Opcodes.ASTORE, stackIndex);

			int stackCount = 1;
			for (int i = 0; i < parameterTypes.length; i++) {
				Class parameterType = parameterTypes[i];

				// load the array
				mv.visitVarInsn(Opcodes.ALOAD, stackIndex);

				// load the index
				visitIntegerInsn(i, mv);

				// load the argument
				mv.visitVarInsn(getLoadOpcodeForType(parameterType), stackCount);

				if (parameterType.isPrimitive()) {
					// wrap primtive
					visitWrapPrimitive(mv, parameterType);
				}

				// store in array
				mv.visitInsn(Opcodes.AASTORE);

				stackCount += getLocalsSizeForType(parameterType);

			}
		}
	}
}
